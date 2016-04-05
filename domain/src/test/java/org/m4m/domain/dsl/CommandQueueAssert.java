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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CommandQueueAssert {
    private final CommandQueue queue;

    public CommandQueueAssert(CommandQueue queue) {
        this.queue = queue;
    }

    public void contains(Command command, int trackId) {
        assertThat(queue, hasItem(new Pair<Command, Integer>(command, trackId)));
    }

    public void equalsTo(Pair<Command, Integer>... expectedCommands) {
        List<Pair<Command, Integer>> actualCommands = new ArrayList<Pair<Command, Integer>>();
        for (Pair<Command, Integer> actualCommand : queue) {
            actualCommands.add(actualCommand);
        }
        assertThat(actualCommands, is(equalTo(Arrays.asList(expectedCommands))));
    }

    public void equalsTo(Command... expectedCommands) {
        List<Command> actualCommands = new ArrayList<Command>();
        for (Pair<Command, Integer> actualCommand : queue) {
            actualCommands.add(actualCommand.left);
        }
        assertThat(actualCommands, is(equalTo(Arrays.asList(expectedCommands))));
    }

    public void isEmpty() {
        assertThat(queue, emptyIterable());
    }
}
