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

package org.m4m.domain.mediaComposer;

import org.m4m.IVideoEffect;
import org.m4m.MediaComposer;
import org.junit.Test;
import org.m4m.domain.FileSegment;
import org.m4m.domain.IEglContext;
import org.m4m.domain.IFrameBuffer;
import org.m4m.domain.IPreview;
import org.m4m.domain.ISurfaceTexture;
import org.m4m.domain.PreviewContext;
import org.m4m.domain.Resolution;
import org.m4m.domain.VideoEffector;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

public class WhenApplyVideoEffect extends MediaComposerTest {
    @Test
    public void withoutPreview_connectEffectorToEncoder() throws InterruptedException {
        IVideoEffect videoEffect = mock(IVideoEffect.class);
        MediaComposer mediaComposer = create.mediaComposer()
                .withVideoEffect(videoEffect)
                .with(a.frame().construct())
                .with(progressListener)
                .construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        //Second time for EOF frame
        verify(videoEffect, times(2)).applyEffect(anyInt(), anyLong(), any(float[].class));
    }

    @Test
    public void withPreview_UseFrameBuffer() throws InterruptedException {
        IVideoEffect videoEffect = mock(IVideoEffect.class);

        IFrameBuffer frameBuffer = mock(IFrameBuffer.class);
        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        VideoEffector effector = create.videoEffector().withFactory(factory).construct();

        IPreview preview = mock(IPreview.class);
        PreviewContext context = new PreviewContext(mock(ISurfaceTexture.class), 0, mock(IEglContext.class));
        when(preview.getSharedContext()).thenReturn(context);
        effector.enablePreview(preview);

        MediaComposer mediaComposer = create.mediaComposer()
                .withFactory(factory)
                .withVideoEffect(videoEffect)
                .with(a.frame().construct())
                .withFrameBuffer(frameBuffer)
                .with(progressListener)
                .withVideoEffector(effector)
                .construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        verify(frameBuffer).setResolution(any(Resolution.class));
        //Second time for EOF frame
        verify(frameBuffer, times(2)).bind();
        verify(frameBuffer, times(2)).unbind();
    }

    @Test
    public void withPreview_UseFrameBuffer_NoEffectApplied_At_Current_Time() throws InterruptedException {
        IVideoEffect videoEffect = mock(IVideoEffect.class);
        when(videoEffect.getSegment()).thenReturn(new FileSegment(100L, 500L));

        IFrameBuffer frameBuffer = mock(IFrameBuffer.class);
        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        VideoEffector effector = new VideoEffector(create.mediaCodec().construct(), factory);

        IPreview preview = mock(IPreview.class);
        PreviewContext context = new PreviewContext(mock(ISurfaceTexture.class), 0, mock(IEglContext.class));
        when(preview.getSharedContext()).thenReturn(context);
        effector.enablePreview(preview);

        MediaComposer mediaComposer = create.mediaComposer()
                .withFactory(factory)
                .withVideoEffectAndSegment(videoEffect)
                .with(a.frame().construct())
                .withFrameBuffer(frameBuffer)
                .with(progressListener)
                .withVideoEffector(effector)
                .construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        verify(frameBuffer).setResolution(any(Resolution.class));
        //Second time for EOF frame
        verify(frameBuffer, times(2)).bind();
        verify(frameBuffer, times(2)).unbind();
    }

    @Test
    public void withoutPreview_NotUseFrameBuffer() throws InterruptedException {
        IVideoEffect videoEffect = mock(IVideoEffect.class);

        IFrameBuffer frameBuffer = mock(IFrameBuffer.class);

        MediaComposer mediaComposer = create.mediaComposer()
                .withVideoEffect(videoEffect)
                .with(a.frame().construct())
                .withFrameBuffer(frameBuffer)
                .with(progressListener)
                .construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        verifyZeroInteractions(frameBuffer);
    }

    @Test
    public void withoutPreview_getInputResolutionFromSource() throws InterruptedException {
        IVideoEffect videoEffect = mock(IVideoEffect.class);
        MediaComposer mediaComposer = create.mediaComposer()
                .withInputResolution(1024, 768)
                .withVideoEffect(videoEffect)
                .with(a.frame().construct())
                .with(progressListener)
                .construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        verify(videoEffect).setInputResolution(eq(new Resolution(1024, 768)));
    }
}


