
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

import java.io.IOException;

public class CaptureSource implements ICaptureSource {

    protected CommandQueue commandQueue = new CommandQueue();

    private Frame currentFrame = new Frame(null, 0, 0, 0, 0, 0);

    protected long startTime = 0;
    private Boolean isStopped = true;

    @Override
    public void setSurfaceSize(int width, int height) {}

    @Override
    public void beginCaptureFrame() {}

    @Override
    public void endCaptureFrame() {
        if (startTime == 0) {
            startTime = System.nanoTime();
        }
    }

    @Override
    public void addSetSurfaceListener(ISurfaceListener listenMe) {}

    @Override
    public void setOutputSurface(ISurface surface) {}

    @Override
    public MediaFormatType getMediaFormatType() {
        return null;
    }

    @Override
    public MediaFormat getOutputMediaFormat() {
        return null;
    }

    @Override
    public void setTrackId(int trackId) {}

    @Override
    public void setOutputTrackId(int trackId) {}

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {}

    @Override
    public void pull(Frame frame) {}

    @Override
    public MediaFormat getMediaFormatByType(MediaFormatType mediaFormatType) {
        return null;
    }

    @Override
    public boolean isLastFile() {
        return false;
    }

    @Override
    public void incrementConnectedPluginsCount() {}

    @Override
    public void close() throws IOException {}

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return true;
    }

    @Override
    public CommandQueue getOutputCommandQueue() {
        return commandQueue;
    }

    @Override
    public void fillCommandQueues() {}

    @Override
    public Frame getFrame() {
        if (isStopped) {
            commandQueue.clear();
            return Frame.EOF();
        }
        return currentFrame;
    }

    @Override
    public void start() {
        isStopped = false;
    }


    @Override
    public void stop() {
        if (isStopped == false) {
            isStopped = true;
            commandQueue.queue(Command.EndOfFile, 0);
        }
        startTime = 0;
    }

    @Override
    public ISurface getSurface() {
        return null;
    }

    @Override
    public void waitForSurface(long pts) {}

    public boolean isStopped() { return isStopped; }
}
