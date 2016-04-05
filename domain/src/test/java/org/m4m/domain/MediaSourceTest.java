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

public class MediaSourceTest extends TestBase {
    @Test
    public void seek_ToFrameWithTrackId1_HasData1() {
        MediaSource mediaSource = create.mediaSource()
            .withVideoTrack(0)
            .withAudioTrack(1)
            .with(create.frame().withTimeStamp(0).withTrackId(0).construct())
            .with(create.frame().withTimeStamp(100).withTrackId(1).construct())
            .construct();

        mediaSource.start();
        assertThat(mediaSource.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.HasData, 0));

        mediaSource.seek(100);
        assertThat(mediaSource.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.HasData, 1));
    }

    @Test
    public void canSelectExistingTrack() {
        MediaSource mediaSource = create.mediaSource().withAudioTrack(0).construct();

        mediaSource.selectTrack(0);
    }

    @Test(expected = RuntimeException.class)
     public void willNotSelectNonExistingTrack() {
        MediaSource mediaSource = create.mediaSource().withAudioTrack(0).construct();

        mediaSource.selectTrack(1);
    }

    @Test
    public void canUnselectExistingTrack() {
        MediaSource mediaSource = create.mediaSource().withAudioTrack(0).construct();

        mediaSource.unselectTrack(0);
    }

    @Test(expected = RuntimeException.class)
    public void willNotUnselectNonExistingTrack() {
        MediaSource mediaSource = create.mediaSource().withAudioTrack(0).construct();

        mediaSource.unselectTrack(1);
    }
}
