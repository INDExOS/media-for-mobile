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

package org.m4m.effects;

import android.content.Context;

import org.m4m.AudioFormat;
import org.m4m.Uri;
import org.m4m.domain.MediaFormat;

import org.m4m.android.AudioFormatAndroid;

import java.nio.ByteBuffer;

public class SubstituteAudioEffect extends AudioEffect {
    private AudioReader reader = new AudioReader();
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
    private Uri uri;
    private AudioFormatAndroid audioFormat;

    @Override
    public void applyEffect(ByteBuffer input, long timeProgress) {
        if (reader.read(byteBuffer)) {

            audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 48000, 2);

            // there may be problem in allocating memory
            // take a look of all data copying correctly
            if (input.capacity() < byteBuffer.limit()){
                input = ByteBuffer.allocateDirect(byteBuffer.limit() + 2);
            }

            byteBuffer.position(0);

            input.position(0);
            input.limit(byteBuffer.limit());
            input.put(byteBuffer);
        }
    }

    public void setFileUri(Context context, Uri uri, AudioFormat mediaFormat) {
        this.uri = uri;

        reader.setFileUri(uri);
        reader.start(context, mediaFormat);
    }

    public Uri getFileUri() {
        return uri;
    }

    public MediaFormat getMediaFormat() {
        return audioFormat;
    }
}
