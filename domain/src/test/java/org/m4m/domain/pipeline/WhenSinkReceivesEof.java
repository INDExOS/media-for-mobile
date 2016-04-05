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
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.Render;
import org.m4m.domain.TestBase;

import java.nio.ByteBuffer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhenSinkReceivesEof extends TestBase {
    private Render render;
    private IMediaMuxer mediaMuxer;

    @Before
    public void getRender() {
        mediaMuxer = create.mediaMuxer().construct();
        render = create.render().with(mediaMuxer).construct();

        render.setMediaFormat(create.videoFormat().construct());
        render.configure();
        render.start();
    }

    @Test
    public void mediaMuxerReceivedEOSFlag() {
        render.push(Frame.EOF());

        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = IMediaCodec.BUFFER_FLAG_END_OF_STREAM;
        bufferInfo.size = Frame.EOF().getLength();
        verify(mediaMuxer).writeSampleData(anyInt(), any(ByteBuffer.class), eq(bufferInfo));
    }

    @Test
    public void mediaMuxerStopped() {
        IPluginOutput output = mock(IPluginOutput.class);
        when(output.getFrame()).thenReturn(Frame.EOF());

        new PullDataCommandHandler(output, render).handle();

        verify(mediaMuxer).stop();
    }

    @Test
    public void mediaMuxerReleased() {
        IPluginOutput output = mock(IPluginOutput.class);
        when(output.getFrame()).thenReturn(Frame.EOF());

        new PullDataCommandHandler(output, render).handle();

        verify(mediaMuxer).release();
    }
}
