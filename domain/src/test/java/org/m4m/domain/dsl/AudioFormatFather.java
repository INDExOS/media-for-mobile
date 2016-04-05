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

package org.m4m.domain.dsl;

import org.m4m.AudioFormat;

public class AudioFormatFather {

    private AudioFormatFake mediaFormat = new AudioFormatFake("audio/mp3", 0, 0);

    private final int channelCount = 2;
    private final int sampleRate = 48000;

    public AudioFormatFather() {
        mediaFormat.setDuration(0);
        mediaFormat.setAudioBitrateInBytes(0);
        mediaFormat.setAudioChannelCount(channelCount);
        mediaFormat.setAudioSampleRateInHz(sampleRate);
    }

    public AudioFormatFather withAudioProfile(int audioProfile) {
        mediaFormat.setAudioProfile(audioProfile);
        return this;
    }

    public AudioFormatFather withMimeType(String mimeType) {
        mediaFormat.setMimeType(mimeType);
        return this;
    }

    public AudioFormatFather withBitRate(int bitRate) {
        mediaFormat.setAudioBitrateInBytes(bitRate);
        return this;
    }

    public AudioFormatFather withSampleRate(int sampleRate) {
        mediaFormat.setAudioSampleRateInHz(sampleRate);
        return this;
    }

    public AudioFormatFather withChannelCount(int channelCount) {
        mediaFormat.setAudioChannelCount(channelCount);
        return this;
    }

    public AudioFormat construct() {
        return mediaFormat;
    }

    public AudioFormatFather withDuration(int durationInMiliseconds) {
        mediaFormat.setDuration(durationInMiliseconds);
        return this;
    }
}
