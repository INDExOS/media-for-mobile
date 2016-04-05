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
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaFormatType;

public class DecoderFather {
    protected MediaCodecFather mediaCodecFather;
    protected IMediaCodec mediaCodec;
    protected MediaFormat mediaFormat;
    private MediaFormatType mediaFormatType = MediaFormatType.VIDEO;
    protected Father create;

    public DecoderFather(Father create) {
        this.create = create;
        mediaCodecFather = create.mediaCodec();
        mediaFormat = create.videoFormat().construct();
    }

    public DecoderFather with(IMediaCodec mediaCodec) {
        this.mediaCodec = mediaCodec;
        return this;
    }

    public DecoderFather with(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
        return this;
    }

    public DecoderFather whichDecodesTo(Frame frame) {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.size = frame.getLength();
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.offset = 0;
        mediaCodecFather
            .withOutputBuffer(frame.getByteBuffer())
            .withDequeueOutputBufferIndex(0)
            .withOutputBufferInfo(bufferInfo);
        return this;
    }

    public DecoderFather withDequeueInputBufferIndex(int... inputBufferIndexes) {
        mediaCodecFather.withDequeueInputBufferIndex(inputBufferIndexes);
        return this;
    }

    public DecoderFather withDequeueOutputBufferIndex(int... outputBufferIndexes) {
        mediaCodecFather.withDequeueOutputBufferIndex(outputBufferIndexes);
        return this;
    }

    public DecoderFather with(MediaFormatType mediaFormatType) {
        this.mediaFormatType = mediaFormatType;
        return this;
    }
}
