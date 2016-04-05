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

package org.m4m.domain;

import org.m4m.VideoFormat;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;

public class VideoEncoderTest extends TestBase {
    @Test
    public void canCreateSurface() throws Exception {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        ISurface surface = create.surface().construct();
        when(mediaCodec.createInputSurface()).thenReturn(surface);

        VideoEncoder encoder = new VideoEncoder(mediaCodec);

        assertEquals(surface, encoder.getSurface());
    }

    @Test
    public void canConfigure() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();

        VideoFormat videoFormat = create.videoFormat()
                .withFrameSize(1024, 768)
                .withBitRate(100)
                .withFrameRate(10)
                .withIFrameInterval(1)
                .construct();
        MediaCodecInfo.CodecCapabilities codecCapabilities = new MediaCodecInfo.CodecCapabilities();
        videoFormat.setColorFormat(codecCapabilities.COLOR_FormatSurface);

        VideoEncoder encoder = new VideoEncoder(mediaCodec);
        encoder.setMediaFormat(videoFormat);
        encoder.configure();

        ArgumentCaptor<VideoFormat> actualVideoFormat = ArgumentCaptor.forClass(VideoFormat.class);
        verify(mediaCodec).configure(actualVideoFormat.capture(), any(ISurfaceWrapper.class), anyInt());
        assertEquals(100, actualVideoFormat.getValue().getVideoBitRateInKBytes());
        assertEquals(10, actualVideoFormat.getValue().getVideoFrameRate());
        assertEquals(1, actualVideoFormat.getValue().getVideoIFrameInterval());
        assertEquals(2130708361, actualVideoFormat.getValue().getInteger("color-format"));
    }

    @Test
    public void getSimpleSurface_whenContextIsSpecified_usesSpecifiedContext() {
        IEglContext eglContext = mock(IEglContext.class);
        PreviewContext previewContext = new PreviewContext(null, 0, eglContext);
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoEncoder videoEncoder = create.videoEncoder().with(mediaCodec).construct();

        videoEncoder.getSimpleSurface(previewContext.getEglContext());

        verify(mediaCodec).createSimpleInputSurface(eq(eglContext));
    }

    @Test
    public void uponEncoderStart_CommandQueuesAreEmpty() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();
        videoEncoder.start();

        assertThat(videoEncoder.getInputCommandQueue()).isEmpty();
        assertThat(videoEncoder.getOutputCommandQueue()).isEmpty();
    }

    @Test
    public void push_afterReceivingOneFrame_StartFeeding() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();

        ISurface surface = create.surface().construct();
        videoEncoder.notifySurfaceReady(surface);

        videoEncoder.push(a.frame().construct());

        assertThat(videoEncoder.getInputCommandQueue()).equalsTo(Command.NeedData);
    }

    @Test
    public void push_afterReceivingTwoFrames_StopFeeding() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();

        ISurface surface = create.surface().construct();
        videoEncoder.notifySurfaceReady(surface);
        videoEncoder.notifySurfaceReady(surface);

        videoEncoder.push(a.frame().construct());

        assertThat(videoEncoder.getInputCommandQueue()).isEmpty();
    }

    @Test
    public void feedMeIfNotDraining_IgnoreInputBuffers() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        when(mediaCodec.dequeueInputBuffer(anyLong())).thenThrow(IllegalStateException.class);
        VideoEncoder videoEncoder = create.videoEncoder().with(mediaCodec).construct();

        videoEncoder.feedMeIfNotDraining();

        assertThat(videoEncoder.getInputCommandQueue()).equalsTo(Command.NeedData);
    }

//    @Test
//    public void uponDrain_InputCommandQueueIsEmpty_OutputCommandQueueIsEndOfFileCommand() {
//        VideoEncoder encoder = create.videoEncoder().construct();
//        encoder.start();
//
//        encoder.fillCommandQueues();
//        encoder.getInputCommandQueue().queue(Command.NeedData, 0);
//        encoder.drain(0);
//
//        assertThat(encoder.getInputCommandQueue()).isEmpty();
//        assertThat(encoder.getOutputCommandQueue()).equalsTo(Command.EndOfFile);
//    }

    @Test
    public void whenDraining_mustSignalEndOfInputStream() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoEncoder encoder = create.videoEncoder().with(mediaCodec).construct();
        encoder.start();

        encoder.drain(0);

        verify(mediaCodec).signalEndOfInputStream();
    }

//    @Test
//    public void pushWhenDrained_mustNotSayNeedData() {
//        VideoEncoder encoder = create.videoEncoder().withDequeueOutputBufferIndex(-1).construct();
//        encoder.start();
//
//        encoder.drain(0);
//        encoder.checkIfOutputQueueHasData();
//
//        Assert.assertEquals(PluginState.Drained, encoder.state);
//
//        encoder.push(create.frame().construct());
//
//        assertThat(encoder.getInputCommandQueue()).isEmpty();
//    }
}
