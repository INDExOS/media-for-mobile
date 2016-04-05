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
import org.m4m.domain.Command;
import org.m4m.domain.Pair;

import static org.junit.Assert.assertEquals;

public class PairTest {
    @Test
    public void pairWithLeftNullAreEqual() {
        Pair<Command, Command> pair1 = new Pair<Command, Command>(null, Command.HasData);
        Pair<Command, Command> pair2 = new Pair<Command, Command>(null, Command.HasData);

        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    public void pairWithRightNullAreEqual() {
        Pair<Command, Command> pair1 = new Pair<Command, Command>(Command.HasData, null);
        Pair<Command, Command> pair2 = new Pair<Command, Command>(Command.HasData, null);

        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    public void toString_NullWhenLeftIsNull() {
        Pair<Command, Integer> pair = new Pair<Command, Integer>(null, 0);
        assertEquals("(NULL, 0)", pair.toString());
    }

    @Test
    public void toString_NullWhenRightIsNull() {
        Pair<Command, Integer> pair = new Pair<Command, Integer>(Command.HasData, null);
        assertEquals("(HasData, NULL)", pair.toString());
    }

    @Test
    public void toString_NullWhenBothLeftAndRightAreNulls() {
        Pair<Command, Integer> pair = new Pair<Command, Integer>(null, null);
        assertEquals("(NULL, NULL)", pair.toString());
    }
}
