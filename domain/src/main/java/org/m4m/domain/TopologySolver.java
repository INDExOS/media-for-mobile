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

import java.util.Collection;
import java.util.LinkedList;

class TopologySolver {
    public Object getNodes;

    class LeftNode extends ConnectedNode<IOutputRaw, IInputRaw> {
        LeftNode(IOutputRaw node) {
            super(node);
        }
    }

    class RightNode extends ConnectedNode<IInputRaw, IOutputRaw> {
        RightNode(IInputRaw node) {
            super(node);
        }
    }

    private final LinkedList<LeftNode> sources = new LinkedList<LeftNode>();
    private final LinkedList<RightNode> sinks = new LinkedList<RightNode>();
    private final LinkedList<LeftNode> pureSources = new LinkedList<LeftNode>();

    private final LinkedList<IsConnectable> rules = new LinkedList<IsConnectable>();
    private boolean solved;
    private LinkedList<ITopologyTree> trees;

    public void addConnectionRule(IsConnectable rule) {
        assertIsNotSolved();
        rules.add(rule);
    }

    public void add(ITransform transform) {
        assertIsNotSolved();
        sinks.add(new RightNode(transform));
        sources.add(new LeftNode(transform));
    }

    public void add(IOutputRaw source) {
        assertIsNotSolved();
        LeftNode leftNode = new LeftNode(source);
        pureSources.add(leftNode);
        this.sources.add(leftNode);
    }

    public void add(IInputRaw sink) {
        assertIsNotSolved();
        sinks.add(new RightNode(sink));
    }

    public Collection<IOutputRaw> getSources() {
        LinkedList<IOutputRaw> sourceCollection = new LinkedList<IOutputRaw>();
        for (LeftNode source : pureSources) {
            sourceCollection.add(source.value());
        }
        return sourceCollection;
    }

    public Collection<IInputRaw> getSinks() {
        LinkedList<IInputRaw> nodes = new LinkedList();
        for (RightNode source : sinks) {
            nodes.add(source.value());
        }
        return nodes;
    }

    public Collection<Pair<IOutputRaw, IInputRaw>> getConnectionsQueue() {
        resolve();

        LinkedList<Pair<IOutputRaw, IInputRaw>> queue = new LinkedList<Pair<IOutputRaw, IInputRaw>>();

        for (ITopologyTree tree : trees) {
            buildConnectionQueue(tree, queue, true);
        }

        return queue;
    }

    private void buildConnectionQueue(ITopologyTree topologyTree, LinkedList<Pair<IOutputRaw, IInputRaw>> queue, boolean bHead) {
        if (topologyTree == null || !(topologyTree.current() instanceof IOutputRaw)) {
            return;
        }

        IOutputRaw output = (IOutputRaw) topologyTree.current();
        for (Object o : topologyTree.next()) {
            ITopologyTree nextTree = (ITopologyTree) o;
            IInputRaw input = (IInputRaw) nextTree.current();

            if (input.canConnectFirst(output) && (!bHead || output.canConnectFirst(input))) {
                queue.add(new Pair(output, input));
                buildConnectionQueue(nextTree, queue, false);
            } else {
                buildConnectionQueue(nextTree, queue, false);
                queue.add(new Pair(output, input));
            }
        }
    }

    public Collection<ITopologyTree> resolve() throws RuntimeException {
        if (!solved) {
            if (!continueResolve()) {
                throw new IllegalStateException("Cannot resolve");
            }

            trees = new LinkedList<ITopologyTree>();
            for (LeftNode left : pureSources) {
                trees.add(buildTree(left));
            }

            solved = true;
        }
        return trees;
    }

    private void assertIsNotSolved() {
        if (solved) {
            throw new IllegalStateException("cannot modify topology after solving");
        }
    }

    private ITopologyTree buildTree(IInputRaw input) {
        LeftNode lNode = findOutputForTransform(input);
        if (null == lNode) {
            return new TopologyNet(input);
        }

        return buildTree(lNode);
    }

    private ITopologyTree buildTree(LeftNode current) {
        TopologyNet net = new TopologyNet(current.value());

        for (IInputRaw input : current.getConnector()) {

            if (null == net.next()) {
                net.setNext(new LinkedList<ITopologyTree>());
            }
            net.next().add(buildTree(input));
        }

        return net;
    }

    private LeftNode findOutputForTransform(IInputRaw input) {
        if (!(input instanceof ITransform)) {
            return null;
        }

        for (LeftNode output : sources) {
            if (output.value() instanceof ITransform) {
                if (input == output.value()) {
                    return output;
                }
            }
        }

        return null;
    }

    private boolean continueResolve() {
        boolean allConnected = true;
        for (LeftNode source : sources) {
            if (!source.isConnected()) {
                allConnected = false;
            }

            for (RightNode sink : sinks) {
                if (source.isConnectedTo(sink.value())) {
                    continue;
                }
                //connect before rule matching to allow verify all connections
                source.connect(sink.value());
                sink.connect(source.value());
                if (matchConnectionRules(source, sink)) {
                    if (continueResolve()) {
                        return true;
                    }
                }
                sink.disconnect(source.value());
                source.disconnect(sink.value());
            }
        }

        return allConnected;
    }

    private boolean matchConnectionRules(LeftNode output, RightNode sink) {
        for (IsConnectable rule : rules) {
            if (rule.isConnectable(output.value(), output.getConnector()) &&
                rule.isConnectable(sink.getConnector(), sink.value())) {
                return true;
            }
        }
        return false;
    }
}
