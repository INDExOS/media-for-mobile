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

abstract class Input implements IInput {
    private CommandQueue inputQueue = new CommandQueue();
    protected PluginState state;
    protected int trackId;

    Input() {
        this.state = PluginState.Starting;
    }

    protected void initInputCommandQueue() {}

    void setState(PluginState state) {
        this.state = state;
    }

    @Override
    public CommandQueue getInputCommandQueue() {
        return inputQueue;
    }

    @Override
    public void drain(int bufferIndex) {
        setState(PluginState.Draining);
        getInputCommandQueue().clear();
    }

    protected void feedMeIfNotDraining() {
        if (state != PluginState.Draining && state != PluginState.Drained) {
            getInputCommandQueue().queue(Command.NeedData, getTrackId());
        }
    }

    public abstract void configure();

    @Override
    public void skipProcessing() {
        getInputCommandQueue().clear();
        getInputCommandQueue().queue(Command.NextPair, getTrackId());
    }


    @Override
    public int getTrackId() {
        return trackId;
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
        initInputCommandQueue();
    }
}
