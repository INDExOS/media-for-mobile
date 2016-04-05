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

class ConnectedNode<T, T1> {
    T node;
    LinkedList<T1> connectedTo = new LinkedList<T1>();

    ConnectedNode(T node) {
        this.node = node;
    }

    public T value() {
        return node;
    }

    public boolean isConnected() {
        return !connectedTo.isEmpty();
    }

    public Collection<T1> getConnector() {
        return connectedTo;
    }

    public void connect(T1 connector) {
        connectedTo.add(connector);
    }

    public void disconnect(T1 connector) {
        connectedTo.remove(connector);
    }

    public boolean isConnectedTo(T1 value) {
        return getConnector().contains(value);
    }
}
