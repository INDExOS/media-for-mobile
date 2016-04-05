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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleMediaSourceTest extends TestBase {
    @Test
    public void getSegmentsDurationForTwoFiles() throws RuntimeException {
        MediaSource trackWithAudioDuration100 = create.mediaSource().with(
                create.audioFormat().withDuration(100).construct())
            .construct();
        MediaSource trackWithAudioDuration200 = create.mediaSource().with(
            create.audioFormat().withDuration(200).construct())
            .construct();

        MultipleMediaSource multipleMediaSource=create.multipleMediaSource()
            .with(trackWithAudioDuration100)
            .with(trackWithAudioDuration200)
            .construct();

        assertEquals(300, multipleMediaSource.getSegmentsDurationInMicroSec());
    }

    @Test(expected = RuntimeException.class)
    public void add_FilesWithoutAudioTrackIfFirstTrackHasAudioTrack_ThrowsRuntimeException() throws RuntimeException {
        MediaSource firstTrackWithVideoAndAudio = create.mediaSource()
            .withVideoTrack(0)
            .with(create.audioFormat().withBitRate(100).construct()).construct();
        MediaSource trackWithVideoOnly = create.mediaSource().withVideoTrack(0).construct();

        create.multipleMediaSource()
            .with(firstTrackWithVideoAndAudio)
            .with(trackWithVideoOnly)
            .construct();
    }
}
