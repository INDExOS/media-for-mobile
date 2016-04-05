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

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class VideoDecoderTest extends TestBase {

    @Test
    public void mustSetOutputSurface() {
        VideoDecoder decoder = create.videoDecoder().construct();
        ISurfaceWrapper surface = create.surfaceContainer().construct();

        decoder.setOutputSurface(surface);

        assertNull(decoder.getSurface());
    }

    @Test
    public void decoderStop_mustClearInputBufferIndexes() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        Decoder decoder = create.videoDecoder().with(mediaCodec).construct();

        decoder.start();

        Frame frame = create.frame().construct();
        decoder.push(frame);
        decoder.push(frame);

        Assert.assertEquals(2, decoder.inputBufferIndexes.size());

        decoder.stop();

        Assert.assertEquals(0, decoder.inputBufferIndexes.size());
    }

    @Test
    public void decoderStop_mustClearOutputBufferIndexes() {
        IMediaCodec mediaCodec = create.mediaCodec()
                .withOutputBuffer(1, 1, 1).withSampleTime(100)
                .withOutputBuffer(2, 2, 2).withSampleTime(200)
                .withDequeueOutputBufferIndex(1, 0)
                .construct();
        Decoder decoder = create.videoDecoder().with(mediaCodec).construct();

        decoder.start();

        Frame frame = create.frame().construct();
        decoder.push(frame);
        decoder.push(frame);

        Assert.assertEquals(2, decoder.outputBufferIndexes.size());

        decoder.stop();

        Assert.assertEquals(0, decoder.outputBufferIndexes.size());
    }

    @Test
    public void isLastFile_returnsFalse() {
        VideoDecoder decoder = create.videoDecoder().construct();

        assertFalse(decoder.isLastFile());
    }

    @Test
    public void mediaCodec_releaseOutputBuffer_doRenderVariableMustBeTrue() {
        IMediaCodec mediaCodec = create.mediaCodec()
                .withDequeueOutputBufferIndex(1)
                .construct();
        VideoDecoder decoder = create.videoDecoder()
                .with(mediaCodec)
                .construct();
        ISurfaceWrapper surface = create.surfaceContainer().construct();

        decoder.setOutputSurface(surface);
        decoder.releaseOutputBuffer(1);

        verify(mediaCodec).releaseOutputBuffer(eq(1), eq(true));
    }

}
