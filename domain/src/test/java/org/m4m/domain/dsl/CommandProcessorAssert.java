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

import org.junit.Assert;
import org.m4m.domain.Command;
import org.m4m.domain.IInput;
import org.m4m.domain.IOutput;
import org.m4m.domain.IOutputRaw;
import org.m4m.domain.Pair;

import static org.junit.Assert.assertNotNull;

public class CommandProcessorAssert {
    private final CommandProcessorSpy commandProcessor;
    private CommandProcessorSpy.LogEntry currentLogEntry;

    public CommandProcessorAssert(CommandProcessorSpy commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public CommandProcessorAssert processed(IOutput expectedOutput, IInput expectedInput) {
        currentLogEntry = commandProcessor.poll();
        assertPairEquals(expectedOutput, expectedInput, currentLogEntry);
        return this;
    }

    public CommandProcessorAssert commands(Command expectedOutputCommand, Command expectedInputCommand) {
        Pair<Pair<Command, Integer>, Pair<Command, Integer>> currentCommandPair = currentLogEntry.commands.poll();
        assertNotNull("(" + expectedOutputCommand + ", " + expectedInputCommand + ") pair was expected but not queued", currentCommandPair);
        Assert.assertEquals(new Pair<Command, Integer>(expectedOutputCommand, 0), currentCommandPair.left);
        Assert.assertEquals(new Pair<Command, Integer>(expectedInputCommand, 0), currentCommandPair.right);
        return this;
    }

    private void assertPairEquals(IOutput expectedOutput, IInput expectedInput, CommandProcessorSpy.LogEntry actual) {
        IOutputRaw actualOutput = actual.output;
        IInput actualInput = actual.input;

        String message = "\nExpected pair: ('" + expectedOutput + "', '" + expectedInput + "')\n" +
            "Actual pair: ('" + actualOutput + "', '" + actualInput + "')\n";
        Assert.assertEquals(message, expectedOutput, actualOutput);
        Assert.assertEquals(message, expectedInput, actualInput);
    }
}
