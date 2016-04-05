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

import org.m4m.AudioFormat;
import org.m4m.VideoFormat;
import org.junit.Test;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Command;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.Pair;
import org.m4m.domain.Plugin;
import org.m4m.domain.Render;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoEncoder;
import org.mockito.InOrder;

import java.nio.ByteBuffer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhenProcessPullFrameCommand extends TestBase {


    @Test
    public void putFrameToRender() {
        Frame decodedFrame = create.frame().construct();
        Plugin decoder = create.videoDecoder().whichDecodesTo(decodedFrame).construct();
        Render render = getRender();
        render.start();

        decoder.push(create.frame().construct()); // This is to generate decoder's output buffer index and buffer info
        new PullDataCommandHandler(decoder, render).handle();

        assertThat(render).willReceive(decodedFrame);
    }

    private Render getRender() {
        Render render = spy(create.render().construct());

//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                return null;
//            }
//        } ).when(render).pushWithReleaser(any(Frame.class), any(IPluginOutput.class));


        return render;
    }

    @Test
    public void decoderShouldRequestMoreData() {
        Plugin decoder = create.videoDecoder().construct();
        //prevent call to not constructed object mediamuxer
        Render render = getRender();
        render.start();

        decoder.getInputCommandQueue().dequeue();
        assertThat(decoder.getInputCommandQueue()).isEmpty();


        decoder.push(create.frame().construct()); // This is to generate decoder's output buffer index and buffer info
        new PullDataCommandHandler(decoder, render).handle();

        assertThat(decoder.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NeedData, 0), new Pair<Command, Integer>(Command.NeedData, 0));
    }

    @Test
    public void decoderShouldReleaseOutputBufferAfterWriteSampleData() {
        IMediaCodec mediaCodec = create
            .mediaCodec()
            .withOutputBuffer()
            .withOutputBuffer()
            .withDequeueOutputBufferIndex(1)
            .construct();
        Frame decodedFrame = create.frame().construct();
        Plugin decoder = create.videoDecoder()
                .with(mediaCodec).whichDecodesTo(decodedFrame).construct();


        IMediaMuxer muxer = create.mediaMuxer().construct();
        Render render = spy(create.render().with(muxer).construct());
        render.start();

        decoder.push(create.frame().construct()); // This is to generate decoder's output buffer index and buffer info
        new PullDataCommandHandler(decoder, render).handle();


        InOrder inOrder = inOrder(muxer, mediaCodec);

        inOrder.verify(muxer).writeSampleData(eq(decodedFrame.getTrackId()),  any(ByteBuffer.class), any(IMediaCodec.BufferInfo.class));
        inOrder.verify(mediaCodec).releaseOutputBuffer(eq(1), eq(false));
    }

    @Test
    public void bufferFramesUntilAllTracksAreConfigured() {
        VideoFormat videoFormat = create.videoFormat().construct();
        IMediaCodec videoMediaCodec = create.mediaCodec()
            .withOutputBuffer()
            .withOutputFormat(videoFormat)
            .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED, 0)
            .construct();
        VideoEncoder videoEncoder = create.videoEncoder().with(videoMediaCodec).construct();
        videoEncoder.push(create.frame().construct()); // to initialize output format. Phew!

        AudioFormat audioFormat = create.audioFormat().construct();
        IMediaCodec audioMediaCodec = create.mediaCodec()
            .withOutputBuffer()
            .withOutputFormat(audioFormat)
            .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED, 0)
            .construct();
        AudioEncoder audioEncoder = create.audioEncoder().with(audioMediaCodec).construct();
        audioEncoder.push(create.frame().construct()); // to initialize output format. Phew!

        IMediaMuxer muxer = create.mediaMuxer().construct();
        Render render = create.render().with(muxer).construct();
        render.configure();
        render.configure(); // to emulate connection to 2 plugins

        // configure audio track
        render.getInputCommandQueue().dequeue();
        new EncoderMediaFormatChangedCommandHandler(audioEncoder, render).handle();
        verify(muxer).addTrack(eq(audioFormat));

        // buffer audio frame
        render.getInputCommandQueue().dequeue();
        new PullDataCommandHandler(audioEncoder, render).handle();
        assertThat(render.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NeedInputFormat, 0));
        verify(muxer, never()).writeSampleData(anyInt(), any(ByteBuffer.class), any(IMediaCodec.BufferInfo.class));

        // configure video track
        new EncoderMediaFormatChangedCommandHandler(videoEncoder, render).handle();
        verify(muxer).addTrack(eq(audioFormat));
        verify(muxer).start();

        // write both audio and video frames
        reset(muxer);
        new PullDataCommandHandler(videoEncoder, render).handle();
        verify(muxer, times(2)).writeSampleData(anyInt(), any(ByteBuffer.class), any(IMediaCodec.BufferInfo.class));
    }
}
