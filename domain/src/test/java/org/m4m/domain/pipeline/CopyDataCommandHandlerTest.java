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

import org.junit.Before;
import org.junit.Test;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class CopyDataCommandHandlerTest extends TestBase {
    private VideoDecoder decoder;
    private AudioEncoder encoder;
    private IMediaCodec mediaCodec;

    @Before
    public void setUp() throws Exception {
        Frame frame = create.frame().construct();
        decoder = create.videoDecoder().whichDecodesTo(frame).construct();
        decoder.push(frame);
        mediaCodec = create.mediaCodec().withInputBuffer(0).construct();
        encoder = create.audioEncoder().with(mediaCodec).construct();
        decoder.start();
        encoder.start();
        encoder.fillCommandQueues();
    }

    @Test
    public void dequeueAndQueueInputBuffers() {
        new CopyDataCommandHandler(decoder, encoder).handle();

        verify(mediaCodec, atLeastOnce()).dequeueInputBuffer(anyLong());
        verify(mediaCodec).queueInputBuffer(eq(0), anyInt(), anyInt(), anyLong(), anyInt());
    }

    @Test
    public void handleEof() {
        decoder = create.videoDecoder().whichDecodesTo(Frame.EOF()).construct();
        decoder.push(Frame.EOF());
        decoder.start();

        new CopyDataCommandHandler(decoder, encoder).handle();

        verify(mediaCodec).queueInputBuffer(anyInt(), anyInt(), anyInt(), anyLong(), eq(IMediaCodec.BUFFER_FLAG_END_OF_STREAM));
    }
}
