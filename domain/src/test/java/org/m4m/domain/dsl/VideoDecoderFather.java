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

import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.VideoDecoder;

public class VideoDecoderFather extends DecoderFather {
    public VideoDecoderFather(Father create) {
        super(create);
    }

    public VideoDecoderFather with(IMediaCodec mediaCodec) {
        super.with(mediaCodec);
        return this;
    }

    public VideoDecoderFather whichDecodesTo(Frame frame) {
        super.whichDecodesTo(frame);
        return this;
    }

    public VideoDecoderFather withDequeueInputBufferIndex(int... inputBufferIndexes) {
        super.withDequeueInputBufferIndex(inputBufferIndexes);
        return this;
    }

    public VideoDecoderFather withDequeueOutputBufferIndex(int... outputBufferIndexes) {
        super.withDequeueOutputBufferIndex(outputBufferIndexes);
        return this;
    }

    public VideoDecoder construct() {
        VideoDecoder decoder = new VideoDecoder(mediaCodec != null ? mediaCodec : mediaCodecFather.construct());
        decoder.setMediaFormat(mediaFormat);
        decoder.setOutputSurface(create.surface().construct());
        return decoder;
    }
}

