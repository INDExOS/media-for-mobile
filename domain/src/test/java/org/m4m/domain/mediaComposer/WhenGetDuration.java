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

import org.m4m.domain.Pair;
import org.junit.Test;

import javax.naming.OperationNotSupportedException;

import static org.junit.Assert.assertEquals;

public class WhenGetDuration extends MediaComposerTest {
    @Test
    public void canGetDurationOfSingleFile() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withDuration(123)
            .construct();

        assertEquals(123, mediaComposer.getDurationInMicroSec());
    }

    @Test
    public void canGetDurationOfFileWithSegment() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withDuration(1000)
            .construct();

        mediaComposer.getSourceFiles().get(0).addSegment(new Pair<Long, Long>(0L, 150L));

        assertEquals(150, mediaComposer.getDurationInMicroSec());
    }

    @Test
    public void canGetDurationOfFileWithTwoSegments() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withDuration(1000)
            .construct();

        mediaComposer.getSourceFiles().get(0).addSegment(new Pair<Long, Long>(0L, 150L));
        mediaComposer.getSourceFiles().get(0).addSegment(new Pair<Long, Long>(500L, 600L));

        assertEquals(150 + 100, mediaComposer.getDurationInMicroSec());
    }
}
