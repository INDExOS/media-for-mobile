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

import org.m4m.domain.IEglContext;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.ISurface;
import org.m4m.domain.MediaFormat;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class MediaCodecFather {
    private IMediaCodec mediaCodec;
    ArrayList<ByteBuffer> inputBuffers = new ArrayList<ByteBuffer>();
    ArrayList<IMediaCodec.BufferInfo> inputBufferInfos = new ArrayList<IMediaCodec.BufferInfo>();
    private int currentInputBufferIndex = 0;
    ArrayList<ByteBuffer> outputBuffers = new ArrayList<ByteBuffer>();
    ArrayList<IMediaCodec.BufferInfo> outputBufferInfos = new ArrayList<IMediaCodec.BufferInfo>();
    private int currentOutputBufferIndex = 0;
    private Father create;
    private ISurface surface;
    private ArrayList<Integer> dequeueOutputBufferResults = new ArrayList<Integer>();
    private ArrayList<Integer> dequeueInputBufferResults = new ArrayList<Integer>();
    private MediaFormat mediaFormat;

    public MediaCodecFather(Father create) {
        this.create = create;
        this.surface = create.surface().construct();
        this.mediaFormat = create.videoFormat().construct();
    }

    public MediaCodecFather withInputBuffer(ByteBuffer inputBuffer) {
        inputBuffers.add(inputBuffer);
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.size = inputBuffer.capacity();
        inputBufferInfos.add(bufferInfo);
        return this;
    }

    public MediaCodecFather withInputBuffer(final int... buffer) {
        inputBuffers.add(create.byteBuffer(buffer));
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.size = buffer.length;
        inputBufferInfos.add(bufferInfo);
        return this;
    }

    public MediaCodecFather withOutputBuffer(final int... buffer) {
        outputBuffers.add(create.byteBuffer(buffer));
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.size = buffer.length;
        outputBufferInfos.add(bufferInfo);
        return this;
    }

    public MediaCodecFather withOutputBuffer(ByteBuffer decodedBytes) {
        outputBuffers.add(decodedBytes);
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.size = decodedBytes.capacity();
        outputBufferInfos.add(bufferInfo);
        return this;
    }

    public MediaCodecFather withOutputBufferInfo(IMediaCodec.BufferInfo bufferInfo) {
        outputBufferInfos.set(outputBufferInfos.size() - 1, bufferInfo);
        return this;
    }

    public MediaCodecFather withSurface(ISurface surface) {
        this.surface = surface;
        return this;
    }

    public MediaCodecFather withMediaCodec(IMediaCodec mediaCodec) {
        this.mediaCodec = mediaCodec;
        return this;
    }

    public MediaCodecFather withDequeueOutputBufferIndex(int... dequeueOutputBufferResult) {
        for (int result : dequeueOutputBufferResult) {
            this.dequeueOutputBufferResults.add(result);
        }
        return this;
    }

    public MediaCodecFather withDequeueInputBufferIndex(int... dequeueInputBufferResult) {
        for (int result : dequeueInputBufferResult) {
            withInputBuffer(10);
            this.dequeueInputBufferResults.add(result);
        }
        return this;
    }

    public MediaCodecFather withSampleTime(int sampleTime) {
        outputBufferInfos.get(outputBufferInfos.size() - 1).presentationTimeUs = sampleTime;
        return this;
    }

    public MediaCodecFather withOutputFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
        return this;
    }

    public IMediaCodec construct() {
        if (mediaCodec == null) {
            mediaCodec = mock(IMediaCodec.class);
            if (inputBuffers.size() == 0) inputBuffers.add(ByteBuffer.allocate(10));
            when(mediaCodec.getInputBuffers()).thenReturn(inputBuffers.toArray(new ByteBuffer[inputBuffers.size()]));

            if (outputBuffers.size() == 0) outputBuffers.add(ByteBuffer.allocate(10));
            when(mediaCodec.getOutputBuffers()).thenReturn(outputBuffers.toArray(new ByteBuffer[outputBuffers.size()]));

            if (outputBufferInfos.size() == 0) outputBufferInfos.add(new IMediaCodec.BufferInfo());
            when(mediaCodec.dequeueOutputBuffer(any(IMediaCodec.BufferInfo.class), anyInt()))
                    .thenAnswer(new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            IMediaCodec.BufferInfo bufferInfo = (IMediaCodec.BufferInfo) invocation.getArguments()[0];

                            if (currentOutputBufferIndex >= dequeueOutputBufferResults.size()) {
                                return IMediaCodec.INFO_TRY_AGAIN_LATER;
                            }
                            int outputBufferIndex = dequeueOutputBufferResults.size() == 0 ? 0 : dequeueOutputBufferResults.get(currentOutputBufferIndex);
                            currentOutputBufferIndex++;

                            IMediaCodec.BufferInfo info;
                            if (outputBufferIndex >= 0) {
                                info = outputBufferInfos.get(outputBufferIndex);
                            } else {
                                info = new IMediaCodec.BufferInfo();
                            }

                            bufferInfo.size = info.size;
                            bufferInfo.offset = info.offset;
                            bufferInfo.presentationTimeUs = info.presentationTimeUs;
                            bufferInfo.flags = info.flags;
                            return outputBufferIndex;
                        }
                    });
            when(mediaCodec.dequeueInputBuffer(anyLong()))
                    .thenAnswer(new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                            if (dequeueInputBufferResults.size() == 0) return 0;
                            if (currentInputBufferIndex > dequeueInputBufferResults.size() - 1) return -1;

                            int index = dequeueInputBufferResults.get(currentInputBufferIndex);
                            currentInputBufferIndex++;
                            return index;
                        }
                    });

            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                    if(currentInputBufferIndex > -1) {
                        currentInputBufferIndex--;
                    }
                    return null;
                }
            }).when(mediaCodec).queueInputBuffer(anyInt(), anyInt(), anyInt(), anyLong(), anyInt());

            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    int outputBufferIndex;

                    if (currentOutputBufferIndex >= dequeueOutputBufferResults.size()) {
                        --currentOutputBufferIndex;
                    }

                    outputBufferIndex = dequeueOutputBufferResults.size() == 0 ? 0 : dequeueOutputBufferResults.get(currentOutputBufferIndex);

                    if (outputBufferIndex < 0) {
                        outputBufferIndex = 0;
                        dequeueOutputBufferResults.set(currentOutputBufferIndex, 0);
                    }

                    outputBufferInfos.get(outputBufferIndex).flags = IMediaCodec.BUFFER_FLAG_END_OF_STREAM;

                    return null;
                }
            }).when(mediaCodec).signalEndOfInputStream();

            if (surface == null) surface = create.surface().construct();
            when(mediaCodec.createInputSurface()).thenReturn(surface);
            when(mediaCodec.createSimpleInputSurface(any(IEglContext.class))).thenReturn(surface);
            when(mediaCodec.getOutputFormat()).thenReturn(mediaFormat);
        }

        return mediaCodec;
    }
}
