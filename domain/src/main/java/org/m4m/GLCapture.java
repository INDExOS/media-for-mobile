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

package org.m4m;

import org.m4m.domain.CapturePipeline;
import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.IMicrophoneSource;

/**
 * Captures and encodes EGL scene to a video file or to network stream
 */

public class GLCapture extends CapturePipeline {
    private ICaptureSource videoSource;
    private IMicrophoneSource audioSource;
    private boolean frameInProgress;

    public GLCapture(IAndroidMediaObjectFactory factory, IProgressListener progressListener) {
        super(factory, progressListener);
    }

    @Override
    protected void setMediaSource() {
        videoSource = androidMediaObjectFactory.createCaptureSource();
        pipeline.setMediaSource(videoSource);
        if (audioSource != null) {
            pipeline.setMediaSource(audioSource);
        }
    }

    /**
     * Configure audio parameters of capturing
     *
     * @param mediaFormat required audio parameters (sampling frequency, number of channels)
     */
    @Override
    public void setTargetAudioFormat(AudioFormat mediaFormat) {
        super.setTargetAudioFormat(mediaFormat);
        audioSource = androidMediaObjectFactory.createMicrophoneSource();
        audioSource.configure(mediaFormat.getAudioSampleRateInHz(), mediaFormat.getAudioChannelCount());
    }

    /**
     * Stops media capturing
     */
    @Override
    public void stop() {
        /*
        videoSource.stop();

        if (audioSource != null) {
            audioSource.stop();
        }

        commandProcessor.process();
        */

        super.stop();
    }

    /**
     * Configures capturing surface
     *
     * @param width  surface width
     * @param height surface height
     */
    public void setSurfaceSize(int width, int height) {
        videoSource.setSurfaceSize(width, height);
    }

    /**
     * Switches the current OpenGL context to capturing context
     */
    public void beginCaptureFrame() {
        if (frameInProgress) {
            return;
        }

        frameInProgress = true;

        videoSource.beginCaptureFrame();
    }

    /**
     * Switches the current OpenGL context back from capturing context
     */
    public void endCaptureFrame() {
        if (!frameInProgress) {
            return;
        }

        videoSource.endCaptureFrame();

        frameInProgress = false;
    }
}
