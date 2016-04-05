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
import org.m4m.domain.Frame;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.IFrameAllocator;
import org.m4m.domain.IOutput;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.Plugin;

class OutputFormatChangedHandler implements ICommandHandler {
    //Logger log = Logger.getLogger(getClass().getSimpleName());

    protected IOutput output;
    protected Plugin plugin;
    private IFrameAllocator inputWithAllocator;

    public OutputFormatChangedHandler(IOutput output, Plugin plugin, IFrameAllocator frameAllocator) {
        this.output = output;
        this.plugin = plugin;
        this.inputWithAllocator = frameAllocator;
    }

    @Override
    public void handle() {
        if (output instanceof MultipleMediaSource) {
            Frame frame = inputWithAllocator.findFreeFrame();

            if (frame == null) {
                restoreCommands();
                return;
            }

            plugin.drain(frame.getBufferIndex());
            plugin.stop();
            plugin.setMediaFormat(output.getMediaFormatByType(plugin.getMediaFormatType()));
            plugin.configure();
            plugin.start();
            plugin.setTrackId(plugin.getTrackId());

            MultipleMediaSource multipleMediaSource = (MultipleMediaSource) output;
            int trackId = multipleMediaSource.getTrackIdByMediaType(plugin.getMediaFormatType());
            multipleMediaSource.selectTrack(trackId);
            multipleMediaSource.setTrackMap(trackId, plugin.getTrackId());
            multipleMediaSource.nextFile();
        }
    }

    private void restoreCommands() {
        output.getOutputCommandQueue().queue(Command.OutputFormatChanged, plugin.getTrackId());
        plugin.getInputCommandQueue().clear();
        plugin.skipProcessing();
        plugin.getInputCommandQueue().queue(Command.NeedData, plugin.getTrackId());
    }
}
