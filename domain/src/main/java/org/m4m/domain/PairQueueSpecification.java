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

import java.util.List;

class PairQueueSpecification implements ISpecification<CommandQueue> {
    private final PairCommandSpecification pairCommandSpecification;

    public PairQueueSpecification(List<Pair<Command, Command>> matchingCommands) {
        pairCommandSpecification = new PairCommandSpecification(matchingCommands);
    }

    @Override
    public boolean satisfiedBy(CommandQueue sourceQueue, CommandQueue targetQueue) {
        Pair<Command, Integer> sourceCommand = sourceQueue.first();
        Pair<Command, Integer> targetCommand = targetQueue.first();
        if (targetCommand == null) return false;
        if (targetCommand.left == Command.NextPair) return true;
        if (sourceCommand == null) return false;
        if (sourceCommand.left == Command.NextPair) return true;
        return pairCommandSpecification.satisfiedBy(sourceCommand, targetCommand);
    }
}
