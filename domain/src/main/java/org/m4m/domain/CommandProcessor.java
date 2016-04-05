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

public class CommandProcessor implements ICommandProcessor {
    //Logger log = Logger.getLogger(getClass().getSimpleName());

    private final ArrayList<OutputInputPair> pairs = new ArrayList<OutputInputPair>();
    private final PairQueueSpecification pairQueueSpecification = new PairQueueSpecification(new MatchingCommands());
    private final IProgressListener progressListener;
    private volatile boolean isPaused = false;

    private static final MatchingCommands matchingCommands = new MatchingCommands();
    private boolean stopped = false;

    public CommandProcessor(IProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public void add(OutputInputPair pair) {
        pairs.add(pair);
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public void process() {
        for (OutputInputPair pair : pairs) {
            pair.output.fillCommandQueues();
            pair.input.fillCommandQueues();
        }

        while (!stopped) {
            for (OutputInputPair pair : pairs) {
                processCommandPairs(pair);
            }
        }
        //log.info("No pairs to process, exit.");
    }

    private void processCommandPairs(OutputInputPair pair) {
        pair.output.fillCommandQueues();
        pair.input.fillCommandQueues();
        CommandQueue outputCommandQueue = pair.output.getOutputCommandQueue();
        CommandQueue inputCommandQueue = pair.input.getInputCommandQueue();

        while (pairQueueSpecification.satisfiedBy(outputCommandQueue, inputCommandQueue)) {
            checkIfPaused();

            Pair<Command, Integer> outputCommand = outputCommandQueue.first();
            Pair<Command, Integer> inputCommand = inputCommandQueue.first();

            if(outputCommand == null || inputCommand == null) continue;

            if (inputCommand.left == Command.NextPair) {
                inputCommandQueue.dequeue();
                break;
            }
            if (outputCommand.left == Command.NextPair) {
                outputCommandQueue.dequeue();
                break;
            }
            process(outputCommandQueue, inputCommandQueue, pair.commandHandlerFactory);
        }
    }

    private void process(CommandQueue outputCommandQueue, CommandQueue inputCommandQueue, CommandHandlerFactory commandHandlerFactory) {
        Pair<Pair<Command, Integer>, Pair<Command, Integer>> matchingCommands = dequeMatchingCommands(outputCommandQueue, inputCommandQueue);
        Pair<Command, Integer> outputCommand = matchingCommands.left;
        Pair<Command, Integer> inputCommand = matchingCommands.right;
        process(outputCommand, inputCommand, commandHandlerFactory);
    }

    private Pair<Pair<Command, Integer>, Pair<Command, Integer>> dequeMatchingCommands(CommandQueue outputCommandQueue, CommandQueue inputCommandQueue) {
        Pair<Command, Integer> outputCommand;
        Pair<Command, Integer> inputCommand;
        for (Pair<Command, Command> matchingCommand : matchingCommands) {

            outputCommand = outputCommandQueue.first();
            inputCommand = inputCommandQueue.first();

            if(outputCommand == null || inputCommand == null) continue;

            boolean match = (matchingCommand.left == null || matchingCommand.left == outputCommand.left) &&
                            (matchingCommand.right == null || matchingCommand.right == inputCommand.left) &&
                            (outputCommand.right == inputCommand.right);
            if (match) {
                if (matchingCommand.left != null) outputCommand = outputCommandQueue.dequeue();
                if (matchingCommand.right != null) inputCommand = inputCommandQueue.dequeue();
                return new Pair<Pair<Command, Integer>, Pair<Command, Integer>>(outputCommand, inputCommand);
            }
        }
        // Cannot get here, because PairCommandSpecification.satisfiedBy handles the case
        throw new UnsupportedOperationException("Pair (" + outputCommandQueue.first() + ", " + inputCommandQueue.first() + ") does not match.");
    }

    protected void process(Pair<Command, Integer> outputCommand, Pair<Command, Integer> inputCommand, CommandHandlerFactory commandHandlerFactory) {
        ICommandHandler commandHandler = commandHandlerFactory.create(outputCommand, inputCommand, progressListener);
        commandHandler.handle();
    }

    private synchronized void checkIfPaused() {
        while (isPaused) try {
            progressListener.onMediaPause();
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        //log.info("### Pause ###");
        isPaused = true;
    }

    public synchronized void resume() {
        //log.info("### Resume ###");
        isPaused = false;
        notifyAll();
    }
}
