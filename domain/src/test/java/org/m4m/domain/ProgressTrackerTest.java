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

import static junit.framework.TestCase.assertEquals;

public class ProgressTrackerTest extends TestBase {
    @Test
    public void defaultProgress_0() {
        ProgressTracker progressTracker = new ProgressTracker();
        progressTracker.setFinish(1);

        assertEquals(0f, progressTracker.getProgress(), 0.1);
    }

    @Test
    public void progress_0_1_afterOneFrameOutOfTen() {
        ProgressTracker progressTracker = new ProgressTracker();
        progressTracker.setFinish(1000);

        progressTracker.track(100);

        assertEquals(0.1f, progressTracker.getProgress(), 0.01);
    }

    @Test
    public void progress_0_5_afterFiveFramesOutOfTen() {
        ProgressTracker progressTracker = new ProgressTracker();
        progressTracker.setFinish(1000);

        progressTracker.track(100);
        progressTracker.track(200);
        progressTracker.track(300);
        progressTracker.track(400);
        progressTracker.track(500);

        assertEquals(0.5f, progressTracker.getProgress(), 0.01);
    }

    @Test
    public void progressMonotonouslyIncreasing() {
        ProgressTracker progressTracker = new ProgressTracker();

        progressTracker.setFinish(1000);
        progressTracker.track(500);
        progressTracker.track(400);

        assertEquals(0.5f, progressTracker.getProgress(), 0.01);
    }
}
