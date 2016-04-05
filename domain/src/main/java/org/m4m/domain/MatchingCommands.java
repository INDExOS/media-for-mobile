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

import java.util.ArrayList;

public final class MatchingCommands extends ArrayList<Pair<Command, Command>> {
    public MatchingCommands() {
        super();
        add(new Pair<Command, Command>(Command.HasData, Command.NeedData));
        add(new Pair<Command, Command>(Command.HasData, Command.NeedInputFormat));
        add(new Pair<Command, Command>(Command.OutputFormatChanged, Command.NeedInputFormat));
        add(new Pair<Command, Command>(Command.OutputFormatChanged, Command.NeedData));
        add(new Pair<Command, Command>(Command.EndOfFile, Command.NeedInputFormat));
        add(new Pair<Command, Command>(Command.EndOfFile, Command.NeedData));
    }
}
