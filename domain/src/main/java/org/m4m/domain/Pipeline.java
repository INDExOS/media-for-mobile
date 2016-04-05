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

import org.m4m.AudioFormat;
import org.m4m.domain.pipeline.ConnectorFactory;
import org.m4m.domain.pipeline.IOnStopListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public class Pipeline {

    private final TopologySolver topologySolver = new TopologySolver();
    private final ICommandProcessor commandProcessor;
    private IOnStopListener onStopListener = new IOnStopListener() {
        @Override
        public void onStop() {
            commandProcessor.stop();
        }
    };

    public Pipeline(ICommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public void setMediaSource(IOutput mediaSource) {
        topologySolver.add(mediaSource);
    }

    public void setMediaSource(ICaptureSource mediaSource) {
        topologySolver.add(mediaSource);
    }

    public void setMediaSource(ICameraSource cameraSource) {
        topologySolver.add(cameraSource);
    }

    public void setMediaSource(IMicrophoneSource microphoneSource) {
        topologySolver.add(microphoneSource);
    }

    public void addTransform(ITransform transform) {
        topologySolver.add(transform);
    }

    public void addVideoDecoder(Plugin videoDecoder) {
        topologySolver.add(videoDecoder);
    }

    public void addVideoEncoder(VideoEncoder videoEncoder) {
        topologySolver.add(videoEncoder);
    }

    public void addAudioDecoder(Plugin audioDecoder) {
        topologySolver.add(audioDecoder);
    }

    public void addAudioEncoder(AudioEncoder audioEncoder) {
        topologySolver.add(audioEncoder);
    }

    public void addVideoEffect(VideoEffector effect) {
        topologySolver.add(effect);
    }

    public void addVideoTimeScaler(VideoTimeScaler scaler) {
        topologySolver.add(scaler);
    }

    public void addAudioEffect(AudioEffector effect) {
        topologySolver.add(effect);
    }

    public void setSink(Render sink) {
        topologySolver.add(sink);
        if (sink != null) sink.addOnStopListener(onStopListener);
    }

    public void resolve() {
        AudioFormat audioFormat = getAudioFormat();

        ConnectorFactory connectorFactory = new ConnectorFactory(commandProcessor, audioFormat);

        Collection<IsConnectable> connectionRules = connectorFactory.createConnectionRules();
        for (IsConnectable connectionRule : connectionRules) {
            topologySolver.addConnectionRule(connectionRule);
        }

        Collection<Pair<IOutputRaw, IInputRaw>> connectionQueue = topologySolver.getConnectionsQueue();
        for (Pair<IOutputRaw, IInputRaw> rawPair : connectionQueue) {
            connectorFactory.connect(rawPair.left, rawPair.right);
        }

        startSource();
    }

    private void startSource() {
        for (IOutputRaw iOutputRaw : topologySolver.getSources()) {
            IRunnable mediaSource = (IRunnable) iOutputRaw;
            mediaSource.start();
        }
    }

    private AudioFormat getAudioFormat() {
        AudioFormat audioFormat = null;

        for (IOutputRaw iOutputRaw : topologySolver.getSources()) {
            if (!(iOutputRaw instanceof IOutput)) {
                continue;
            }
            IOutput mediaSource = (IOutput) iOutputRaw;

            audioFormat = (AudioFormat) mediaSource.getMediaFormatByType(MediaFormatType.AUDIO);

            if (audioFormat != null) {
                break;
            }
        }
        return audioFormat;
    }

    public void stop() {
        for (IOutputRaw source : topologySolver.getSources()) {
            IRunnable mediaSource = (IRunnable) source;
            mediaSource.stop();
        }
    }

    public void release() throws IOException {
        for (IOutputRaw node : topologySolver.getSources()) {
            ((Closeable) node).close();
        }

        for (IInputRaw node : topologySolver.getSinks()) {
            ((Closeable) node).close();
        }
    }
}
