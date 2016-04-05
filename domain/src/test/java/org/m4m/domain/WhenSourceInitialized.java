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

import static org.mockito.Mockito.verify;

public class WhenSourceInitialized extends TestBase {
    @Test
    public void addHasOutputToCommandQueue() {
        IOutput source = create.mediaSource().construct();

        source.start();

        assertThat(source.getOutputCommandQueue()).contains(Command.EndOfFile, 0);
    }

    @Test
    public void multipleMediaSourceQueuedHasDataToCommandQueue() throws RuntimeException {
        IOutput source = create.multipleMediaSource()
            .with(create.mediaSource().withVideoTrack(0).with(create.frame().construct()))
            .construct();

        source.start();

        assertThat(source.getOutputCommandQueue()).contains(Command.HasData, 0);
    }

    @Test
    public void multipleMediaSourceQueuedEofToCommandQueue() throws RuntimeException {
        MultipleMediaSource source = create.multipleMediaSource()
            .with(create.mediaSource().withVideoTrack(0))
            .construct();

        source.selectTrack(0);
        source.start();

        assertThat(source.getOutputCommandQueue()).contains(Command.EndOfFile, 0);
    }

    @Test
    public void allTracksSelected() {
        IMediaExtractor extractor = create.mediaExtractor()
            .withTrack(create.videoFormat().construct())
            .withTrack(create.videoFormat().construct())
            .withTrack(create.videoFormat().construct())
            .construct();

        MediaSource mediaSource = create.mediaSource().with(extractor).construct();
        mediaSource.selectTrack(0);
        mediaSource.selectTrack(1);
        mediaSource.selectTrack(2);


        verify(extractor).selectTrack(0);
        verify(extractor).selectTrack(1);
        verify(extractor).selectTrack(2);
    }
}
