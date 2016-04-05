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

package org.m4m.domain.pipeline;

import org.m4m.VideoFormat;
import org.junit.Test;
import org.m4m.domain.Command;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IOutput;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.Pair;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class PushDataCommandHandlerTest extends TestBase {
    @Test
    public void pushDataFromMediaSourceToDecoder() {
        Frame frame = create.frame(1, 2, 3).construct();
        IOutput mediaSource = create.mediaSource().with(frame)
                .construct();
        mediaSource.start();

        IMediaCodec mediaCodec = mock(IMediaCodec.class);
        ByteBuffer decoderInputBuffer = create.byteBuffer(0, 0, 0);
        when(mediaCodec.getInputBuffers()).thenReturn(new ByteBuffer[]{decoderInputBuffer});
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        decoder.start();
        decoder.fillCommandQueues();

        new PushDataCommandHandler(mediaSource, decoder, decoder).handle();

        assertEquals(create.byteBuffer(1, 2, 3), decoderInputBuffer);
    }

    @Test
    public void whenPushFrameToDecoderItNeedsData() {
        Frame frame = create.frame(1, 2, 3).construct();
        IOutput mediaSource = create.mediaSource().with(frame)
                .construct();
        mediaSource.start();

        VideoDecoder decoder = create.videoDecoder().with(create.mediaCodec().construct()).construct();
        decoder.start();

        new PushDataCommandHandler(mediaSource, decoder, decoder).handle();

        assertThat(decoder.getInputCommandQueue()).contains(Command.NeedData, 0);
    }

    @Test
    public void decoderShouldAskCommandProcessorToProcessNextPair_IfNoFreeInputBuffers() {
        IOutput mediaSource = create.mediaSource().with(1).frame().construct();
        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(-1).construct();

        decoder.getInputCommandQueue().dequeue();
        new PushDataCommandHandler(mediaSource, decoder, decoder).handle();

        assertThat(decoder.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NextPair, 0), new Pair<Command, Integer>(Command.NeedData, 0));
    }

    @Test
    public void shouldTriggerHasDataCommandForEof() {
        IOutput mediaSource = create.mediaSource()
                .with(1).frame()
                .construct();
        mediaSource.start();
        VideoDecoder decoder = create.videoDecoder().withDequeueOutputBufferIndex(0).construct();
        decoder.start();
        decoder.fillCommandQueues();

        new PushDataCommandHandler(mediaSource, decoder, decoder).handle();
        new EofCommandHandler(mediaSource, decoder, decoder).handle();

        decoder.getFrame();
        Frame actualFrame = decoder.getFrame();
        assertEquals(Frame.EOF(), actualFrame);
    }

    @Test
    public void shouldReconfigureDecoderWhenHandleEof() throws RuntimeException {
        IOutput twoFilesMediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(1).frame())
                .with(create.mediaSource().with(1).frame())
                .construct();
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        twoFilesMediaSource.start();
        decoder.start();
        decoder.fillCommandQueues();

        PushDataCommandHandler commandHandler = new PushDataCommandHandler(twoFilesMediaSource, decoder, decoder);
        commandHandler.handle(); // frame1
        OutputFormatChangedHandler outputFormatChangedHandler = new OutputFormatChangedHandler(twoFilesMediaSource, decoder, decoder);
        outputFormatChangedHandler.handle();
        commandHandler.handle(); // EOF, then frame2

        verify(mediaCodec).stop();
        verify(mediaCodec).configure(any(MediaFormat.class), any(ISurfaceWrapper.class), anyInt());
        verify(mediaCodec, times(2)).start();
    }

    @Test
    public void shouldSkipEof() throws RuntimeException {
        IOutput twoFilesMediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(1).frame(1, 1, 1))
                .with(create.mediaSource().with(1).frame(2, 2, 2))
                .construct();
        ByteBuffer decoderInputBuffer = create.byteBuffer(0, 0, 0);
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        when(mediaCodec.getInputBuffers()).thenReturn(new ByteBuffer[]{decoderInputBuffer});
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        twoFilesMediaSource.start();
        decoder.start();
        decoder.fillCommandQueues();

        PushDataCommandHandler pushDataCommandHandler = new PushDataCommandHandler(twoFilesMediaSource, decoder, decoder);
        pushDataCommandHandler.handle();
        pushDataCommandHandler.handle();

        assertEquals(create.byteBuffer(2, 2, 2), decoderInputBuffer);
    }

    @Test
    public void configureDecoderWithChangedMediaFormat() throws RuntimeException {
        IOutput twoFilesMediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(1).frame()
                        .with(create.videoFormat().withFrameSize(1024, 768).withBitRate(100).construct()))
                .with(create.mediaSource().with(1).frame()
                        .with(create.videoFormat().withFrameSize(1024, 768).withBitRate(250).construct()))
                .construct();
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        twoFilesMediaSource.start();
        decoder.start();
        decoder.fillCommandQueues();

        PushDataCommandHandler pushDataCommandHandler = new PushDataCommandHandler(twoFilesMediaSource, decoder, decoder);
        pushDataCommandHandler.handle();
        OutputFormatChangedHandler outputFormatChangedHandler = new OutputFormatChangedHandler(twoFilesMediaSource, decoder, decoder);
        outputFormatChangedHandler.handle();
        pushDataCommandHandler.handle();

        ArgumentCaptor<VideoFormat> mediaFormatCaptor = ArgumentCaptor.forClass(VideoFormat.class);
        verify(mediaCodec).configure(mediaFormatCaptor.capture(), any(ISurfaceWrapper.class), anyInt());
        assertEquals(250, mediaFormatCaptor.getValue().getVideoBitRateInKBytes());
    }

    @Test
    public void configureVideoDecoderWithVideoFormat() throws RuntimeException {
        IOutput twoFilesMediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(1).frame().withAudioTrack(0).withVideoTrack(1))
                .with(create.mediaSource()
                        .with(1).audioFrames()
                        .with(create.audioFormat().withBitRate(12).construct())
                        .with(1).videoFrames()
                        .with(create.videoFormat().withFrameSize(1024, 768).withBitRate(250).construct()))
                .construct();
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        twoFilesMediaSource.start();
        decoder.start();
        decoder.fillCommandQueues();

        PushDataCommandHandler pushDataCommandHandler = new PushDataCommandHandler(twoFilesMediaSource, decoder, decoder);
        pushDataCommandHandler.handle();
        OutputFormatChangedHandler outputFormatChangedHandler = new OutputFormatChangedHandler(twoFilesMediaSource, decoder, decoder);
        outputFormatChangedHandler.handle();
        pushDataCommandHandler.handle();

        ArgumentCaptor<VideoFormat> mediaFormatCaptor = ArgumentCaptor.forClass(VideoFormat.class);
        verify(mediaCodec).configure(mediaFormatCaptor.capture(), any(ISurfaceWrapper.class), anyInt());
        assertEquals(250, mediaFormatCaptor.getValue().getVideoBitRateInKBytes());
    }
}
