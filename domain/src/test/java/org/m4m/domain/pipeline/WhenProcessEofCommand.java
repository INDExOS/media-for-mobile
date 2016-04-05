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
import org.m4m.domain.Command;
import org.m4m.domain.Frame;
import org.m4m.domain.IFrameAllocator;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhenProcessEofCommand extends TestBase {

    private MultipleMediaSource mediaSource;

    @Before
    public void Before() {
        mediaSource = create.multipleMediaSource().construct();
    }

    @Test
    public void sendEofFlagToMediaCodec() {
        MediaSource mediaSource = create.mediaSource().with(Frame.EOF()).construct();
        mediaSource.start();

        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoder decoder = create.videoDecoder().with(mediaCodec).construct();
        decoder.start();
        decoder.fillCommandQueues();

        new EofCommandHandler(mediaSource, decoder, decoder).handle();

        verify(mediaCodec, atLeastOnce()).queueInputBuffer(anyInt(), anyInt(), anyInt(), anyLong(), eq(IMediaCodec.BUFFER_FLAG_END_OF_STREAM));
    }

    @Test
    public void withNullFrame_decoderRestoresHasDataCommand() {
        VideoDecoder decoder = create.videoDecoder().construct();
        IFrameAllocator frameAllocator = mock(IFrameAllocator.class);
        when(frameAllocator.findFreeFrame()).thenReturn(null);

        new EofCommandHandler(mediaSource, decoder, frameAllocator).handle();

        assertThat(decoder.getInputCommandQueue()).contains(Command.NeedData, 0);
    }


    private VideoDecoder createDecoder() {
        VideoDecoder decoder = create.videoDecoder().construct();
        decoder.start();
        return decoder;
    }
}
