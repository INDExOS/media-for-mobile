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

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IsPairQueueSpecificationTest extends TestBase {
    @Test
    public void matchQueuesContainingHasDataAndNeedData() {
        CommandQueue sourceQueue = new CommandQueue();
        sourceQueue.queue(Command.HasData, 0);
        CommandQueue targetQueue = new CommandQueue();
        targetQueue.queue(Command.NeedData, 0);

        assertTrue(createSpecification().satisfiedBy(sourceQueue, targetQueue));
    }

    @Test
    public void specificationDoesNotChangeQueues() {
        CommandQueue sourceQueue = new CommandQueue();
        sourceQueue.queue(Command.HasData, 0);
        CommandQueue targetQueue = new CommandQueue();
        targetQueue.queue(Command.NeedData, 0);

        createSpecification().satisfiedBy(sourceQueue, targetQueue);

        assertThat(sourceQueue).contains(Command.HasData, 0);
        assertThat(targetQueue).contains(Command.NeedData, 0);
    }

    @Test
    public void notMatchEmptySourceQueue() {
        CommandQueue sourceQueue = new CommandQueue();
        CommandQueue targetQueue = new CommandQueue();
        targetQueue.queue(Command.NeedData, 0);

        assertFalse(createSpecification().satisfiedBy(sourceQueue, targetQueue));
    }


    @Test
    public void notMatchEmptyTargetQueue() {
        CommandQueue sourceQueue = new CommandQueue();
        sourceQueue.queue(Command.HasData, 0);
        CommandQueue targetQueue = new CommandQueue();

        assertFalse(createSpecification().satisfiedBy(sourceQueue, targetQueue));
    }

    @Test
    public void notMatchEmptyBothSourceAndTargetQueue() {
        CommandQueue sourceQueue = new CommandQueue();
        CommandQueue targetQueue = new CommandQueue();

        assertFalse(createSpecification().satisfiedBy(sourceQueue, targetQueue));
    }

    @Test
    public void notMatchWhenSourceHasNoDataToFeedTarget() {
        CommandQueue sourceQueue = new CommandQueue();
        sourceQueue.queue(Command.NeedData, 0);
        CommandQueue targetQueue = new CommandQueue();
        targetQueue.queue(Command.NeedData, 0);

        assertFalse(createSpecification().satisfiedBy(sourceQueue, targetQueue));
    }

    @Test
    public void notMatchWhenSourceAndTargetAreNotHungry() {
        CommandQueue sourceQueue = new CommandQueue();
        sourceQueue.queue(Command.HasData, 0);
        CommandQueue targetQueue = new CommandQueue();
        targetQueue.queue(Command.HasData, 0);

        assertFalse(createSpecification().satisfiedBy(sourceQueue, targetQueue));
    }

    private PairQueueSpecification createSpecification() {
        ArrayList<Pair<Command, Command>> matchingCommands = new ArrayList<Pair<Command, Command>>();
        matchingCommands.add(new Pair<Command, Command>(Command.HasData, Command.NeedData));
        return new PairQueueSpecification(matchingCommands);
    }
}
