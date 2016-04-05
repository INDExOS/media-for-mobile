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
package org.m4m.domain.dsl;

import org.m4m.IVideoEffect;
import org.m4m.domain.Frame;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class VideoEffectAssert {
    private IVideoEffect videoEffect;

    public VideoEffectAssert(IVideoEffect videoEffect) {this.videoEffect = videoEffect;}

    public void received(Frame frame) {
        verify(videoEffect, atLeastOnce()).applyEffect(anyInt(), eq(frame.getSampleTime()), any(float[].class));
    }

    public void receivedDuring(Frame frame, int milis) {
        verify(videoEffect, timeout(milis).atLeastOnce()).applyEffect(anyInt(), eq(frame.getSampleTime()), any(float[].class));
    }
}
