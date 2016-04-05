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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IsPairCommandSpecificationTest extends TestBase {
    @Test
    public void matchWhenSourceHasDataAndTargetNeedsData() {
        PairCommandSpecification specification = createSpecification();

        assertTrue(specification.satisfiedBy(Command.HasData, Command.NeedData));
    }

    @Test
    public void notMatchWhenSourceHasDataAndTargetHasData() {
        PairCommandSpecification specification = createSpecification();
        assertFalse(specification.satisfiedBy(Command.HasData, Command.HasData));
    }

    @Test
    public void matchWhenSourceChangedFormatAndTargetNeedsFormat() {
        PairCommandSpecification specification = createSpecification();
        assertTrue(specification.satisfiedBy(Command.OutputFormatChanged, Command.NeedInputFormat));
    }

    @Test
    public void matchWhenSourceChangedFormatAndTargetNeedsData() {
        PairCommandSpecification specification = createSpecification();
        assertTrue(specification.satisfiedBy(Command.OutputFormatChanged, Command.NeedData));
    }

    @Test
    public void matchWhenSourceHasDataAndTargetNeedsDataForSameTrack() {
        PairCommandSpecification specification = createSpecification();

        assertTrue(specification.satisfiedBy(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0)));
    }

    @Test
    public void notMatchWhenSourceHasDataAndTargetNeedsDataForDifferentTrack() {
        PairCommandSpecification specification = createSpecification();

        assertFalse(specification.satisfiedBy(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 1)));
    }

    @Test
    public void notMatchWhenSourceHasDataAndTargetHasDataForSameTrack() {
        PairCommandSpecification specification = createSpecification();
        assertFalse(specification.satisfiedBy(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.HasData, 0)));
    }

    private PairCommandSpecification createSpecification() {
        return new PairCommandSpecification(new MatchingCommands());
    }
}
