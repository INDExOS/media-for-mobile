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

import org.m4m.IVideoEffect;
import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class VideoEffectorTest extends TestBase {
    VideoEffector effector;

    @Before
    public void Setup() {
        effector = new VideoEffector(create.mediaCodec().construct(), new AndroidMediaObjectFactoryFake(null));
    }

    @Test
    public void apply1EffectInValidInterval() {
        IVideoEffect mockEffect = mock(IVideoEffect.class);
        ISurface mockSurface = mock(ISurface.class);
        effector.setOutputSurface(mockSurface);
        effector.start();

        effector.getVideoEffects().add(mockEffect);
        when(mockEffect.getSegment()).thenReturn(new FileSegment(10l, 20l));


        effector.push(create.frame().withTimeStamp(5).construct());
        effector.push(create.frame().withTimeStamp(10).construct());
        effector.push(create.frame().withTimeStamp(15).construct());
        effector.push(create.frame().withTimeStamp(20).construct());
        effector.push(create.frame().withTimeStamp(25).construct());

        verify(mockEffect, never()).applyEffect(anyInt(), eq(5l), any(float[].class));
        verify(mockEffect, never()).applyEffect(anyInt(), eq(25l), any(float[].class));
        verify(mockEffect).applyEffect(anyInt(), eq(10l), any(float[].class));
        verify(mockEffect).applyEffect(anyInt(), eq(15l), any(float[].class));
        verify(mockEffect).applyEffect(anyInt(), eq(20l), any(float[].class));
    }

    @Test
    public void apply2EffectsInValidInterval() {
        IVideoEffect mockEffect = mock(IVideoEffect.class);
        IVideoEffect mockEffect2 = mock(IVideoEffect.class);
        ISurface mockSurface = mock(ISurface.class);
        effector.setOutputSurface(mockSurface);
        effector.start();

        effector.getVideoEffects().add(mockEffect);
        effector.getVideoEffects().add(mockEffect2);
        when(mockEffect.getSegment()).thenReturn(new FileSegment(10l, 15l));
        when(mockEffect2.getSegment()).thenReturn(new FileSegment(20l, 25l));


        effector.push(create.frame().withTimeStamp(5).construct());
        effector.push(create.frame().withTimeStamp(10).construct());
        effector.push(create.frame().withTimeStamp(15).construct());
        effector.push(create.frame().withTimeStamp(20).construct());
        effector.push(create.frame().withTimeStamp(25).construct());
        effector.push(create.frame().withTimeStamp(30).construct());

        verify(mockEffect, never()).applyEffect(anyInt(), eq(5l), any(float[].class));
        verify(mockEffect, never()).applyEffect(anyInt(), eq(20l), any(float[].class));
        verify(mockEffect, never()).applyEffect(anyInt(), eq(25l), any(float[].class));
        verify(mockEffect, never()).applyEffect(anyInt(), eq(30l), any(float[].class));
        verify(mockEffect).applyEffect(anyInt(), eq(10l), any(float[].class));
        verify(mockEffect).applyEffect(anyInt(), eq(15l), any(float[].class));

        verify(mockEffect2, never()).applyEffect(anyInt(), eq(5l), any(float[].class));
        verify(mockEffect2, never()).applyEffect(anyInt(), eq(10l), any(float[].class));
        verify(mockEffect2, never()).applyEffect(anyInt(), eq(15l), any(float[].class));
        verify(mockEffect2, never()).applyEffect(anyInt(), eq(30l), any(float[].class));
        verify(mockEffect2).applyEffect(anyInt(), eq(20l), any(float[].class));
        verify(mockEffect2).applyEffect(anyInt(), eq(25l), any(float[].class));
    }

    @Test
    public void drain_putsEofCommand() {
        VideoEffector videoEffector = create.videoEffector().construct();

        videoEffector.drain(0);

        assertThat(videoEffector.getOutputCommandQueue()).equalsTo(Command.EndOfFile);
    }

    @Test
    public void getFrame_fromDrainingVideoEffector() {
        VideoEffector videoEffector = create.videoEffector().construct();

        videoEffector.drain(0);
        Frame frame = videoEffector.getFrame();

        assertEquals(null, frame.getByteBuffer());
        assertEquals(1, frame.getLength());
        assertEquals(1, frame.getSampleTime());
        assertEquals(0, frame.getBufferIndex());
        assertEquals(0, frame.getFlags());
        assertEquals(0, frame.getTrackId());
    }

    @Test
    public void pushEof_NotDrains() {
        ISurface mockSurface = mock(ISurface.class);
        VideoEffector videoEffector = create.videoEffector().construct();
        videoEffector.setOutputSurface(mockSurface);
        videoEffector.start();

        videoEffector.push(Frame.EOF());

        assertNotEquals(videoEffector.getOutputCommandQueue(), Command.EndOfFile);
    }

    @Test
    public void useDefaultEglContext_WhenNoPreview() {
        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        IEglContext currentEglContext = mock(IEglContext.class);
        factory.withCurrentEglContext(currentEglContext);

        VideoEffector videoEffector = create.videoEffector().withFactory(factory).construct();

        final IEglContext[] actualEglContext = {null};
        ISurfaceListener listener = new ISurfaceListener() {
            @Override
            public void onSurfaceAvailable(IEglContext eglContext) {
                actualEglContext[0] = eglContext;
            }
        };
        videoEffector.onSurfaceAvailable(listener);

        assertSame(currentEglContext, actualEglContext[0]);
    }

    @Test
    public void isLastFile_shouldReturnFalse() {
        VideoEffector videoEffector = create.videoEffector().construct();
        boolean shouldBeFalse = videoEffector.isLastFile();

        assertFalse(shouldBeFalse);
    }

    @Test(expected = RuntimeException.class)
    public void start_nullEncoderSurfaceException() {
        VideoEffector videoEffector = create.videoEffector().construct();
        videoEffector.start();
    }

    @Test(expected = RuntimeException.class)
    public void start_nullInternalSurfaceException() {
        VideoEffector videoEffector = create.videoEffector().construct();
        videoEffector.getSurface();
    }

}
