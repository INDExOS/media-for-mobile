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

import org.m4m.domain.Command;
import org.m4m.domain.CommandQueue;
import org.m4m.domain.Pair;

import static org.m4m.domain.Command.EndOfFile;

public class InfiniteCommandQueue extends CommandQueue {
    private Command infiniteCommand;

    InfiniteCommandQueue(Command infiniteCommand) {
        this.infiniteCommand = infiniteCommand;
    }

    @Override
    public void queue(Command command, Integer trackId) {
        if (command == EndOfFile) {
            super.clear();
        }
        super.queue(command, trackId);
    }

    @Override
    public Pair<Command, Integer> dequeue() {
        if (dequeueLastCommand()) {
            if (EndOfFile != super.first().left) {
                queue(infiniteCommand, 0);
            }
        }
        return super.dequeue();
    }

    private boolean dequeueLastCommand() {
        return queue.size() == 1;
    }
}

