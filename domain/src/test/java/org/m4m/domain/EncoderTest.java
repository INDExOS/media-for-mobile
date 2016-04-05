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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EncoderTest extends TestBase {

    @Test(expected = UnsupportedOperationException.class)
    public void pull_shouldThrowUnsupportedOperationException() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();
        videoEncoder.pull(create.frame().construct());
    }

    @Test
    public void test_setTrackId() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();

        videoEncoder.setTrackId(1);

        assertEquals(1, videoEncoder.getTrackId());
    }

    @Test
    public void test_onSurfaceAvailable() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();
        IOnSurfaceReady listener = mock(IOnSurfaceReady.class);

        videoEncoder.onSurfaceAvailable(listener);

        assertEquals(listener, videoEncoder.listeners.get(0));
    }

    @Test
    public void test_getSurface() {
        VideoEncoder videoEncoder = create.videoEncoder().construct();
        IOnSurfaceReady listener = mock(IOnSurfaceReady.class);
        videoEncoder.onSurfaceAvailable(listener);

        videoEncoder.getSurface();

        verify(listener).onSurfaceReady();
    }
}
