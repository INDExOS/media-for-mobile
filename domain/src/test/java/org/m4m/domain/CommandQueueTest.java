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

public class CommandQueueTest extends TestBase {
    @Test
    public void canAddCommand() {
        CommandQueue commandQueue = new CommandQueue();

        commandQueue.queue(Command.HasData, 0);

        assertEquals(new Pair<Command, Integer>(Command.HasData, 0), commandQueue.dequeue());
    }

    @Test
    public void canGetFirstCommand() {
        CommandQueue commandQueue = new CommandQueue();

        commandQueue.queue(Command.HasData, 0);

        assertEquals(new Pair<Command, Integer>(Command.HasData, 0), commandQueue.first());
    }

    @Test
    public void firstCommandIsNull_ForEmptyQueue() {
        CommandQueue commandQueue = new CommandQueue();

        assertNull(commandQueue.first());
    }

    @Test
    public void checkIncorrectCommands() {
        MatchingCommands matchCommand = new MatchingCommands();
        PairCommandSpecification queueSpecification = new PairCommandSpecification(matchCommand);
        CommandQueue outputQueue = new CommandQueue();
        outputQueue.queue(Command.NeedInputFormat, 0);

        CommandQueue inputQueue = new CommandQueue();
        inputQueue.queue(Command.EndOfFile, 0);

        for (int i = 0; i < matchCommand.size(); i++) {
            assertNotEquals(outputQueue.first().left, matchCommand.get(i).left);
        }
        for (int i = 0; i < matchCommand.size(); i++) {
            assertNotEquals(inputQueue.first().left, matchCommand.get(i).right);
        }

        assertEquals(queueSpecification.satisfiedBy(outputQueue.first(), inputQueue.first()),false);
    }
}
