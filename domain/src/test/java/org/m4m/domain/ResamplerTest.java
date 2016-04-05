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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResamplerTest extends TestBase {

    // vvbuzove: need to add a check on the output frame size.
    // need also to check other scenarios:
    // - Short frame buffer (not enough space for the output)
    // - Other?

    @Test(expected = RuntimeException.class)
    public void resampleWhenResamplerNotConfigured_ThrowRuntimeException() {
        Resampler resampler = create.resampler().construct();
        resampler.resampleFrame(a.frame().construct());
    }

    @Test(expected = RuntimeException.class)
    public void canVerifyTargetChannelCount_ThrowRuntimeException() {
        AudioFormat format = create.audioFormat().withChannelCount(3).construct();
        create.resampler().withTargetAudioFormat(format).construct();
    }

    @Test(expected = RuntimeException.class)
    public void canVerifyTargetSampleRate_ThrowRuntimeException() {
        AudioFormat format = create.audioFormat().withSampleRate(1).construct();
        create.resampler().withTargetAudioFormat(format).construct();
    }

    @Test(expected = RuntimeException.class)
    public void canVerifyInputChannelCount_ThrowRuntimeException() {
        AudioFormat format = create.audioFormat().withChannelCount(3).construct();
        create.resampler().withInputAudioFormat(format).construct();
    }

    @Test(expected = RuntimeException.class)
    public void canVerifyInputSampleRate_ThrowRuntimeException() {
        AudioFormat format = create.audioFormat().withSampleRate(1).construct();
        create.resampler().withInputAudioFormat(format).construct();
    }

    @Test
    public void canTellWhetherResamplingRequired_IdenticalParameters() {
        AudioFormat targetFormat = create.audioFormat().withChannelCount(2).withSampleRate(48000).construct();
        AudioFormat inputFormat = create.audioFormat().withChannelCount(2).withSampleRate(48000).construct();

        Resampler resampler = create.resampler().withTargetAudioFormat(targetFormat).withInputAudioFormat(inputFormat).construct();

        assertEquals(false, resampler.resamplingRequired());
    }

    @Test
    public void canTellWhetherResamplingRequired_ChannelCountsDiffer() {
        AudioFormat targetFormat = create.audioFormat().withChannelCount(2).withSampleRate(48000).construct();
        AudioFormat inputFormat = create.audioFormat().withChannelCount(1).withSampleRate(48000).construct();

        Resampler resampler = create.resampler().withTargetAudioFormat(targetFormat).withInputAudioFormat(inputFormat).construct();

        assertEquals(true, resampler.resamplingRequired());
    }

    @Test
    public void canTellWhetherResamplingRequired_SampleRatesDiffer() {
        AudioFormat targetFormat = create.audioFormat().withChannelCount(2).withSampleRate(48000).construct();
        AudioFormat inputFormat = create.audioFormat().withChannelCount(2).withSampleRate(32000).construct();

        Resampler resampler = create.resampler().withTargetAudioFormat(targetFormat).withInputAudioFormat(inputFormat).construct();

        assertEquals(true, resampler.resamplingRequired());
    }


    @Test
    public void afterResampling_SettingsUnchanged() {
        AudioFormat targetFormat = create.audioFormat().withChannelCount(2).withSampleRate(48000).construct();
        AudioFormat inputFormat = create.audioFormat().withChannelCount(1).withSampleRate(32000).construct();
        Resampler resampler = create.resampler().withTargetAudioFormat(targetFormat).withInputAudioFormat(inputFormat).construct();

        resampler.resampleFrame(a.frame().construct());

        assertEquals(targetFormat.getAudioChannelCount(), resampler.getTargetChannelCount());
        assertEquals(targetFormat.getAudioSampleRateInHz(), resampler.getTargetSampleRate());

        assertEquals(inputFormat.getAudioChannelCount(), resampler.getInputChannelCount());
        assertEquals(inputFormat.getAudioSampleRateInHz(), resampler.getInputSampleRate());
    }

    @Test
    public void doResample_convertsSampleRate_MonoToMono() {
        with(1, 48000, 1, 44100, 1000000);
        with(1, 48000, 1, 32000, 1000000);
        with(1, 48000, 1, 8000, 1000000);
    }

    @Test
    public void doResample_convertsSampleRate_StereoToStereo() {
        with(2, 48000, 2, 44100, 1000000);
        with(2, 48000, 2, 32000, 1000000);
        with(2, 48000, 2, 8000, 1000000);
    }

    @Test
    public void doResample_convertsSampleRate_MonoToStereo() {
        with(1, 48000, 2, 44100, 1000000);
        with(1, 48000, 2, 32000, 1000000);
    }

    @Test
    public void doResample_convertsSampleRate_StereoToMono() {
        with(2, 48000, 1, 44100, 1000000);
        with(2, 48000, 1, 32000, 1000000);
    }

    @Test
    public void doResampleFromMonoToStereo() {
        with(1, 8000, 2, 8000, 1000000);
    }

    @Test
    public void doResampleFromStereoToMono() {
        with(2, 48000, 1, 48000, 1000000);
    }

    private void with(int inChannelCount, int inSampleRate, int outChannelCount, int outSampleRate, int numFrames) {

        AudioFormat targetFormat = create.audioFormat().withChannelCount(outChannelCount).withSampleRate(outSampleRate).construct();
        AudioFormat inputFormat = create.audioFormat().withChannelCount(inChannelCount).withSampleRate(inSampleRate).construct();

        Resampler resampler = create.resampler().withTargetAudioFormat(targetFormat).withInputAudioFormat(inputFormat).construct();

        for (int i = 0; i < numFrames; i++) {
            Frame frame = create.frame().construct();
            long sampleTime = frame.getSampleTime();
            resampler.resampleFrame(frame);

            assertEquals(sampleTime, frame.getSampleTime());

            // vvbuzove: need to add a check on the output frame size.
            // outLen = ( (inLen/inChan)*((float)outRate/(float)inRate) + 2 ) * outChan; // +2 - just in case
        }
    }
}
