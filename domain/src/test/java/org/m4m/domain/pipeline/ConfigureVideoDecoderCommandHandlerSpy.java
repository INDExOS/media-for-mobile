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

package org.m4m.domain.pipeline;//

import org.m4m.domain.IVideoOutput;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.dsl.CommandProcessorSpy;

public class ConfigureVideoDecoderCommandHandlerSpy extends ConfigureVideoDecoderCommandHandler {
    private final CommandProcessorSpy commandProcessorSpy;
    private final VideoDecoder input;

    public ConfigureVideoDecoderCommandHandlerSpy(IVideoOutput output, VideoDecoder input, CommandProcessorSpy processor) {
        super(output, input);
        this.input = input;
        this.commandProcessorSpy = processor;
    }

    @Override
    public void handle() {
        commandProcessorSpy.logPair(output, input);
        super.handle();
    }

}
