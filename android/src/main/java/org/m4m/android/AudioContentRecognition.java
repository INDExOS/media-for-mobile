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

package org.m4m.android;

import org.m4m.IRecognitionPlugin;
import org.m4m.domain.IAudioContentRecognition;
import org.m4m.domain.RecognitionPipeline;

public class AudioContentRecognition implements IAudioContentRecognition {
    private RecognitionPipeline pipeline;
    private MicrophoneSource source;
    private IRecognitionPlugin plugin;
    private Thread thread;

    public void setRecognizer(IRecognitionPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isRunning() {
        return (thread != null);
    }

    public void start() {
        if (plugin == null) {
            throw new IllegalStateException("Set recognition plugin using setRecognizer before calling start.");
        }

        if (isRunning()) {
            throw new IllegalStateException("Recognition already started.");
        }


        source = new MicrophoneSource();
        source.configure(44100, 1);

        plugin.start();

        pipeline = new RecognitionPipeline(source, plugin);

        startThread();
    }

    public void stop() {
        if (isRunning() == false) {
            return;
        }

        plugin.stop();

        pipeline.stop();
        thread.interrupt();

        pipeline = null;
        thread = null;
    }

    private void startThread() {
        thread = new Thread(new Runnable() {
            public void run() {
                pipeline.start();
            }
        }, "recordingThread");

        thread.start();
    }
}
