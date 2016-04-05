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

package org.m4m.domain;

import org.m4m.AudioFormat;

public class AudioEncoder extends Encoder {

    private Resampler resampler = null;
    private AudioFormat inputAudioFormat;

    public AudioEncoder(IMediaCodec mediaCodec) {
        super(mediaCodec);
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    @Override
    public void setInputMediaFormat(MediaFormat mediaFormat) {
        inputAudioFormat = (AudioFormat) mediaFormat;

        if (resampler != null) {
            resampler.setInputParameters(inputAudioFormat);
        } else {

            // If we have no resampler functionality we should
            // check that channel count and sample rate match
            if (inputAudioFormat.getAudioSampleRateInHz() != ((AudioFormat) mediaFormat).getAudioSampleRateInHz() &&
                    inputAudioFormat.getAudioChannelCount() != ((AudioFormat) mediaFormat).getAudioSampleRateInHz() ) {
                throw new UnsupportedOperationException("");
            }
        }
    }

    @Override
    protected void initInputCommandQueue() {}

    @Override
    public boolean isLastFile() {
        return true;
    }

    @Override
    public void setOutputSurface(ISurface surface) {}

    @Override
    public void waitForSurface(long pts) {}

    @Override
    public void drain(int bufferIndex) {
        if (state != PluginState.Normal) return;

        super.drain(bufferIndex);
    }

    @Override
    public void push(Frame frame) {

        if (frame.equals(Frame.EOF())) {
            mediaCodec.queueInputBuffer(frame.getBufferIndex(), 0, 0, frame.getSampleTime(), frame.getFlags());
            checkIfOutputQueueHasData();
        }
        else if (!frame.equals(Frame.empty())) {
            if (resampler != null) resampleAudioFrame(frame);
            mediaCodec.queueInputBuffer(frame.getBufferIndex(), 0, frame.getLength(), frame.getSampleTime(), 0);
            checkIfOutputQueueHasData();
        }

        super.push(frame);
    }

    public int getSampleRate() {
        return getAudioFormat().getAudioSampleRateInHz();
    }

    public int getChannelCount() {
        return getAudioFormat().getAudioChannelCount();
    }

    public int getBitRate() {
        return getAudioFormat().getAudioBitrateInBytes();
    }

    public void setSampleRate(int sampleRateInHz) {
        getAudioFormat().setAudioSampleRateInHz(sampleRateInHz);
    }

    public void setChannelCount(int channelCount) {
        getAudioFormat().setAudioChannelCount(channelCount);
    }

    private AudioFormat getAudioFormat() {
        return (AudioFormat) mediaFormat;
    }

    public void resampleAudioFrame(Frame frame) {
        resampler.resampleFrame(frame);
    }

    public void addResampler(Resampler resampler) {
        mediaFormat.setInteger("max-input-size", 48 * 1024); // TODO: What is this? Do we really need this magic number?
        this.resampler=resampler;
    }
}
