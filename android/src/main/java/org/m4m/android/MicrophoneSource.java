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


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.m4m.domain.Frame;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaFormatType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MicrophoneSource extends org.m4m.domain.MicrophoneSource {

    private static final long sampleSize = 2;
    private AudioRecord recorder;

    private int sampleRate;
    private int recordChannels;
    private int androidChannels;
    private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int minBufferSize;

    private long startTimeNs;

    @Override
    public void pull(Frame frame) {
        if (isStopped()) {
            super.pull(frame);
            // Sometimes crashes on this code. Is it necessary to release recorder right here?
            /*
            if (recorder != null) {
                recorder.stop();
                recorder.release();

                recorder = null;
            }
            */
            startTimeNs = 0;

            return;
        }

        if (startTimeNs == 0) {
            startTimeNs = System.nanoTime();
        }

        int bufferSize = minBufferSize / 2;

        if (bufferSize > frame.getByteBuffer().capacity()) {
            bufferSize = frame.getByteBuffer().capacity();
        }

        int actualRead;
        if (frame.getByteBuffer().isDirect()) {
            actualRead = recorder.read(frame.getByteBuffer(), bufferSize);
        } else {
            short [] buffer = new short[bufferSize];
            actualRead = recorder.read(buffer, 0, bufferSize);
            byte[] bytes = new byte[buffer.length * 2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
            ByteBuffer bb = ByteBuffer.allocate(minBufferSize).put(bytes);

            frame.setByteBuffer(bb);
        }

        frame.setLength(actualRead);

        long presentationTimeNs = (actualRead / (sampleRate * sampleSize * recordChannels)) / 1000000000;

        long sampleTimeMicrosec = (System.nanoTime() - startTimeNs + presentationTimeNs) / 1000;
        frame.setSampleTime(sampleTimeMicrosec);

        //Log.i("AMP Camera Issue", " " + sampleTimeMicrosec);

        super.pull(frame);
    }

    @Override
    public void close() {
        if (recorder != null) {
            recorder.release();
        }
        recorder = null;
    }

    @Override
    public void start() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, this.sampleRate, androidChannels, audioEncoding, minBufferSize * 4);

        int state = recorder.getState();

        if (state != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalStateException("Failed to start AudioRecord! Used by another application?");
        }

        recorder.startRecording();

        super.start();
    }

    @Override
    public void stop() {

        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }

        recorder = null;
        super.stop();
    }

    @Override
    public MediaFormat getMediaFormatByType(MediaFormatType mediaFormatType) {
        if (mediaFormatType.toString().startsWith("audio") == false) {
            return null;
        }

        return new AudioFormatAndroid("audio/aac", sampleRate, recordChannels);
    }

    public synchronized void configure(int sampleRate, int channels) {
        this.sampleRate = sampleRate;
        recordChannels = channels;

        switch (recordChannels) {
            case 1: {
                androidChannels = AudioFormat.CHANNEL_IN_MONO;
            }
            break;

            case 2: {
                androidChannels = AudioFormat.CHANNEL_IN_STEREO;
            }
            break;
        }

        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, androidChannels, audioEncoding);

        if (minBufferSize < 0) {
            this.sampleRate = 8000;
            minBufferSize = AudioRecord.getMinBufferSize(sampleRate, androidChannels, audioEncoding);
        }
    }
}
