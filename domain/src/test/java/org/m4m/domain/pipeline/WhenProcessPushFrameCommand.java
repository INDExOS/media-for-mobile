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

import org.junit.Assert;
import org.junit.Test;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.MediaSource;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class WhenProcessPushFrameCommand extends TestBase {
    @Test
    public void fillDecoderInputBuffer() {
        Frame encodedFrame = create.frame(0, 1, 2).construct();
        MediaSource mediaSource = create.mediaSource()
                .with(encodedFrame)
                .with(Frame.EOF())
                .construct();
        mediaSource.start();
        ByteBuffer inputBuffer = ByteBuffer.allocate(3);
        IMediaCodec mediaCodec = create.mediaCodec().withInputBuffer(inputBuffer).construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        decoder.start();
        decoder.fillCommandQueues();

        new PushDataCommandHandler(mediaSource, decoder, decoder).handle();

        Assert.assertThat(encodedFrame.getByteBuffer().array(), is(equalTo(inputBuffer.array())));
    }
}

