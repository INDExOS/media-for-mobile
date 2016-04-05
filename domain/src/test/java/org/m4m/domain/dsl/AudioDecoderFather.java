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

import org.m4m.domain.AudioDecoder;
import org.m4m.domain.Frame;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaFormatType;

public class AudioDecoderFather extends DecoderFather {
    public AudioDecoderFather(Father create) {
        super(create);
        with(MediaFormatType.AUDIO);
        with(create.audioFormat().construct());
    }

    public AudioDecoderFather whichDecodesTo(Frame frame) {
        super.whichDecodesTo(frame);
        return this;
    }

    public AudioDecoderFather with(MediaFormat mediaFormat) {
        super.with(mediaFormat);
        return this;
    }

    public AudioDecoder construct() {
        AudioDecoder decoder = new AudioDecoder(mediaCodec != null ? mediaCodec : mediaCodecFather.construct());
        decoder.setMediaFormat(mediaFormat);
        return decoder;
    }
}
