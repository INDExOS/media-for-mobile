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
import org.m4m.domain.Encoder;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.Pair;
import org.m4m.domain.Render;
import org.m4m.domain.TestBase;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class MediaFormatChangedCommandHandlerTest extends TestBase {
    @Test
    public void configureMuxer_WhenMediaFormatChanged() throws Exception {
        VideoFormat videoFormat = create.videoFormat().construct();
        videoFormat.setVideoFrameSize(1024, 768);
        videoFormat.setVideoBitRateInKBytes(250);
        videoFormat.setVideoFrameRate(25);
        videoFormat.setVideoIFrameInterval(10);
        IMediaCodec mediaCodec = create.mediaCodec()
                .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                .withOutputFormat(videoFormat)
                .construct();
        Encoder encoder = create.videoEncoder().with(mediaCodec).construct();
        IMediaMuxer muxer = create.mediaMuxer().construct();
        Render render = create.render().with(muxer).construct();
        render.configure();

        encoder.checkIfOutputQueueHasData();
        new EncoderMediaFormatChangedCommandHandler(encoder, render).handle();

        ArgumentCaptor<VideoFormat> actualMediaFormat = ArgumentCaptor.forClass(VideoFormat.class);
        verify(muxer).addTrack(actualMediaFormat.capture());
        assertEquals(250, actualMediaFormat.getValue().getVideoBitRateInKBytes());
        assertEquals(25, actualMediaFormat.getValue().getVideoFrameRate());
        assertEquals(10, actualMediaFormat.getValue().getVideoIFrameInterval());
        verify(muxer).start();
    }

    @Test
    public void configureEncoderOutputTrackId_WhenMediaFormatChanged() throws Exception {
        IMediaCodec mediaCodec = create.mediaCodec().withDequeueOutputBufferIndex(0).construct();
        MediaFormat mediaformat = create.videoFormat().construct();
        Encoder encoder = create.videoEncoder().with(mediaCodec).with(mediaformat).construct();
        encoder.getOutputCommandQueue().queue(Command.OutputFormatChanged, 0);
        Render render = create.render().construct();
        render.getInputCommandQueue().queue(Command.NeedInputFormat, 0);

        new EncoderMediaFormatChangedCommandHandler(encoder, render).handle();

        assertThat(encoder.getInputCommandQueue()).isEmpty();

        encoder.checkIfOutputQueueHasData();
        assertThat(encoder.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.HasData, 0));

        Frame frame = encoder.getFrame();
        assertEquals(0, frame.getTrackId());
        assertThat(encoder.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NeedData, 0));
    }
}
