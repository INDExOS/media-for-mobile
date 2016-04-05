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

import org.m4m.domain.MediaFormat;

public class MediaFormatTranslator {
    public static android.media.MediaFormat from(MediaFormat mediaFormat) {
        if (mediaFormat instanceof VideoFormatAndroid) {
            return ((VideoFormatAndroid) mediaFormat).getNativeFormat();
        }

        if (mediaFormat instanceof AudioFormatAndroid) {
            return ((AudioFormatAndroid) mediaFormat).getNativeFormat();
        }

        throw new UnsupportedOperationException("Please, don't use MediaFormatTranslator function with this type:" + mediaFormat.getClass().toString());
    }

    public static MediaFormat toDomain(android.media.MediaFormat mediaFormat) {
        if (mediaFormat.getString(android.media.MediaFormat.KEY_MIME).startsWith("video")) {
            return new VideoFormatAndroid(mediaFormat);
        }

        if (mediaFormat.getString(android.media.MediaFormat.KEY_MIME).startsWith("audio")) {
            return new AudioFormatAndroid(mediaFormat);
        }

        throw new UnsupportedOperationException("Unrecognized mime type:" + mediaFormat.getString(android.media.MediaFormat.KEY_MIME));
    }
}
