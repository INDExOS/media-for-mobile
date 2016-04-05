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

import org.m4m.IProgressListener;

import java.util.ArrayList;
import java.util.List;

public class CommandHandlerFactory {
    List<entry> handlerCreators = new ArrayList<entry>();

    public ICommandHandler create(Pair<Command, Integer> lhsCommand, Pair<Command, Integer> rhsCommand, IProgressListener progressListener) {
        IHandlerCreator handlerCreator = findHandlerCreator(lhsCommand, rhsCommand);
        return handlerCreator.create();
    }

    public void register(Pair<Command, Integer> lhsCommand, Pair<Command, Integer> rhsCommand, IHandlerCreator handlerCreator) {
        handlerCreators.add(new entry(lhsCommand, rhsCommand, handlerCreator));
    }

    private IHandlerCreator findHandlerCreator(Pair<Command, Integer> lhsCommand, Pair<Command, Integer> rhsCommand) {
        for (entry handlerCreator : handlerCreators) {
            if (handlerCreator.leftCommand == null && handlerCreator.rightCommand != null &&
                handlerCreator.rightCommand.equals(rhsCommand)) {
                return handlerCreator.handlerCreator;
            }
            if (handlerCreator.rightCommand == null && handlerCreator.leftCommand != null &&
                handlerCreator.leftCommand.equals(lhsCommand)) {
                return handlerCreator.handlerCreator;
            }
            if (handlerCreator.leftCommand != null && handlerCreator.rightCommand != null &&
                handlerCreator.leftCommand.equals(lhsCommand) && handlerCreator.rightCommand.equals(rhsCommand)) {
                return handlerCreator.handlerCreator;
            }
        }
        throw new IllegalArgumentException("Command handler for pair (" + lhsCommand + ", " + rhsCommand + ") not found");
    }

    private class entry {
        public Pair<Command, Integer> leftCommand;
        public Pair<Command, Integer> rightCommand;
        public IHandlerCreator handlerCreator;

        private entry(Pair<Command, Integer> leftCommand, Pair<Command, Integer> rightCommand, IHandlerCreator handlerCreator) {
            this.leftCommand = leftCommand;
            this.rightCommand = rightCommand;
            this.handlerCreator = handlerCreator;
        }
    }
}
