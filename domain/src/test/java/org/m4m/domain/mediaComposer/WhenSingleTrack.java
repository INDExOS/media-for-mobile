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

package org.m4m.domain.mediaComposer;

import org.m4m.AudioFormat;
import org.m4m.MediaComposer;
import org.m4m.VideoFormat;
import org.junit.Test;
import org.m4m.domain.AudioDecoder;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.MediaSource;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEncoder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WhenSingleTrack extends MediaComposerTest {
    @Test
    public void canSetAudioFormatWithoutTarget() {
        mediaComposer = new MediaComposer(new AndroidMediaObjectFactoryFake(create), progressListener);
        AudioFormat audioFormat = create.audioFormat().construct();
        mediaComposer.setTargetAudioFormat(audioFormat);
        assertEquals(audioFormat, mediaComposer.getTargetAudioFormat());
    }

    @Test
    public void canSetVideoFormatWithoutTarget() {
        mediaComposer = new MediaComposer(new AndroidMediaObjectFactoryFake(create), progressListener);
        VideoFormat videoFormat = create.videoFormat().construct();
        mediaComposer.setTargetVideoFormat(videoFormat);
        assertEquals(videoFormat, mediaComposer.getTargetVideoFormat());
    }

    @Test
    public void canTranscodeWithVideoFormatSetButNoVideoInStream() throws IOException, InterruptedException {
        AndroidMediaObjectFactoryFake androidMediaObjectFactoryFake = new AndroidMediaObjectFactoryFake(create);

        MediaSource mediaSource = create.mediaSource()
                .withAudioTrack(0, 100).with(4).audioFrames()
                .construct();

        AudioDecoder audioDecoder = create.audioDecoder().construct();

        AudioEncoder audioEncoder = create.audioEncoder()
                .withOutputFormatChanged()
                .whichEncodesTo(a.frame().withTimeStamp(0).construct())
                .whichEncodesTo(a.frame().withTimeStamp(25).construct())
                .whichEncodesTo(a.frame().withTimeStamp(50).construct())
                .whichEncodesTo(a.frame().withTimeStamp(75).withFlag(4).construct())
                .construct();

        androidMediaObjectFactoryFake
                .withMediaSource(mediaSource)
                .withAudioDecoder(audioDecoder)
                .withAudioEncoder(audioEncoder);

        mediaComposer = new MediaComposer(androidMediaObjectFactoryFake, progressListener);

        VideoFormat videoFormat = create.videoFormat().construct();
        mediaComposer.setTargetVideoFormat(videoFormat);

        AudioFormat audioFormat = create.audioFormat().construct();
        mediaComposer.setTargetAudioFormat(audioFormat);

        mediaComposer.addSourceFile("");
        mediaComposer.setTargetFile("");

        mediaComposer.start();
        waitUntilDone(progressListener);

        assertThat(progressListener).containsProgress(0f, 0.25f, 0.5f, 0.75f, 1f);
    }

    @Test
    public void canTranscodeWithAudioFormatSetButNoAudioInStream() throws IOException, InterruptedException {
        AndroidMediaObjectFactoryFake androidMediaObjectFactoryFake = new AndroidMediaObjectFactoryFake(create);

        MediaSource mediaSource = create.mediaSource()
                .withVideoTrack(0, 100).with(4).videoFrames()
                .construct();

        VideoDecoder decoder = create.videoDecoder()
                .whichDecodesTo(create.frame(100).withTimeStamp(12).construct())
                .whichDecodesTo(create.frame(100).withTimeStamp(12).construct())
                .whichDecodesTo(create.frame(100).withTimeStamp(12).construct())
                .whichDecodesTo(create.frame(100).withTimeStamp(12).construct())
                .construct();

        VideoEncoder videoEncoder = create.videoEncoder()
                .withOutputFormatChanged()
                .whichEncodesTo(create.frame(100).withTimeStamp(0).construct())
                .whichEncodesTo(create.frame(100).withTimeStamp(25).construct())
                .whichEncodesTo(create.frame(100).withTimeStamp(50).construct())
                .whichEncodesTo(create.frame(100).withTimeStamp(75).construct())

                .construct();

        androidMediaObjectFactoryFake
                .withVideoDecoder(decoder)
                .withVideoEncoder(videoEncoder)
                .withMediaSource(mediaSource);

        mediaComposer = new MediaComposer(androidMediaObjectFactoryFake, progressListener);

        VideoFormat videoFormat = create.videoFormat().construct();
        mediaComposer.setTargetVideoFormat(videoFormat);

        AudioFormat audioFormat = create.audioFormat().construct();
        mediaComposer.setTargetAudioFormat(audioFormat);

        mediaComposer.addSourceFile("");
        mediaComposer.setTargetFile("");

        mediaComposer.start();
        waitUntilDone(progressListener);

        assertThat(progressListener).containsProgress(0f, 0.25f, 0.5f, 0.75f, 1f);
    }
}
