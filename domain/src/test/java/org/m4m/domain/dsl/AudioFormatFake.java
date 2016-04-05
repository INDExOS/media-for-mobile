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

import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.Hashtable;

public class AudioFormatFake extends AudioFormat {
    public Dictionary<String, Integer> values = new Hashtable<String, Integer>();
    public Dictionary<String, String> stringValues = new Hashtable<String, String>();
    public Dictionary<String, Long> longValues = new Hashtable<String, Long>();


    public AudioFormatFake(String mimeType, int sampleRate, int channelCount) {
        setAudioCodec(mimeType);
        setAudioSampleRateInHz(sampleRate);
        setAudioChannelCount(channelCount);

        stringValues.put(KEY_MIME, "audio/mp3");
    }

    @Override
    protected int getInteger(String key) {
        return values.get(key);
    }

    @Override
    protected long getLong(String key) {
        return longValues.get(key);
    }

    @Override
    public void setInteger(String key, int value) {
        values.put(key, value);
    }

    @Override
    protected String getString(String key) {
        return null;
    }

    public void setDuration(long duration) {
        longValues.put(KEY_DURATION, duration);
    }

    public void setMimeType(String mimeType) {
        stringValues.put(KEY_MIME, mimeType);
    }

    @Override
    public String getMimeType() {
        return stringValues.get(KEY_MIME);
    }

    @Override
    public ByteBuffer getByteBuffer(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
