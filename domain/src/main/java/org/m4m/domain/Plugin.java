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

public abstract class Plugin extends Input implements ITransform, IPluginOutput {
    private CommandQueue outputQueue = new CommandQueue();
    protected MediaFormat mediaFormat = null;

    protected Plugin() {
        super();
    }

    public void checkIfOutputQueueHasData() {}

    public void notifySurfaceReady(ISurface surface) {}

    @Override
    public CommandQueue getOutputCommandQueue() {
        return outputQueue;
    }

    public abstract void start();

    public abstract void stop();

    @Override
    public void push(Frame frame) {
        if (frame.equals(Frame.EOF())) {
            drain(frame.getBufferIndex());
        }
    }

    @Override
    public MediaFormatType getMediaFormatType() {
        if (mediaFormat.getMimeType().startsWith("audio")) {
            return MediaFormatType.AUDIO;
        }

        return MediaFormatType.VIDEO;
    }

    @Override
    public MediaFormat getMediaFormatByType(MediaFormatType mediaFormatType) {
        if (mediaFormat.getMimeType().startsWith(mediaFormatType.toString())) {
            return mediaFormat;
        }
        return null;
    }

    @Override
    public MediaFormat getOutputMediaFormat() {
        return mediaFormat;
    }

    @Override
    public void incrementConnectedPluginsCount() {}

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return true;
    }

    public boolean canConnectFirst(IOutputRaw connector) {
        return true;
    }

    @Override
    public void recreate() {}

    public void setInputResolution(Resolution resolution) {
        getSurface().setInputSize(resolution.width(), resolution.height());
    }
}
