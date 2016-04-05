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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class WhenVideoDecoderFillsCommandQueues extends TestBase {
    @Test
    public void saysNeedData_whenHasInputBuffer() {
        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(2).construct();
        decoder.start();

        decoder.fillCommandQueues();

        assertThat(decoder.getInputCommandQueue()).equalsTo(Command.NeedData);
    }

    @Test
    public void doesNotSayNeedData_whenHasNoInputBuffer() {
        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(-1).construct();
        decoder.start();

        decoder.fillCommandQueues();

        assertThat(decoder.getInputCommandQueue()).isEmpty();
    }

    @Test
    public void findFreeFrameReturnsNull_whenFillCommandQueuesNotCalled() {
        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(0).construct();
        decoder.start();

        Frame frame = decoder.findFreeFrame();

        assertNull(frame);
    }

    @Test
    public void findFreeFrameReturnsCorrectBufferIndex_whenHasInputBuffer() {
        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(0).construct();
        decoder.start();

        decoder.fillCommandQueues();
        Frame frame = decoder.findFreeFrame();

        assertEquals(0, frame.getBufferIndex());
    }

    @Test
    public void callFillCommandQueuesTwiceAndFindFreeFrameOnce_whenHasInputBuffer() {
        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(0, 1).construct();
        decoder.start();

        decoder.fillCommandQueues();
        decoder.fillCommandQueues();
        Frame frame = decoder.findFreeFrame();

        assertEquals(0, frame.getBufferIndex());
    }

    /*
    @Test
    public void () {
        IMediaCodec mediaCodec = create.mediaCodec().withDequeueInputBufferIndex(2).construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        decoder.start();

        decoder.fillCommandQueues();
        decoder.push(a.frame().withInputBufferIndex(2).construct());

        verify(mediaCodec).queueInputBuffer(eq(2), anyInt(), anyInt(), anyLong(), anyInt());
    }
    */
}
