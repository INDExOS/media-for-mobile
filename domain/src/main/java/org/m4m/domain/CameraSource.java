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

public class CameraSource implements ICameraSource {
    private CommandQueue commandQueue = new CommandQueue();
    private boolean isStopped = true;

    @Override
    public void setOutputSurface(ISurface surface) { }

    @Override
    public void setPreview(IPreview preview) { }

    @Override
    public void setCamera(Object camera) { }

    @Override
    public void configure() { }

    @Override
    public Frame getFrame() { return null; }

    @Override
    public ISurface getSurface() {
        return null;
    }

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return false;
    }

    @Override
    public CommandQueue getOutputCommandQueue() {
        return commandQueue;
    }

    @Override
    public void fillCommandQueues() { }

    @Override
    public void start() {
        isStopped = false;
    }

    @Override
    public void stop() {
        commandQueue.clear();
        getOutputCommandQueue().queue(Command.EndOfFile, 0);
        isStopped = true;
    }

    @Override
    public Resolution getOutputResolution() {
        return new Resolution(0, 0);
    }

    @Override
    public void close() throws IOException { }

    public Boolean isStopped() { return isStopped; }
}
