/*
 * Copyright 2014-2016 Media for Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.m4m.domain.dsl;

import org.m4m.Uri;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaExtractor;
import org.m4m.domain.MediaFormat;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class MediaExtractorFather {
    private final Father create;
    private List<MediaFormat> tracks = new LinkedList<MediaFormat>();

    public void withFilePath(String filePath) {
        when(mediaExtractor.getFilePath()).thenReturn(filePath);
    }

    public void withFileDescriptor(FileDescriptor fileDescriptor) {
        when(mediaExtractor.getFileDescriptor()).thenReturn(fileDescriptor);
    }

    public void withUri(Uri uri) {
        when(mediaExtractor.getUri()).thenReturn(uri);
    }

    public void withInfinite(Frame frame) {
        withFrame(frame);
        advanceAnswer.turnInfiniteModeOn();
    }

    private class MyAnswer<T> implements Answer {
        private Queue<T> dataQueue = new LinkedList<T>();

        public void add(T value) {
            dataQueue.add(value);
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            if (!dataQueue.isEmpty()) {
                return peek();
            } else {
                return -1;
            }
        }

        public boolean isEmpty() {
            return dataQueue.isEmpty();
        }

        public void poll() {
            dataQueue.poll();
        }

        public T peek() {
            return dataQueue.peek();
        }
    }

    private class AdvanceAnswer implements Answer {
        private MyAnswer<Long> ptsAnswer;
        private MyAnswer<Integer> flagsAnswer;
        private MyAnswer<Integer> trackIdAnswer;
        private ReadSampleDataAnswer readSampleDataAnswer;
        private boolean isInfiniteMode = false;

        public AdvanceAnswer(MyAnswer<Long> ptsAnswer, MyAnswer<Integer> flagsAnswer, MyAnswer<Integer> trackIdAnswer, ReadSampleDataAnswer readSampleDataAnswer) {
            this.ptsAnswer = ptsAnswer;
            this.flagsAnswer = flagsAnswer;
            this.trackIdAnswer = trackIdAnswer;
            this.readSampleDataAnswer = readSampleDataAnswer;
        }

        public void turnInfiniteModeOn() {
            isInfiniteMode = true;
        }

        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            if (isInfiniteMode) return null;

            if (!ptsAnswer.isEmpty()) {
                ptsAnswer.poll();
                flagsAnswer.poll();
                trackIdAnswer.poll();
                readSampleDataAnswer.poll();
            }

            return null;
        }
    }

    private class ReadSampleDataAnswer implements Answer {
        private Queue<ByteBuffer> dataQueue = new LinkedList<ByteBuffer>();
        private Queue<Integer> lenQueue = new LinkedList<Integer>();

        public void add(ByteBuffer b) {
            dataQueue.add(b);
        }

        public void add(int len) {
            lenQueue.add(len);
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] != null) {
                ByteBuffer buffer = (ByteBuffer) arguments[0];
                buffer.clear();
                ByteBuffer myBuffer = dataQueue.peek();
                if (myBuffer != null && myBuffer.capacity() > 0) {
                    myBuffer.rewind();
                    buffer.rewind();
                    buffer.put(myBuffer);
                    buffer.rewind();
                    myBuffer.rewind();
                }
            }
            Integer length = lenQueue.peek();


            return length != null ? length : -1;
        }

        public void poll() {
            dataQueue.poll();
            lenQueue.poll();
        }
    }

    private class SeekAnswer implements Answer {
        private MyAnswer<Long> ptsAnswer;
        private MyAnswer<Integer> flagsAnswer;
        private MyAnswer<Integer> trackIdAnswer;
        private ReadSampleDataAnswer readSampleDataAnswer;

        public SeekAnswer(MyAnswer<Long> ptsAnswer, MyAnswer<Integer> flagsAnswer, MyAnswer<Integer> trackIdAnswer, ReadSampleDataAnswer readSampleDataAnswer) {
            this.ptsAnswer = ptsAnswer;
            this.flagsAnswer = flagsAnswer;
            this.trackIdAnswer = trackIdAnswer;
            this.readSampleDataAnswer = readSampleDataAnswer;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Object[] arguments = invocation.getArguments();
            Long seekPosition = (Long) arguments[0];

            while (ptsAnswer.peek() != null && ptsAnswer.peek() < seekPosition) {
                ptsAnswer.poll();
                flagsAnswer.poll();
                trackIdAnswer.poll();
                readSampleDataAnswer.poll();
            }

            return null;
        }
    }

    MyAnswer<Long> ptsAnswer = new MyAnswer<Long>();
    MyAnswer<Integer> flagsAnswer = new MyAnswer<Integer>();
    MyAnswer<Integer> trackIdAnswer = new MyAnswer<Integer>();
    ReadSampleDataAnswer readSampleDataAnswer = new ReadSampleDataAnswer();
    AdvanceAnswer advanceAnswer = new AdvanceAnswer(ptsAnswer, flagsAnswer, trackIdAnswer, readSampleDataAnswer);
    SeekAnswer seekAnswer = new SeekAnswer(ptsAnswer, flagsAnswer, trackIdAnswer, readSampleDataAnswer);
    private final IMediaExtractor mediaExtractor;

    public MediaExtractorFather(Father create) {
        this.create = create;
        mediaExtractor = mock(IMediaExtractor.class);
        doAnswer(readSampleDataAnswer).when(mediaExtractor).readSampleData(any(ByteBuffer.class));
        doAnswer(ptsAnswer).when(mediaExtractor).getSampleTime();
        doAnswer(flagsAnswer).when(mediaExtractor).getSampleFlags();
        doAnswer(trackIdAnswer).when(mediaExtractor).getSampleTrackIndex();
        doAnswer(advanceAnswer).when(mediaExtractor).advance();
        doAnswer(seekAnswer).when(mediaExtractor).seekTo(anyLong(), anyInt());
    }

    public MediaExtractorFather withSampleData(final ByteBuffer buffer, final int length) {
        readSampleDataAnswer.add(buffer);
        readSampleDataAnswer.add(length);
        return this;
    }

    public MediaExtractorFather withTimeStamp(long pts) {
        ptsAnswer.add(pts);
        return this;
    }

    public MediaExtractorFather withFrame(final Frame frame) {
        readSampleDataAnswer.add(frame.getByteBuffer());
        readSampleDataAnswer.add(frame.getLength());
        withTimeStamp(frame.getSampleTime());
        withFlag(frame.getFlags());
        withTrackId(frame.getTrackId());

        return this;
    }

    private MediaExtractorFather withFlag(int flags) {
        flagsAnswer.add(flags);
        return this;
    }

    private MediaExtractorFather withTrackId(int trackId) {
        trackIdAnswer.add(trackId);
        return this;
    }

    public MediaExtractorFather withTrack(MediaFormat format) {
        tracks.add(format);
        return this;
    }

    public IMediaExtractor construct() {
        if (tracks.size() == 0) {
            withTrack(create.videoFormat().construct());
        }
        when(mediaExtractor.getTrackCount()).thenReturn(tracks.size());
        for (int i = 0; i < tracks.size(); i++) {
            when(mediaExtractor.getTrackFormat(i)).thenReturn(tracks.get(i));
        }
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                if ((Integer)args[0] > tracks.size() - 1) {
                    throw new IllegalStateException("");
                }
                return null;
            }}).
                when(mediaExtractor).selectTrack(anyInt());
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                if ((Integer)args[0] > tracks.size() - 1) {
                    throw new IllegalStateException("");
                }
                return null;
            }}).
                when(mediaExtractor).unselectTrack(anyInt());

        return mediaExtractor;
    }

}
