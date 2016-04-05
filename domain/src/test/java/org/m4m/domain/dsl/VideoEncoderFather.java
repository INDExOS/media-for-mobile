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

import org.m4m.domain.Encoder;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.ISurface;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.VideoEncoder;

public class VideoEncoderFather extends FatherOf<Encoder> {
    private MediaCodecFather mediaCodecFather;
    private int bufferIndex = 0;

    public VideoEncoderFather(Father create) {
        super(create);
        mediaCodecFather = create.mediaCodec();
    }

    public VideoEncoderFather with(ISurface surface) {
        mediaCodecFather.withSurface(surface);
        return this;
    }

    public VideoEncoderFather with(IMediaCodec mediaCodec) {
        mediaCodecFather.withMediaCodec(mediaCodec);
        return this;
    }

    public VideoEncoderFather with(MediaFormat mediaFormat) {
        mediaCodecFather.withOutputFormat(mediaFormat);
        return this;
    }

    @Override
    public VideoEncoder construct() {
        VideoEncoder videoEncoder = new VideoEncoder(mediaCodecFather.construct());

        return  videoEncoder;
    }

    public VideoEncoderFather withOutputFormatChanged() {
        mediaCodecFather.withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED);
        return this;
    }

    public VideoEncoderFather whichEncodesTo(Frame frame) {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.size = frame.getLength();
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.offset = 0;
        mediaCodecFather
            .withOutputBuffer(frame.getByteBuffer())
            .withDequeueOutputBufferIndex(bufferIndex++)
            .withOutputBufferInfo(bufferInfo);
        return this;
    }

    public VideoEncoderFather withDequeueInputBufferIndex(int... inputBufferIndexes) {
        mediaCodecFather.withDequeueInputBufferIndex(inputBufferIndexes);
        return this;
    }

    public VideoEncoderFather withDequeueOutputBufferIndex(int... outputBufferIndex) {
        mediaCodecFather.withDequeueOutputBufferIndex(outputBufferIndex);
        return this;
    }
}
