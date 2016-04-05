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

import org.m4m.domain.dsl.CommandProcessorSpy;
import org.junit.Test;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.Pipeline;
import org.m4m.domain.Render;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import java.io.IOException;

import static org.mockito.Mockito.verify;

public class WhenPipelineRelease extends TestBase {

    @Test
    public void mediaMuxerDisposed() throws IOException {
        IMediaMuxer mediaMuxer = create.mediaMuxer().construct();
        Render render = create.render().with(mediaMuxer).construct();
        CommandProcessorSpy commandProcessor = create.commandProcessor().construct();
        Pipeline pipeline = create.pipeline()
                .with(commandProcessor)
                .with(create.mediaSource().frame(1, 2, 3, 4, 5).construct())
                .withVideoDecoder(create.videoDecoder().whichDecodesTo(create.frame(7, 7, 7, 7, 7).construct()).construct())
                .with(create.videoEncoder().withOutputFormatChanged()
                        .whichEncodesTo(create.frame(1, 1, 1).construct()).construct())
                .with(render).construct();
        

        pipeline.resolve();
        commandProcessor.process();
        pipeline.release();
        verify(mediaMuxer).release();
    }

    @Test
    public void decoderDisposed() throws IOException {

        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder videoDecoder = create.videoDecoder().with(mediaCodec).construct();

        Pipeline pipeline = create.pipeline()
                .with(create.mediaSource().construct())
                .withVideoDecoder(videoDecoder)
                .with(create.videoEncoder().construct())
                .with(create.render().construct()).construct();

        pipeline.resolve();
        pipeline.release();
        verify(mediaCodec).release();
    }


}
