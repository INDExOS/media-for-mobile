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
import org.m4m.domain.Encoder;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.ISurface;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class DrainCommandHandlerTest extends TestBase {
    @Test
    public void encoderShouldNotSwapBuffersIfEof() {
        VideoDecoder decoder = create.videoDecoder().construct();
        ISurface surface = create.surface().construct();
        Encoder encoder = create.videoEncoder().with(surface).construct();

        decoder.drain(0);
        decoder.setOutputSurface(surface);
        encoder.getInputCommandQueue().dequeue();
        new DrainCommandHandler(encoder).handle();

        verify(surface, never()).awaitNewImage();
        verify(surface, never()).swapBuffers();
    }

    @Test
    public void decoderShouldNotReleaseOutputBufferIfEof() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        ISurface surface = create.surface().construct();
        Encoder encoder = create.videoEncoder().with(surface).construct();

        decoder.drain(0);
        decoder.setOutputSurface(surface);
        encoder.getInputCommandQueue().dequeue();
        new DrainCommandHandler(encoder).handle();

        verify(mediaCodec, never()).releaseOutputBuffer(anyInt(), anyBoolean());
    }
}
