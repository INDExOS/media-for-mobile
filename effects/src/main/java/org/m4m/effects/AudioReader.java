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
import org.m4m.Uri;

import org.m4m.domain.Resampler;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioReader {
    protected static final int TIMEOUT_USEC = 10000;

    protected MediaExtractor audioExtractor;
    protected MediaCodec audioDecoder;
    protected boolean noEOS = true;
    protected Resampler resampler = null;
    protected AudioReader audioReader;

    protected ByteBuffer[] audioDecoderInputBuffers = null;
    protected ByteBuffer[] audioDecoderOutputBuffers = null;
    protected ByteBuffer[] audioEncoderInputBuffers = null;
    protected ByteBuffer[] audioEncoderOutputBuffers = null;
    protected MediaCodec.BufferInfo audioDecoderOutputBufferInfo = null;
    protected MediaFormat decoderOutputAudioFormat = null;
    protected Uri uri;
    protected AudioFormat primaryAudioFormat;
    protected MediaFormat inputFormat;

    protected ByteBuffer resamplerBuffer;
    protected int resamplerBufferPosition = 0;
    protected int resamplerBufferLimit = 0;
    protected final int frameSize = 2048;
    protected final int maxDeltaHz = 12*2;

    final int targetSampleRate = 48000;
    final int targetChannelCount = 2;


    public void setFileUri(Uri uri) {
        this.uri = uri;
    }

    public void start(Context context, AudioFormat mediaFormat) {
        primaryAudioFormat = mediaFormat;
        audioExtractor = createExtractor(context);

        int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
        inputFormat = audioExtractor.getTrackFormat(audioInputTrack);

        int primarySampleRate = primaryAudioFormat.getAudioSampleRateInHz();
        int secondarySampleRate = inputFormat.getInteger("sample-rate");

        int primaryChanelCount = primaryAudioFormat.getAudioChannelCount();
        int secondaryChanelCount = inputFormat.getInteger("channel-count");

        boolean sampleRateSatisfy = false;
        boolean channelCountSatisfy = false;

        if ((primarySampleRate == targetSampleRate) && (secondarySampleRate == targetSampleRate)){
            sampleRateSatisfy = true;
        }
        if((primaryChanelCount == targetChannelCount)&&(secondaryChanelCount == targetChannelCount)){
            channelCountSatisfy = true;
        }

        // if sampleRate and channelCount both of streams satisfy the target parameters
        // when use standart audio effect applying
        if(sampleRateSatisfy && channelCountSatisfy){
            audioReader = new EqualRateAudioReader(audioExtractor, inputFormat);
        }
        else{
            audioReader = new DifferentRateAudioReader(audioExtractor, inputFormat, secondarySampleRate, secondaryChanelCount);
        }

        audioReader.start(context, mediaFormat);
    }

    public boolean read(ByteBuffer byteBuffer) {
        return audioReader.read(byteBuffer);
    }

    protected MediaExtractor createExtractor(Context context) {
        MediaExtractor extractor;
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, android.net.Uri.parse(uri.getString()), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractor;
    }

    protected int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    protected static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    protected static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    protected MediaCodec createAudioDecoder(MediaFormat inputFormat) {
        MediaCodec decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
            decoder.configure(inputFormat, null, null, 0);
            decoder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return decoder;
    }

    public MediaFormat getDecoderOutputAudioFormat() {
        return decoderOutputAudioFormat;
    }
}
