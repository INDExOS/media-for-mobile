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

import java.util.Iterator;
import java.util.LinkedList;


public class CommandQueue implements Iterable<Pair<Command, Integer>> {
    protected LinkedList<Pair<Command, Integer>> queue = new LinkedList<Pair<Command, Integer>>();

    public CommandQueue() {
    }

    public CommandQueue(CommandQueue commandQueue) {
        for (Pair<Command, Integer> pair : commandQueue) {
            queue(pair.left, pair.right);
        }
    }

    public void queue(Command command, Integer trackId) {
        Pair<Command, Integer> pair = new Pair<Command, Integer>(command, trackId);
        queue.add(pair);
    }

    public Pair<Command, Integer> dequeue() {
        return queue.poll();
    }

    @Override
    public Iterator<Pair<Command, Integer>> iterator() {
        return queue.iterator();
    }

    public Pair<Command, Integer> first() {
        if (size() == 0) {
            return null;
        }

        return queue.peek();
    }

    public Pair<Command, Integer> last() {
        if (size() == 0) return null;
        return queue.getLast();
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }
}
