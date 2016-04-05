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
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.ResamplerAndroid;

import java.nio.ByteBuffer;

public class DifferentRateAudioReader extends AudioReader {
    int primaryFrameBufferSize = 0;

    int inputSampleRate = 0;
    int inputChannelCount = 0;

    int intermediateSampleRate = 0;
    int intermediateChannelCount = 0;


    public DifferentRateAudioReader(MediaExtractor audioExtractor, MediaFormat inputFormat, int inputSampleRate, int inputChanelCount){
        this.audioExtractor = audioExtractor;
        this.inputFormat = inputFormat;

        this.inputSampleRate = inputSampleRate;
        this.inputChannelCount = inputChanelCount;

        resampler = new ResamplerAndroid(new AudioFormatAndroid("audio/mp4a-latm", targetSampleRate, targetChannelCount));
        resampler.setInputParameters(new AudioFormatAndroid("audio/mp4a-latm", inputSampleRate, inputChanelCount));

        resamplerBuffer = ByteBuffer.allocate(frameSize * maxDeltaHz);
    }

    @Override
    public void start(Context context, AudioFormat mediaFormat) {
        audioDecoder = createAudioDecoder(inputFormat);

        audioDecoderInputBuffers = audioDecoder.getInputBuffers();
        audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
        audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();

        intermediateSampleRate  = mediaFormat.getAudioSampleRateInHz();
        intermediateChannelCount = mediaFormat.getAudioChannelCount();
    }

    public boolean read(ByteBuffer byteBuffer) {
        boolean decoderNotAlligned = true;
        boolean noData = true;
        int byteBufferPosition = 0; // position where we begin to write in byteBuffer, particularly after writing the tail

        // just read data from resamplerBufffer and don't touch decoderBuffers at all
        // but if we have lack of data then we are going through the "while" loop to fill
        // resamplerBuffer with data
        if ((resamplerBufferLimit - resamplerBufferPosition) > primaryFrameBufferSize){
            byteBuffer.limit(primaryFrameBufferSize);
            byteBuffer.position(0);
            resamplerBuffer.limit(resamplerBufferPosition + primaryFrameBufferSize);
            resamplerBuffer.position(resamplerBufferPosition);
            byteBuffer.put(resamplerBuffer);

            resamplerBufferPosition += primaryFrameBufferSize;

            return true;
        }
        else if (resamplerBufferPosition != 0){
            // copy the remaining data from resamplerBuffer
            // in simple words we just copy tail of resamplerBuffer data
            byteBuffer.limit(primaryFrameBufferSize);
            byteBuffer.position(0);

            resamplerBuffer.position(resamplerBufferPosition);
            resamplerBuffer.limit(resamplerBufferLimit);

            byteBuffer.put(resamplerBuffer);
            byteBuffer.position(resamplerBufferLimit - resamplerBufferPosition);
            byteBufferPosition+=(resamplerBufferLimit - resamplerBufferPosition);

            if((resamplerBufferLimit - resamplerBufferPosition) == primaryFrameBufferSize){
                resamplerBufferPosition = 0;
                resamplerBufferLimit = 0;

                return true;
            }

            resamplerBufferPosition = 0;
            resamplerBufferLimit = 0;
        }

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
            // when try to mono -> stereo audioDecoderOutputBufferInfo.size - 2048, but (float)targetSampleRate/(float)intermediateSampleRate) = 1 in this case but
            // we need 2, as if we transfer mono to stereo

            int intermediateFrameSize = 0;
            if (intermediateChannelCount == 1){
                intermediateFrameSize = 2048;
            }
            else {
                intermediateFrameSize = 4096;
            }
            primaryFrameBufferSize = (int) ((intermediateFrameSize * targetChannelCount/intermediateChannelCount)*((float)targetSampleRate/(float)intermediateSampleRate));

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

                // logic for handling some problem mono streams on Intel devices
                if (decoderNotAlligned){
                    int decoderChannelCount = decoderOutputAudioFormat.getInteger("channel-count");

                    if (decoderChannelCount != inputChannelCount){
                        inputChannelCount = decoderChannelCount;
                        resampler.setInputParameters(new AudioFormatAndroid("audio/mp4a-latm", inputSampleRate, inputChannelCount));
                    }

                    decoderNotAlligned = false;
                }

                resamplerBuffer.limit(audioDecoderOutputBufferInfo.size);
                resamplerBuffer.position(0);
                decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.size);
                decoderOutputBuffer.position(0);
                resamplerBuffer.put(decoderOutputBuffer);

                resampler.resampleBuffer(resamplerBuffer, audioDecoderOutputBufferInfo.size);
                resamplerBufferLimit = resamplerBuffer.limit();



                int byteBufferLimit = Math.min(primaryFrameBufferSize - byteBufferPosition, resamplerBufferLimit);

                byteBuffer.limit(byteBufferPosition + byteBufferLimit);
                byteBuffer.position(byteBufferPosition);

                // need to keep in mind if we wrote the tail and position of byteBuffer is not null
                if (resamplerBufferLimit < primaryFrameBufferSize){
                    resamplerBuffer.limit(Math.min(primaryFrameBufferSize - byteBufferPosition, resamplerBufferLimit));

                    resamplerBuffer.position(resamplerBufferPosition);
                    byteBuffer.put(resamplerBuffer);

                    byteBufferPosition += byteBufferLimit;
                    resamplerBufferPosition += byteBufferLimit;


                    if (byteBufferPosition >= primaryFrameBufferSize && resamplerBufferPosition >= resamplerBufferLimit) {
                        resamplerBufferPosition = 0;
                        byteBufferPosition = 0;
                        noData = false;
                    }
                    else if(byteBufferPosition >= primaryFrameBufferSize){
                        byteBufferPosition = 0;
                        noData = false;
                    }
                    else if (resamplerBufferPosition >= resamplerBufferLimit){
                        resamplerBufferPosition = 0;
                        noData = true;
                    }
                }
                else{
                    resamplerBuffer.limit(resamplerBufferPosition + primaryFrameBufferSize - byteBufferPosition);
                    resamplerBuffer.position(resamplerBufferPosition);

                    byteBuffer.put(resamplerBuffer);
                    resamplerBufferPosition += byteBufferLimit;

                    noData = false;
                }

                audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
            }
        }

        return !noData;
    }
}
