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
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import org.m4m.AudioFormat;

import java.nio.ByteBuffer;

public class EqualRateAudioReader extends AudioReader {

    public EqualRateAudioReader(MediaExtractor audioExtractor, MediaFormat inputFormat){
        this.audioExtractor = audioExtractor;
        this.inputFormat = inputFormat;
    }

    @Override
    public void start(Context context, AudioFormat mediaFormat) {
        audioDecoder = createAudioDecoder(inputFormat);

        audioDecoderInputBuffers = audioDecoder.getInputBuffers();
        audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
        audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
    }

    public boolean read(ByteBuffer byteBuffer) {
        boolean noData = true;
        while (noData && noEOS) {
            int decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(TIMEOUT_USEC);

            if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            }

            if (decoderInputBufferIndex >= 0) {
                ByteBuffer decoderInputBuffer = audioDecoderInputBuffers[decoderInputBufferIndex];
                int size = audioExtractor.readSampleData(decoderInputBuffer, 0);

                long presentationTime = audioExtractor.getSampleTime();

                if (size >= 0) {
                    audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, audioExtractor.getSampleFlags());
                }

                noEOS = audioExtractor.advance();
            }

            int decoderOutputBufferIndex = audioDecoder.dequeueOutputBuffer(audioDecoderOutputBufferInfo, TIMEOUT_USEC);
            if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            }
            if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();

            }
            if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                decoderOutputAudioFormat = audioDecoder.getOutputFormat();
            }

            if (decoderOutputBufferIndex >= 0) {
                ByteBuffer decoderOutputBuffer = audioDecoder.getOutputBuffers()[decoderOutputBufferIndex];

                byteBuffer.limit(audioDecoderOutputBufferInfo.size);
                byteBuffer.position(0);
                decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.size);
                decoderOutputBuffer.position(0);
                byteBuffer.put(decoderOutputBuffer);
                audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);

                noData = false;
            }
        }

        return !noData;
    }
}
