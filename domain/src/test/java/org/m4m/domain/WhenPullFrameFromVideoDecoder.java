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

import static junit.framework.Assert.assertEquals;

public class WhenPullFrameFromVideoDecoder extends TestBase {
    @Test
    public void shouldHandleOutputBufferChange() {
        IMediaCodec mediaCodec = create.mediaCodec()
            .withOutputBuffer()
            .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_BUFFERS_CHANGED, 0)
            .construct();

        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        decoder.start();

        decoder.push(create.frame().construct());
        decoder.push(create.frame().construct());
        Frame frame = decoder.getFrame();

        assertEquals(0, frame.getBufferIndex());
    }

    @Test
    public void canHandleEof() {
        IMediaCodec mediaCodec = create.mediaCodec()
            .withOutputBuffer(11)
            .withOutputBuffer(22)
            .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_BUFFERS_CHANGED, 0, 1)
            .construct();

        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        decoder.start();

        decoder.push(create.frame().construct());
        decoder.push(create.frame().construct());
        decoder.drain(0);

        Frame frame;
        frame = decoder.getFrame();
        assertEquals(0, frame.getBufferIndex());

        frame = decoder.getFrame();
        assertEquals(1, frame.getBufferIndex());

        frame = decoder.getFrame();
        assertEquals(Frame.EOF(), frame);
    }

    @Test
    public void canPullFrame() {
        IMediaCodec mediaCodec = create.mediaCodec()
                .withOutputBuffer(1, 2, 3, 4)
                .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED, 0)
                .construct();

        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();

        decoder.pull(create.frame().construct());
        decoder.drain(0);

        Frame pulledFrame = create.frame().withLength(4).construct();
        decoder.pull(pulledFrame);

        byte[] expectedByteArray = new byte[] {1, 2, 3, 4};

        Assert.assertArrayEquals(expectedByteArray, pulledFrame.getByteBuffer().array());
    }
}
