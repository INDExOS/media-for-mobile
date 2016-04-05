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

import static org.junit.Assert.assertEquals;

public class CameraSourceTest extends TestBase {
    @Test
    public void stop_putsEofCommand() {
        CameraSource cameraSource = new CameraSource();

        cameraSource.start();
        cameraSource.stop();

        assertThat(cameraSource.getOutputCommandQueue()).equalsTo(Command.EndOfFile);
    }

    @Test
    public void stop_handlesMultipleStopsCorrectly() {
        CameraSource cameraSource = new CameraSource();

        cameraSource.start();
        cameraSource.stop();
        cameraSource.stop();

        assertThat(cameraSource.getOutputCommandQueue()).equalsTo(Command.EndOfFile);
    }

    @Test
    public void start_putsHasDataCommand() {
        CameraSource cameraSource = new CameraSource();

        cameraSource.start();

        assertThat(cameraSource.getOutputCommandQueue()).isEmpty();

    }

    @Test
    public void start_resetsStopFlag() {
        CameraSource cameraSource = new CameraSource();

        assertEquals(cameraSource.isStopped(), true);
        cameraSource.start();
        assertEquals(cameraSource.isStopped(), false);
        cameraSource.stop();
        assertEquals(cameraSource.isStopped(), true);
        cameraSource.start();
        assertEquals(cameraSource.isStopped(), false);
    }
}
