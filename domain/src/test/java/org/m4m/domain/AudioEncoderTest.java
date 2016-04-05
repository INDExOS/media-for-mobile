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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AudioEncoderTest extends TestBase {

    @Test
    public void uponEncoderStart_CommandQueuesAreEmpty() {
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.start();

        assertThat(encoder.getInputCommandQueue()).isEmpty();
        assertThat(encoder.getOutputCommandQueue()).isEmpty();
    }

    @Test
    public void fillCommandQueues_AddsNeedDataToEmptyInputCommandQueue() {
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.start();

        encoder.fillCommandQueues();

        assertThat(encoder.getInputCommandQueue()).equalsTo(Command.NeedData);
    }

    @Test
    public void fillCommandQueues_InputCommandQueueSizeDoesNotExceedTheNumberOfAvailableInputBuffers() {
        AudioEncoder encoder = create.audioEncoder().withDequeueInputBufferIndex(-1, -1, 0, 1).construct();
        encoder.start();

        for(int i = 0; i < 100; i++) {
            encoder.fillCommandQueues();
        }
        assertThat(encoder.getInputCommandQueue()).equalsTo(Command.NeedData, Command.NeedData);
    }

    @Test
    public void uponDrain_InputCommandQueueIsEmpty() {
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.start();

        encoder.fillCommandQueues();
        encoder.getInputCommandQueue().queue(Command.NeedData, 0);
        encoder.drain(0);

        assertThat(encoder.getInputCommandQueue()).isEmpty();
    }

    @Test
    public void uponPushFrameEOF_InputCommandQueueIsNotEmpty() {
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.start();

        encoder.fillCommandQueues();
        encoder.getInputCommandQueue().queue(Command.NeedData, 0);
        encoder.push(Frame.EOF());

        assertFalse(encoder.getInputCommandQueue().queue.isEmpty());
    }


    @Test
    public void saysNeedData_whenHasInputBuffer() {
        AudioEncoder encoder = create.audioEncoder().withDequeueInputBufferIndex(2).construct();
        encoder.start();

        encoder.fillCommandQueues();

        assertThat(encoder.getInputCommandQueue()).equalsTo(Command.NeedData);
    }

    @Test
    public void doesNotSayNeedData_whenHasNoInputBuffer() {
        AudioEncoder encoder = create.audioEncoder().withDequeueInputBufferIndex(-1).construct();
        encoder.start();

        encoder.fillCommandQueues();

        assertThat(encoder.getInputCommandQueue()).isEmpty();
    }

    @Test
    public void findFreeFrameReturnsNull_whenFillCommandQueuesNotCalled() {
        AudioEncoder encoder = create.audioEncoder().withDequeueInputBufferIndex(2).construct();
        encoder.start();

        Frame frame = encoder.findFreeFrame();

        assertNull(frame);
    }

    @Test
    public void findFreeFrameReturnsCorrectBufferIndex_whenHasInputBuffer() {
        AudioEncoder encoder = create.audioEncoder().withDequeueInputBufferIndex(0, 1).construct();
        encoder.start();

        Frame frame;

        encoder.fillCommandQueues();
        encoder.fillCommandQueues();

        frame = encoder.findFreeFrame();

        assertEquals(0, frame.getBufferIndex());

        frame = encoder.findFreeFrame();

        assertEquals(1, frame.getBufferIndex());
    }

    @Test
    public void pushWhenDraining_mustNotSayNeedData() {
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.start();

        encoder.drain(0);

        assertEquals(PluginState.Draining, encoder.state);

        encoder.push(create.frame().construct());

        assertThat(encoder.getInputCommandQueue()).isEmpty();
    }

    @Test
    public void pushWhenDrained_mustNotSayNeedData() {
        AudioEncoder encoder = create.audioEncoder().withDequeueOutputBufferIndex(-1).construct();
        encoder.start();

        encoder.drain(0);
        encoder.checkIfOutputQueueHasData();

        assertEquals(PluginState.Drained, encoder.state);

        encoder.push(create.frame().construct());

        assertThat(encoder.getInputCommandQueue()).isEmpty();
    }
}
