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

import java.nio.ByteBuffer;

public class Resampler {
    //Logger log = Logger.getLogger(getClass().getSimpleName());

    protected int inputChannelCount;
    protected int inputSampleRate;

    protected int targetChannelCount;
    protected int targetSampleRate;

    private boolean configured;

    protected void setup() {
    }

    public Resampler(AudioFormat audioFormat) {
        setup();
        setTargetParameters(audioFormat);
    }

    public void setTargetParameters(AudioFormat audioFormat) {

        int channelCount = audioFormat.getAudioChannelCount();
        int sampleRate = audioFormat.getAudioSampleRateInHz();

        if ((channelCount != 1 && channelCount != 2) || !sampleRateSupported(sampleRate)) {
            throw new IllegalArgumentException("Given target audio parameters not supported.");
        }
        if (targetChannelCount != channelCount || targetSampleRate != sampleRate) {
            targetChannelCount = channelCount;
            targetSampleRate = sampleRate;
        }
    }

    public void setInputParameters(AudioFormat audioFormat) {

        int channelCount = audioFormat.getAudioChannelCount();
        int sampleRate = audioFormat.getAudioSampleRateInHz();

        if ((channelCount != 1 && channelCount != 2) || !sampleRateSupported(sampleRate)) {
            throw new IllegalArgumentException("Given input audio parameters not supported.");
        }
        if (this.inputChannelCount != channelCount || this.inputSampleRate != sampleRate) {
            this.inputChannelCount = channelCount;
            this.inputSampleRate = sampleRate;

            allocateInitInternalBuffers();
        }
    }

    public boolean resamplingRequired() {
        if (inputChannelCount != targetChannelCount || inputSampleRate != targetSampleRate) {
            return true;
        }
        return false;
    }

    public void resampleFrame(Frame frame) {
        if (!configured) {
            throw new IllegalArgumentException("Resampler not configured.");
        }
    }

    public void resampleBuffer(ByteBuffer frameBuffer, int bufferLenght) {
        if (!configured) {
            throw new IllegalArgumentException("Resampler not configured.");
        }
    }

    public int getTargetChannelCount() {
        return targetChannelCount;
    }

    public int getTargetSampleRate() {
        return targetSampleRate;
    }

    public int getInputChannelCount() {
        return inputChannelCount;
    }

    public int getInputSampleRate() {
        return inputSampleRate;
    }

    protected void allocateInitInternalBuffers() {
        configured = true;
    }

    public boolean sampleRateSupported(int sampleRate) {
        for (SampleRate c : SampleRate.values()) {
            if (c.getValue() == sampleRate) {
                return true;
            }
        }
        return false;
    }
}
