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
import org.m4m.VideoFormat;
import org.m4m.domain.dsl.MediaSourceFather;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhenGetMediaFormat extends MediaComposerTest {
    @Test
    public void canGetAudioFormat() {
        MediaSourceFather mediaSource = create.mediaSource().withAudioTrack(0);
        mediaComposer = create.mediaComposer().with(mediaSource).construct();
        AudioFormat audioFormat = create.audioFormat().construct();

        mediaComposer.setTargetAudioFormat(audioFormat);

        assertEquals(audioFormat, mediaComposer.getTargetAudioFormat());
    }

    @Test
    public void canGetVideoFormat() {
        VideoFormat videoFormat = create.videoFormat().construct();

        mediaComposer.setTargetVideoFormat(videoFormat);

        assertEquals(videoFormat, mediaComposer.getTargetVideoFormat());
    }
}
