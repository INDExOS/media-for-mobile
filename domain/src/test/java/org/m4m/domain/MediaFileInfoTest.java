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
import org.m4m.VideoFormat;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MediaFileInfoTest extends TestBase {

    @Test
    public void canGetAudioInfo_WhenAllValuesSet() {

        AudioFormat audioFormat = create.audioFormat().construct();

        audioFormat.setAudioProfile(0);

        String mimeType = audioFormat.getMimeType();
        int channelCount = audioFormat.getAudioChannelCount();
        int sampleRate = audioFormat.getAudioSampleRateInHz();

        int aacProfile = audioFormat.getAudioProfile();
        String audioCodec = audioFormat.getAudioCodec();

        int bitRate = audioFormat.getAudioBitrateInBytes();

        assertEquals("audio/mp3", mimeType);
        assertEquals(2, channelCount);
        assertEquals(48000, sampleRate);

        assertEquals(0, aacProfile);
        assertEquals("audio/mp3", audioCodec);

        assertEquals(0, bitRate);
    }

    @Test
    public void canGetVideoInfo_WhenAllValuesSet() {

        VideoFormat videoFormat = create.videoFormat().construct();

        videoFormat.setVideoFrameRate(0);
        videoFormat.setVideoIFrameInterval(0);
        videoFormat.setVideoBitRateInKBytes(0);

        String mimeType = videoFormat.getMimeType();
        Resolution resolution = videoFormat.getVideoFrameSize();
        int frameRate = videoFormat.getVideoFrameRate();
        int bitRate = videoFormat.getVideoBitRateInKBytes();
        String videoCodec = videoFormat.getVideoCodec();

        assertEquals("video/avc", mimeType);
        assertEquals(0, resolution.width());
        assertEquals(0, resolution.height());
        assertEquals(0, frameRate);
        assertEquals(0, bitRate);
        assertEquals(null, videoCodec);
    }

    @Test(expected = RuntimeException.class)
    public void whenAudioProfileNotSet_ThrowRuntimeException() {

        AudioFormat audioFormat = create.audioFormat().construct();

        audioFormat.getAudioProfile();
    }

    @Test(expected = RuntimeException.class)
    public void whenVideoFrameRateNotSet_ThrowRuntimeException() {

        VideoFormat videoFormat = create.videoFormat().construct();

        videoFormat.getVideoFrameRate();
    }

    @Test(expected = RuntimeException.class)
    public void whenVideoIFrameIntervalNotSet_ThrowRuntimeException() {

        VideoFormat videoFormat = create.videoFormat().construct();

        videoFormat.getVideoIFrameInterval();
    }

    @Test(expected = RuntimeException.class)
    public void whenVideoBitRateInKByteslNotSet_ThrowRuntimeException() {

        VideoFormat videoFormat = create.videoFormat().construct();

        videoFormat.getVideoBitRateInKBytes();
    }
}
