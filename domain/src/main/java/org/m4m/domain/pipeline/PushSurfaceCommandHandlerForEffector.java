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

import org.m4m.domain.Frame;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.Plugin;

/* Will be merged with PushSurfaceCommandHandler. It should be one universal
 * handler version suits for all component used GL surface.
 */
class PushSurfaceCommandHandlerForEffector implements ICommandHandler {
    protected final IPluginOutput output;
    protected final Plugin input;

    public PushSurfaceCommandHandlerForEffector(IPluginOutput output, Plugin input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public void handle() {
        Frame frame = output.getFrame();
        output.releaseOutputBuffer(frame.getBufferIndex());

        input.push(frame);
        input.checkIfOutputQueueHasData();
    }
}
