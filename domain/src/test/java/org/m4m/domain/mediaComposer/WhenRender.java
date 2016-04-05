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

package org.m4m.domain.mediaComposer;


import org.m4m.domain.TestBase;
import org.m4m.domain.pipeline.IOnStopListener;
import org.junit.Test;
import org.m4m.domain.CommandProcessor;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.Pipeline;
import org.m4m.domain.Render;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEncoder;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhenRender extends TestBase {
    @Test
    public void sinkReceivedEncodedFrames() throws IOException, IllegalAccessException, InstantiationException, OperationNotSupportedException {
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(2).videoFrames().construct())
                .construct();

        IMediaCodec decoderMediaCodec = create.mediaCodec()
                .withOutputBuffer(4, 5, 6)
                .withOutputBuffer(4, 5, 6)
                .withOutputBuffer()
                .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED, 0, 1)
                .construct();
        VideoDecoder decoder = create.videoDecoder().with(decoderMediaCodec).construct();

        IMediaCodec encoderMediaCodec = create.mediaCodec()
                .withOutputBuffer(44, 55, 66)
                .withOutputBuffer(44, 55, 66)
                .withOutputBuffer()
                .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED, 0, 1)
                .construct();
        VideoEncoder encoder = create.videoEncoder().with(encoderMediaCodec).construct();
        IMediaMuxer muxer = create.mediaMuxer().construct();

        final CommandProcessor commandProcessor = new CommandProcessor(new ProgressListenerFake());
        Pipeline pipeline = new Pipeline(commandProcessor);

        pipeline.setMediaSource(multipleMediaSource);
        pipeline.addVideoDecoder(decoder);
        pipeline.addVideoEncoder(encoder);
        pipeline.setSink(create.render().with(muxer).construct());
        pipeline.resolve();

        commandProcessor.process();

        ByteBuffer expectedBuffer = create.byteBuffer(44, 55, 66);
        IMediaCodec.BufferInfo expectedBufferInfo = new IMediaCodec.BufferInfo();
        expectedBufferInfo.size = 3;
        verify(muxer, times(2)).writeSampleData(eq(0), eq(expectedBuffer), eq(expectedBufferInfo));
        verify(muxer).stop();
        verify(muxer).release();
    }

    @Test
    public void decoderHandlesMediaFormatChange() throws IOException, OperationNotSupportedException {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        VideoEncoder encoder = create.videoEncoder().construct();

        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(1).videoFrames())
                .with(create.mediaSource().with(1).videoFrames())
                .construct();

        final CommandProcessor commandProcessor = new CommandProcessor(new ProgressListenerFake());
        Pipeline pipeline = new Pipeline(commandProcessor);
        pipeline.setMediaSource(multipleMediaSource);
        pipeline.addVideoDecoder(decoder);
        pipeline.addVideoEncoder(encoder);
        pipeline.setSink(create.render().construct());
        pipeline.resolve();

        commandProcessor.process();

        verify(mediaCodec, times(2)).configure(any(MediaFormat.class), any(ISurfaceWrapper.class), anyInt());
    }

    @Test
    public void renderNotifiesOnMediaStop() {
        IOnStopListener onStopListener = mock(IOnStopListener.class);
        Render render = create.render().withOnStopListener(onStopListener).construct();

        render.configure();
        render.drain(0);

        verify(onStopListener).onStop();
    }
}

