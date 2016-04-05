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

import org.m4m.IRecognitionPlugin;

import java.nio.ByteBuffer;

public class RecognitionPipeline {
    enum State {
        NotInitialized,
        Initialized,
        Running,
        Stopping
    }

    private State state;
    private IRecognitionPlugin recognitionPlugin;
    private IOutput mediaSource;
    private Frame frame;
    private ByteBuffer buffer;
    private IRecognitionPlugin.RecognitionInput pluginInput;
    private final int bufferSize = 1024 * 16;

    public RecognitionPipeline(IOutput source, IRecognitionPlugin plugin) {
        if (plugin == null || source == null) {
            throw new IllegalArgumentException("Plugin or Source can't be null");
        }

        state = State.NotInitialized;
        buffer = ByteBuffer.allocateDirect(bufferSize);
        frame = new Frame(buffer, bufferSize, 0, 0, 0, 0);
        pluginInput = new IRecognitionPlugin.RecognitionInput();
        recognitionPlugin = plugin;
        mediaSource = source;
        pluginInput.setMediaFormat(mediaSource.getMediaFormatByType(MediaFormatType.AUDIO));
    }

    public void start() {
        setState(State.Running);
        mediaSource.start();
        while (state == State.Running) {
            mediaSource.pull(frame);
            pluginInput.setFrame(frame);
            recognitionPlugin.recognize(pluginInput);
        }
        mediaSource.stop();

        setState(State.Initialized);
    }

    public void stop() {
        setState(State.Stopping);
    }

    private void setState(State state) {
        this.state = state;
    }
}
