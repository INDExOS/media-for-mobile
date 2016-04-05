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
import org.m4m.domain.Render;

class PullDataCommandHandler implements ICommandHandler {
    protected Render input;
    protected IPluginOutput output;

    public PullDataCommandHandler(IPluginOutput output, Render input) {
        super();
        this.input = input;
        this.output = output;
    }

    @Override
    public void handle() {
        Frame frame = output.getFrame();

        input.pushWithReleaser(frame, output);
        //input.push(frame);
        //output.releaseOutputBuffer(frame.getBufferIndex());
        if (Frame.EOF().equals(frame)) {
            input.drain(frame.getBufferIndex());
        }
    }
}
