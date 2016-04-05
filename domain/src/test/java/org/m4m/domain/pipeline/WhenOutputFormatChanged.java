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
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.Pair;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

public class WhenOutputFormatChanged extends TestBase {
    @Test
    public void decoderKeepsOriginalTrackId() throws Exception {
        MultipleMediaSource mediaSource = create.multipleMediaSource()
                .with(create.mediaSource()
                        .withVideoTrack(0)
                        .withAudioTrack(1)
                        .with(1).videoFrames()
                        .construct())
                .with(create.mediaSource()
                        .withAudioTrack(0)
                        .withVideoTrack(1)
                        .with(1).videoFrames()
                        .construct())
                .construct();
        mediaSource.start();
        mediaSource.selectTrack(0);
        mediaSource.selectTrack(1);

        VideoDecoder decoder = create.videoDecoder().construct();
        decoder.setTrackId(0);
        decoder.start();

        new ConfigureVideoDecoderCommandHandler(mediaSource, decoder).handle();
        new PushDataCommandHandler(mediaSource, decoder, decoder).handle();
        decoder.fillCommandQueues();
        new OutputFormatChangedHandler(mediaSource, decoder, decoder).handle();
        decoder.fillCommandQueues();
        new ConfigureVideoDecoderCommandHandler(mediaSource, decoder).handle();

        assertThat(decoder.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NeedInputFormat, 0));
    }

    @Test
    public void restoreCommandsIfDecoderHasNoFreeInputBuffers() throws RuntimeException {
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource().construct();
        VideoDecoder decoder = create.videoDecoder()
                .withDequeueInputBufferIndex(-1)
                .construct();

        new OutputFormatChangedHandler(multipleMediaSource, decoder, decoder).handle();

        assertThat(multipleMediaSource.getOutputCommandQueue()).equalsTo(Command.OutputFormatChanged);
        assertThat(decoder.getInputCommandQueue()).equalsTo(Command.NextPair, Command.NeedData);
    }

    @Test
    public void restoreCommandsWithCorrectTrackId_IfDecoderHasNoFreeInputBuffers() throws RuntimeException {
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource().construct();
        VideoDecoder decoder = create.videoDecoder()
                .withDequeueInputBufferIndex(-1)
                .construct();
        decoder.setTrackId(1);

        new OutputFormatChangedHandler(multipleMediaSource, decoder, decoder).handle();

        assertThat(decoder.getInputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.NextPair, 1), new Pair<Command, Integer>(Command.NeedData, 1));
    }
}
