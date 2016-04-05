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

public class WhenPullFrameFromMultipleMediaSource extends TestBase {
    @Test
    public void shouldGenerateCorrectCommands() throws RuntimeException {
        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(1).videoFrames())
            .with(create.mediaSource().with(1).videoFrames())
            .construct();
        mediaSource.selectTrack(0);
        mediaSource.start();

        Frame frame = create.frame().construct();
        mediaSource.incrementConnectedPluginsCount();
        mediaSource.pull(frame);
        mediaSource.nextFile();
        mediaSource.pull(frame);

        assertThat(mediaSource.getOutputCommandQueue()).equalsTo(
            new Pair<Command, Integer>(Command.HasData, 0),
            new Pair<Command, Integer>(Command.OutputFormatChanged, 0),
            new Pair<Command, Integer>(Command.HasData, 0),
            new Pair<Command, Integer>(Command.EndOfFile, 0)
        );
    }

    @Test
    public void canPullFramesFromTwoFiles() throws RuntimeException {
        Frame firstFileFrame = create.frame(1, 2, 3).construct();
        Frame secondFileFrame = create.frame(4, 5, 6).construct();

        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(firstFileFrame).construct())
            .with(create.mediaSource().with(secondFileFrame).construct())
            .construct();
        multipleMediaSource.start();

        assertThat(multipleMediaSource).willPull(firstFileFrame, secondFileFrame);
    }

    @Test
    public void canPullThreeFramesFromTwoFiles() throws RuntimeException {
        Frame frame1 = create.frame(1, 2, 3).construct();
        Frame frame2 = create.frame(4, 5, 6).construct();
        Frame frame3 = create.frame(7, 8, 9).construct();

        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(frame1).construct())
            .with(create.mediaSource().with(frame2).with(frame3).construct())
            .construct();
        multipleMediaSource.start();

        assertThat(multipleMediaSource).willPull(frame1, frame2, frame3);
    }

    @Test
    public void canPullFramesFromThreeMediaFiles() throws RuntimeException {
        Frame frame1 = create.frame(1, 2, 3).construct();
        Frame frame2 = create.frame(4, 5, 6).construct();
        Frame frame3 = create.frame(7, 8, 9).construct();

        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(frame1).construct())
            .with(create.mediaSource().with(frame2).construct())
            .with(create.mediaSource().with(frame3).construct())
            .construct();
        multipleMediaSource.start();

        assertThat(multipleMediaSource).willPull(frame1, frame2, frame3);
    }

    @Test
    public void canCorrectTimeStampsFromTwoMediaSources() throws RuntimeException {
        Frame firstFileFrame = create.frame().withTimeStamp(1).construct();
        Frame secondFileFrame = create.frame().withTimeStamp(1).construct();
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(firstFileFrame).construct())
            .with(create.mediaSource().with(secondFileFrame).construct())
            .construct();
        multipleMediaSource.start();

        Frame frame = create.frame().construct();
        multipleMediaSource.pull(frame);
        multipleMediaSource.pull(frame);

        assertEquals(1 + 1 + 1, frame.getSampleTime());
    }

    @Test
    public void canCorrectTimeStampsFromThreeMediaSources() throws RuntimeException {
        Frame firstFileFrame = create.frame().withTimeStamp(100).construct();
        Frame secondFileFrame = create.frame().withTimeStamp(110).construct();
        Frame thirdFileFrame = create.frame().withTimeStamp(120).construct();

        MultipleMediaSource multipleMediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(firstFileFrame).construct())
            .with(create.mediaSource().with(secondFileFrame).construct())
            .with(create.mediaSource().with(thirdFileFrame).construct())
            .construct();
        multipleMediaSource.start();


        Frame frame = create.frame().construct();
        multipleMediaSource.pull(frame);
        multipleMediaSource.pull(frame);
        multipleMediaSource.pull(frame);

        assertEquals(100 + 1 + 110 + 1 + 120, frame.getSampleTime());
    }

    @Test
    public void hasEofCommand() throws RuntimeException {
        Frame frame = create.frame(1, 2, 3, 4).withTimeStamp(100500).construct();

        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(frame).construct())
            .construct();
        mediaSource.selectTrack(0);
        mediaSource.start();

        Frame outFrame = create.frame(1, 1, 1, 1).construct();
        mediaSource.getOutputCommandQueue().dequeue();
        mediaSource.pull(outFrame);

        assertEquals(0, frame.getTrackId());
        assertThat(mediaSource.getOutputCommandQueue()).contains(Command.EndOfFile, 0);
        mediaSource.getOutputCommandQueue().dequeue();

        assertThat(mediaSource.getOutputCommandQueue()).isEmpty();
    }

    @Test
    public void generatesNewHasDataCommand() throws RuntimeException {
        Frame frame = create.frame(1, 2, 3, 4).withTimeStamp(100500).construct();
        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(frame).construct())
            .with(create.mediaSource().with(frame).construct())
            .construct();
        mediaSource.incrementConnectedPluginsCount();
        mediaSource.selectTrack(0);
        mediaSource.start();

        Frame frameOut = create.frame(0, 0, 0, 0).withTimeStamp(0).construct();

        assertThat(mediaSource.getOutputCommandQueue()).contains(Command.HasData, 0);
        mediaSource.getOutputCommandQueue().dequeue();
        mediaSource.pull(frameOut);

        assertThat(mediaSource.getOutputCommandQueue()).contains(Command.OutputFormatChanged, 0);
        mediaSource.getOutputCommandQueue().dequeue();
        mediaSource.nextFile();

        assertThat(mediaSource.getOutputCommandQueue()).contains(Command.HasData, 0);
        mediaSource.getOutputCommandQueue().dequeue();
        mediaSource.pull(frameOut);

        assertThat(mediaSource.getOutputCommandQueue()).contains(Command.EndOfFile, 0);
        mediaSource.getOutputCommandQueue().dequeue();

        assertThat(mediaSource.getOutputCommandQueue()).isEmpty();
    }

    @Test
    public void canCorrectTimeStampsFromVideoTrackOnly() throws RuntimeException {
        Frame firstFileVideoFrame = create.frame().withTimeStamp(100).withTrackId(0).construct();
        Frame firstFileAudioFrame = create.frame().withTimeStamp(200).withTrackId(1).construct();
        Frame secondFileFrame = create.frame().withTimeStamp(110).withTrackId(0).construct();

        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().withVideoTrack(0).withAudioTrack(1).with(firstFileVideoFrame).with(firstFileAudioFrame).construct())
            .with(create.mediaSource().withVideoTrack(0).withAudioTrack(1).with(secondFileFrame).construct())
            .construct();
        mediaSource.selectTrack(0);
        mediaSource.selectTrack(1);
        mediaSource.start();

        Frame frame = create.frame().construct();
        mediaSource.pull(frame);
        mediaSource.pull(frame);
        mediaSource.pull(frame);

        assertEquals(200 + 1 + 110, frame.getSampleTime());
    }
}
