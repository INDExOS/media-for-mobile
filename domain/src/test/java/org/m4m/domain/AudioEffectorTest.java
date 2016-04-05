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

import org.m4m.IAudioEffect;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AudioEffectorTest extends TestBase {
    AudioEffector effector;

    @Before
    public void Setup() {
        effector = new AudioEffector(create.mediaCodec().construct());
    }

    @Test
    public void apply1EffectInValidInterval() {
        IAudioEffect mockEffect = mock(IAudioEffect.class);
        ISurface mockSurface = mock(ISurface.class);
        ByteBuffer input = mock(ByteBuffer.class);
        effector.setOutputSurface(mockSurface);
        effector.start();

        effector.getAudioEffects().add(mockEffect);
        when(mockEffect.getSegment()).thenReturn(new Pair(10l, 20l));

        effector.push(create.frame().withTimeStamp(5).construct());
        effector.push(create.frame().withTimeStamp(10).construct());
        effector.push(create.frame().withTimeStamp(15).construct());
        effector.push(create.frame().withTimeStamp(20).construct());
        effector.push(create.frame().withTimeStamp(25).construct());

        verify(mockEffect, never()).applyEffect(eq(input), eq(5l));
        verify(mockEffect, never()).applyEffect(eq(input), eq(25l));
        verify(mockEffect).applyEffect(any(ByteBuffer.class), eq(10l));
        verify(mockEffect).applyEffect(any(ByteBuffer.class), eq(15l));
        verify(mockEffect).applyEffect(any(ByteBuffer.class), eq(20l));
    }

    @Test
    public void receivedFrameEqualsToFoundFrame() {
        Frame frame = effector.findFreeFrame();

        assertEquals(frame, effector.getFrame());
    }

    @Test
    public void getFrameReturnsNullUntilFindFreeFrameIsCalled() {
        assertEquals(null, effector.getFrame());
    }
}