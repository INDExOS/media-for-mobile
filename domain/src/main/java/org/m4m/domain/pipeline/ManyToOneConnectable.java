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

class ManyToOneConnectable implements IsConnectable {
    ManyTypes mOutTypes;
    Class mInType;

    ManyToOneConnectable(ManyTypes manyTypes, Class inType) {
        mOutTypes = manyTypes;
        mInType = inType;
    }

    public static ManyToOneConnectable ManyToOneConnections
        (ManyTypes manyTypes, Class inType) {
        return new ManyToOneConnectable(manyTypes, inType);
    }

    @Override
    public boolean isConnectable(IOutputRaw output, Collection<IInputRaw> input) {
        if (input.size() != 1) {
            return false;
        }
        LinkedList<IOutputRaw> c = new LinkedList<IOutputRaw>();
        c.add(output);
        return isConnectable(c, input.iterator().next());
    }

    @Override
    public boolean isConnectable(Collection<IOutputRaw> outputs, IInputRaw input) {
        if (!mInType.isInstance(input)) {
            return false;
        }
        for (IOutputRaw output : outputs) {
            boolean instanceDetected = false;
            for (Class mOutType : mOutTypes.getTypes()) {
                if (mOutType.isInstance(output)) {
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
}
