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

import org.m4m.domain.Command;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.IVideoOutput;
import org.m4m.domain.Plugin;

class ConfigureVideoDecoderCommandHandler implements ICommandHandler {
    protected final IVideoOutput output;
    private final Plugin input;

    public ConfigureVideoDecoderCommandHandler(IVideoOutput output, Plugin decoder) {
        this.output = output;
        input = decoder;
    }

    @Override
    public void handle() {
        output.getOutputCommandQueue().queue(Command.HasData, input.getTrackId());
        input.setInputResolution(output.getOutputResolution());
    }
}

