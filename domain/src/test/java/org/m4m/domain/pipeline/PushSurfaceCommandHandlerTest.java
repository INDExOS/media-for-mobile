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

import org.junit.Test;
import org.m4m.domain.Command;
import org.m4m.domain.Encoder;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.Pair;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

public class PushSurfaceCommandHandlerTest extends TestBase {
    @Test
    public void encoderShouldRequestMoreDataAfterNotifySurfaceReady() {
        VideoDecoder decoder = create.videoDecoder().construct();
        Encoder encoder = create.videoEncoder().with(create.surface().construct()).construct();

        handlePushSurfaceCommand(decoder, encoder);

        assertThat(encoder.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NeedData, 0));
    }

    @Test
    public void encoderShouldNotRequestMoreDataAfterNotifySurfaceReadyWhenDraining() {
        VideoDecoder decoder = create.videoDecoder().construct();
        Encoder encoder = create.videoEncoder().with(create.surface().construct()).construct();
        encoder.start();
        encoder.drain(0);

        handlePushSurfaceCommand(decoder, encoder);

        assertThat(encoder.getInputCommandQueue()).isEmpty();
    }

    @Test
    public void encoderShouldTriggerOutputFormatChangedCommand_IfFormatHasChanged() {
        VideoDecoder decoder = create.videoDecoder().construct();
        IMediaCodec mediaCodec = create.mediaCodec()
            .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            .construct();
        Encoder encoder = create.videoEncoder()
            .with(create.surface().construct())
            .with(mediaCodec)
            .construct();

        handlePushSurfaceCommand(decoder, encoder);

        assertThat(encoder.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.OutputFormatChanged, 0));
    }

    private void handlePushSurfaceCommand(VideoDecoder decoder, Encoder encoder) {
        encoder.getInputCommandQueue().dequeue();
        decoder.setOutputSurface(encoder.getSurface());
        new PushSurfaceCommandHandler(decoder, encoder).handle();
    }
}
