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

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class WhenPullFrameFromEncoder extends TestBase {
    @Test
    public void shouldNotTriggerHasDataCommand_AfterOnePush_IfNoOutputBuffers() {
        IMediaCodec mediaCodec = create.mediaCodec().withDequeueOutputBufferIndex(-1).construct();
        Encoder encoder = new VideoEncoder(mediaCodec);
        assertThat(encoder.getOutputCommandQueue()).isEmpty();

        encoder.notifySurfaceReady(create.surface().construct());

        assertThat(encoder.getOutputCommandQueue()).isEmpty();
    }


    @Test
    public void shouldTriggerHasDataCommand_AfterOnePush_IfThereIsOutputBuffer() {
        IMediaCodec mediaCodec = create.mediaCodec().withDequeueOutputBufferIndex(0).construct();
        Encoder encoder = new VideoEncoder(mediaCodec);
        assertThat(encoder.getOutputCommandQueue()).isEmpty();

        encoder.notifySurfaceReady(create.surface().construct());
        encoder.checkIfOutputQueueHasData();

        assertThat(encoder.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.HasData, 0));
    }

    @Test
    public void shouldPullDataFromPreviouslyDequeuedBuffer() {
        ByteBuffer expectedByteBuffer = create.byteBuffer(1, 2, 3);
        IMediaCodec mediaCodec = create
                .mediaCodec()
                .withOutputBuffer()
                .withOutputBuffer(expectedByteBuffer)
                .withDequeueOutputBufferIndex(1, -1)
                .construct();
        Encoder encoder = new VideoEncoder(mediaCodec);
        assertThat(encoder.getOutputCommandQueue()).isEmpty();

        encoder.notifySurfaceReady(create.surface().construct());
        encoder.checkIfOutputQueueHasData();

        Frame frame = encoder.getFrame();
        Assert.assertThat(frame.getByteBuffer().array(), is(equalTo(expectedByteBuffer.array())));
    }

    @Test
    public void shouldPullEmptyFrameIfMediaCodecHasNoOutputBuffers() {
        IMediaCodec mediaCodec = create
                .mediaCodec()
                .withOutputBuffer()
                .withDequeueOutputBufferIndex(-1)
                .construct();
        Encoder encoder = new VideoEncoder(mediaCodec);

        encoder.notifySurfaceReady(create.surface().construct());
        encoder.checkIfOutputQueueHasData();

        Frame frame = encoder.getFrame();
        assertEquals(null, frame.getByteBuffer());
        assertEquals(0, frame.getLength());
        assertEquals(0, frame.getBufferIndex());
        assertEquals(0, frame.getFlags());
        assertEquals(0, frame.getTrackId());
    }

    @Test
    public void shouldPullTwoFrames() {
        IMediaCodec mediaCodec = create
                .mediaCodec()
                .withOutputBuffer(1, 1, 1).withSampleTime(100)
                .withOutputBuffer(2, 2, 2).withSampleTime(200)
                .withDequeueOutputBufferIndex(1, 0)
                .construct();
        Encoder encoder = new VideoEncoder(mediaCodec);

        encoder.notifySurfaceReady(create.surface().construct());
        encoder.checkIfOutputQueueHasData();
        encoder.notifySurfaceReady(create.surface().construct());
        encoder.checkIfOutputQueueHasData();

        Frame frame = encoder.getFrame();
        Assert.assertThat(frame.getByteBuffer().array(), is(equalTo(new byte[]{2, 2, 2})));
        assertEquals(200, frame.getSampleTime());

        frame = encoder.getFrame();
        Assert.assertThat(frame.getByteBuffer().array(), is(equalTo(new byte[]{1, 1, 1})));
        assertEquals(100, frame.getSampleTime());
    }
}
