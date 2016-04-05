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

import org.m4m.AudioFormat;

import java.nio.ByteBuffer;

public class AudioFormatAndroid extends AudioFormat {
    private android.media.MediaFormat mediaFormat;

    AudioFormatAndroid(android.media.MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
        setAudioCodec(mediaFormat.getString(KEY_MIME));
    }

    public AudioFormatAndroid(String mimeType, int sampleRate, int channelCount) {
        this.mediaFormat = android.media.MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
        setAudioCodec(mimeType);
    }

    public android.media.MediaFormat getNativeFormat() {
        return mediaFormat;
    }

    @Override
    public ByteBuffer getByteBuffer(String key) {
        return mediaFormat.getByteBuffer(key);
    }

    @Override
    public void setInteger(String key, int value) {
        mediaFormat.setInteger(key, value);
    }

    @Override
    public int getInteger(String key) {
        return mediaFormat.getInteger(key);
    }

    @Override
    protected long getLong(String key) {
        return mediaFormat.getLong(key);
    }

    @Override
    protected String getString(String key) {
        return mediaFormat.getString(key);
    }
}