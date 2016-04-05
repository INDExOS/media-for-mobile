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
import org.m4m.domain.MediaCodecPlugin;


public class ConfigureVideoTimeScalerVideoEffectorCommand implements ICommandHandler {
    protected final MediaCodecPlugin scaler;
    private final MediaCodecPlugin effector;

    public ConfigureVideoTimeScalerVideoEffectorCommand(MediaCodecPlugin scaler, MediaCodecPlugin effector) {
        this.scaler = scaler;
        this.effector = effector;
    }

    @Override
    public void handle() {
//        effector.getOutputCommandQueue().queue(Command.OutputFormatChanged, effector.getOutputTrackId());
//        effector.getInputCommandQueue().queue(Command.NeedData, effector.getTrackId());

        scaler.getOutputCommandQueue().queue(Command.OutputFormatChanged, effector.getTrackId());
        effector.getInputCommandQueue().queue(Command.NeedInputFormat, scaler.getTrackId());

//        effector.setMediaFormat(scaler.getOutputMediaFormat());
//        scaler.setOutputTrackId(effector.getTrackIdByMediaFormat(scaler.getOutputMediaFormat())); // getTrackIdByMediaFormat
//        effector.start();

//        scaler.checkIfOutputQueueHasData();

//        effector.setInputResolution(scaler.getOutputResolution());
    }
}

