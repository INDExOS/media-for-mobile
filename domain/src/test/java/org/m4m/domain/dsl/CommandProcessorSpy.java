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

import org.m4m.IProgressListener;
import org.m4m.domain.Command;
import org.m4m.domain.CommandHandlerFactory;
import org.m4m.domain.CommandProcessor;
import org.m4m.domain.IInput;
import org.m4m.domain.IOutputRaw;
import org.m4m.domain.OutputInputPair;
import org.m4m.domain.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class CommandProcessorSpy extends CommandProcessor {
    private Queue<LogEntry> monitored = new LinkedList<LogEntry>();
    private LogEntry lastLogEntry;

    public CommandProcessorSpy(IProgressListener progressListener) {
        super(progressListener);
    }

    @Override
    public void add(OutputInputPair pair) {
        super.add(pair);
    }

    public void logPair(IOutputRaw output, IInput input) {
        if (lastLogEntry != null && output == lastLogEntry.output && input == lastLogEntry.input) return;

        lastLogEntry = new LogEntry(output, input);
        monitored.add(lastLogEntry);
    }

    public LogEntry poll() {
        return monitored.poll();
    }

    @Override
    protected void process(Pair<Command, Integer> outputCommand, Pair<Command, Integer> inputCommand, CommandHandlerFactory commandHandlerFactory) {
        super.process(outputCommand, inputCommand, commandHandlerFactory);
        if (lastLogEntry != null) {
            lastLogEntry.commands.add(new Pair<Pair<Command, Integer>, Pair<Command, Integer>>(outputCommand, inputCommand));
        }
    }

    public class LogEntry {
        public IOutputRaw output;
        public IInput input;
        public Queue<Pair<Pair<Command, Integer>, Pair<Command, Integer>>> commands = new LinkedList<Pair<Pair<Command, Integer>, Pair<Command, Integer>>>();

        private LogEntry(IOutputRaw output, IInput input) {
            this.output = output;
            this.input = input;
        }
    }
}


