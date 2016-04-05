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

import org.m4m.domain.MediaFormat;

/**
 * This class is used to describe and/or setup parameters of audio data.
 */
public abstract class AudioFormat extends MediaFormat {
    private static final String KEY_SAMPLE_RATE = "sample-rate";
    private static final String KEY_CHANNEL_COUNT = "channel-count";
    private static final String KEY_IS_ADTS = "is-adts";
    private static final String KEY_CHANNEL_MASK = "channel-mask";
    private static final String KEY_AAC_PROFILE = "aac-profile";
    private static final String KEY_FLAC_COMPRESSION_LEVEL = "flac-compression-level";
    private static final String KEY_MAX_INPUT_SIZE = "max-input-size";

    public static final int ENCODING_PCM_16BIT = 2;

    public static final int CHANNEL_OUT_FRONT_LEFT = 0x4;
    public static final int CHANNEL_OUT_FRONT_RIGHT = 0x8;

    public static final int CHANNEL_OUT_MONO = CHANNEL_OUT_FRONT_LEFT;
    public static final int CHANNEL_OUT_STEREO = (CHANNEL_OUT_FRONT_LEFT | CHANNEL_OUT_FRONT_RIGHT);

    public static final java.lang.String KEY_BIT_RATE = "bitrate";

    private static final String NO_INFO_AVAILABLE = "No info available.";

    private String mimeType;

    protected void setAudioCodec(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns audio codec MIME type.
     *
     * @return MIME type.
     */
    public String getAudioCodec() {
        return this.mimeType;
    }

    /**
     * Sets audio sample rate in Hz.
     *
     * @param sampleRate Sample rate in Hz.
     */
    public void setAudioSampleRateInHz(int sampleRate) {
        setInteger(KEY_SAMPLE_RATE, sampleRate);
    }

    /**
     * Returns audio sample rate in Hz.
     *
     * @return Sample rate in Hz.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getAudioSampleRateInHz() {
        try {
            return getInteger(KEY_SAMPLE_RATE);
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }

    /**
     * Sets the channel count: 2 - stereo, 1 - mono.
     *
     * @param channelCount Channel count.
     */
    public void setAudioChannelCount(int channelCount) {
        setInteger(KEY_CHANNEL_COUNT, channelCount);
    }

    /**
     * Returns the channel count: 2 - stereo, 1 - mono
     *
     * @return Channel count.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getAudioChannelCount() {
        try {
            return getInteger(KEY_CHANNEL_COUNT);
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }

    /**
     * Sets audio bit rate in bytes.
     *
     * @param bitRate Audio bit rate in bytes.
     */
    public void setAudioBitrateInBytes(int bitRate) {
        setInteger(KEY_BIT_RATE, bitRate);
    }

    /**
     * Returns audio bit rate in bytes.
     *
     * @return Audio bit rate in bytes.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getAudioBitrateInBytes() {
        try {
            return getInteger(KEY_BIT_RATE);
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }

    /**
     * Sets maximum audio frame size in samples.
     *
     * @param size Maximum frame size in samples.
     */
    public void setKeyMaxInputSize(int size) {
        setInteger(KEY_MAX_INPUT_SIZE, size);
    }

    /**
     * Optional, if content is AAC audio, sets the desired profile
     * See http://developer.android.com/reference/android/media/MediaCodecInfo.CodecProfileLevel.html for values.
     *
     * @param Profile AAC profile.
     */
    public void setAudioProfile(int Profile) {
        setInteger(KEY_AAC_PROFILE, Profile);
    }

    /**
     * Returns audio AAC profile.
     *
     * @return Audio AAC profile.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getAudioProfile() {
        try {
            return getInteger(KEY_AAC_PROFILE);
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }
}
