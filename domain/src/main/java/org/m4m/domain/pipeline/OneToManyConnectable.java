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

package org.m4m.domain.pipeline;

import org.m4m.domain.IInputRaw;
import org.m4m.domain.IOutputRaw;
import org.m4m.domain.IsConnectable;

import java.util.Collection;
import java.util.LinkedList;

class OneToManyConnectable implements IsConnectable {
    Class outType;
    ManyTypes inTypes;

    public OneToManyConnectable(Class outType, ManyTypes inTypes) {
        this.outType = outType;
        this.inTypes = inTypes;
    }

    public static OneToManyConnectable OneToManyConnection(Class outType, ManyTypes inTypes) {
        return new OneToManyConnectable(outType, inTypes);
    }

    public static OneToManyConnectable OneToManyConnection(Class outType, Class inType) {
        return new OneToManyConnectable(outType, new ManyTypes(inType));
    }


    @Override
    public boolean isConnectable(IOutputRaw output, Collection<IInputRaw> inputs) {
        if (!outType.isInstance(output)) {
            return false;
        }
        for (IInputRaw input : inputs) {
            boolean instanceDetected = false;
            for (Class mInType : inTypes.getTypes()) {
                if (mInType.isInstance(input)) {
                    instanceDetected = true;
                    break;
                }
            }
            if (!instanceDetected) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isConnectable(Collection<IOutputRaw> output, IInputRaw input) {
        if (output.size() != 1) {
            return false;
        }
        LinkedList<IInputRaw> c = new LinkedList();
        c.add(input);
        return isConnectable(output.iterator().next(), c);
    }
}
