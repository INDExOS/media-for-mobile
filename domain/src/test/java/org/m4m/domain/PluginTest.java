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
import org.m4m.CameraCapture;
import org.m4m.VideoFormat;
import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;
import org.m4m.domain.mediaComposer.ProgressListenerFake;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PluginTest extends TestBase {

    protected ProgressListenerFake progressListener;

    @Test
    public void getMediaFormatType_Audio() {
        AudioFormat audioFormat = create.audioFormat().construct();
        AudioEncoder audioEncoder = create.audioEncoder().construct();
        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        factory.withAudioEncoder(audioEncoder);
        new CameraCapture(factory, progressListener).setTargetAudioFormat(audioFormat);

        assertEquals(MediaFormatType.AUDIO, audioEncoder.getMediaFormatType());
    }

    @Test
    public void getMediaFormatType_Video() {
        VideoFormat videoFormat = create.videoFormat().withVideoBitRateInKBytes(0).withFrameRate(0).withIFrameInterval(0).construct();
        VideoEncoder videoEncoder = create.videoEncoder().construct();
        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        factory.withVideoEncoder(videoEncoder);
        new CameraCapture(factory, progressListener).setTargetVideoFormat(videoFormat);

        assertEquals(MediaFormatType.VIDEO, videoEncoder.getMediaFormatType());
    }

    @Test
    public void canConnectFirst() {
        AudioEncoder audioEncoder = create.audioEncoder().construct();

        boolean shouldBeTrue = audioEncoder.canConnectFirst(mock(IInputRaw.class));

        assertTrue(shouldBeTrue);
    }

}
