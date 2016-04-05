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

import static org.junit.Assert.*;

public class FrameBufferTest extends TestBase {
    @Test
    public void configureSingleTrack_MakesAllTracksConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(1);

        frameBuffer.configure(0);

        assertTrue(frameBuffer.areAllTracksConfigured());
    }

    @Test
    public void configureOnlyFirstTrack_MakesAllTracksNotConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(2);

        frameBuffer.configure(0);

        assertFalse(frameBuffer.areAllTracksConfigured());
    }

    @Test
    public void configureBothTracks_MakesAllTracksConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(2);

        frameBuffer.configure(0);
        frameBuffer.configure(1);

        assertTrue(frameBuffer.areAllTracksConfigured());
    }

    @Test
    public void configureTheSameTrackTwice_DoesNotMakesAllTracksConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(2);

        frameBuffer.configure(0);
        frameBuffer.configure(0);

        assertFalse(frameBuffer.areAllTracksConfigured());
    }

    @Test
    public void canNotPull_UntilConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(1);

        assertFalse(frameBuffer.canPull());
    }

    @Test
    public void canPull_AfterBothTracksAreConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(2);

        frameBuffer.configure(0);
        frameBuffer.configure(1);
        frameBuffer.push(create.frame().construct());

        assertTrue(frameBuffer.canPull());
    }

    @Test
    public void canPull_AfterSingleTrackIsConfiguredAndFrameIsPushed() {
        FrameBuffer frameBuffer = new FrameBuffer(1);

        frameBuffer.configure(0);
        frameBuffer.push(create.frame().construct());

        assertTrue(frameBuffer.canPull());
    }

    @Test
    public void canNotPull_UntilFrameIsPushed() {
        FrameBuffer frameBuffer = new FrameBuffer(1);

        frameBuffer.configure(0);

        assertFalse(frameBuffer.canPull());
    }

    @Test
    public void canNotPull_UntilBothTracksAreConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(2);

        frameBuffer.configure(0);
        frameBuffer.push(create.frame().construct());

        assertFalse(frameBuffer.canPull());
    }

    @Test
    public void pull_ReturnsPushedFrame() {
        FrameBuffer frameBuffer = new FrameBuffer(1);

        frameBuffer.configure(0);
        Frame frame = create.frame().construct();
        frameBuffer.push(frame);

        assertEquals(frame, frameBuffer.pull());
    }

    @Test
    public void pull_ReturnsTwoPushedFrames() {
        FrameBuffer frameBuffer = new FrameBuffer(1);

        frameBuffer.configure(0);
        Frame frame1 = create.frame().construct();
        Frame frame2 = create.frame().construct();
        frameBuffer.push(frame1);
        frameBuffer.push(frame2);

        assertEquals(frame1, frameBuffer.pull());
        assertEquals(frame2, frameBuffer.pull());
    }

    @Test
    public void addTrack_ClearsAllTracksConfigured() {
        FrameBuffer frameBuffer = new FrameBuffer(1);
        frameBuffer.configure(0);

        frameBuffer.addTrack();

        assertFalse(frameBuffer.areAllTracksConfigured());
    }
}
