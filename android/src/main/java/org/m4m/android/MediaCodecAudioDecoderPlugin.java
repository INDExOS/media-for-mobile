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

import android.media.MediaCodec;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;

import java.io.IOException;

public class MediaCodecAudioDecoderPlugin extends MediaCodecDecoderPlugin {
    public MediaCodecAudioDecoderPlugin() {
        super("audio/mp4a-latm");
    }

    @Override
    public void configure(MediaFormat mediaFormat, ISurfaceWrapper surface, int flags) {
        mediaCodec.configure(MediaFormatTranslator.from(mediaFormat), null, null, flags);
    }

    @Override
    public void release() {
        mediaCodec.release();
    }

    @Override
    public void recreate() {
        try {
            release();
            this.mediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
