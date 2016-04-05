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

class PairCommandSpecification implements ISpecification<Command> {
    private final List<Pair<Command, Command>> matchingCommands;

    public PairCommandSpecification(List<Pair<Command, Command>> matchingCommands) {
        this.matchingCommands = matchingCommands;
    }


    @Override
    public boolean satisfiedBy(Command source, Command target) {
        if (source == null && target == null) return false;
        for (Pair<Command, Command> matchingCommand : matchingCommands) {
            if (matchingCommand.left != null && matchingCommand.left == source && matchingCommand.right != null && matchingCommand.right == target)
                return true;
            if (matchingCommand.left == null && matchingCommand.right == target) return true;
            if (matchingCommand.right == null && matchingCommand.left == source) return true;
        }
        return false;
    }

    public boolean satisfiedBy(Pair<Command, Integer> sourcePair, Pair<Command, Integer> targetPair) {
        if (sourcePair == null || targetPair == null) return false;

        for (Pair<Command, Command> matchingCommand : matchingCommands) {
            if (matchingCommand.left != null && matchingCommand.left == sourcePair.left
                && matchingCommand.right != null && matchingCommand.right == targetPair.left
                && sourcePair.right == targetPair.right)
                return true;
            if (matchingCommand.left == null && matchingCommand.right == targetPair.left && sourcePair.right == targetPair.right)
                return true;
            if (matchingCommand.right == null && matchingCommand.left == targetPair.left && sourcePair.right == targetPair.right)
                return true;
        }
        return false;
    }
}
