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

import org.m4m.MediaFile;
import org.m4m.domain.dsl.AudioFormatFake;
import org.m4m.domain.dsl.VideoFormatFake;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;

public class MediaFileTest extends TestBase {
    @Test
    public void canAddSingleSegment() {
        MediaFile mediaFile = new MediaFile(create.mediaSource().construct());
        Pair<Long, Long> segment = new Pair<Long, Long>(0L, 1L);

        mediaFile.addSegment(segment);

        Assert.assertThat(mediaFile.getSegments(), hasItem(segment));
    }

    @Test
    public void canAddSecondSegment() {
        MediaFile mediaFile = new MediaFile(create.mediaSource().construct());
        Pair<Long, Long> firstSegment = new Pair<Long, Long>(0L, 1L);
        Pair<Long, Long> secondSegment = new Pair<Long, Long>(1L, 2L);

        mediaFile.addSegment(firstSegment);
        mediaFile.addSegment(secondSegment);

        Assert.assertThat(mediaFile.getSegments(), hasItem(firstSegment));
        Assert.assertThat(mediaFile.getSegments(), hasItem(secondSegment));
    }

    @Test
    public void canInsertSegment() {
        MediaFile mediaFile = new MediaFile(create.mediaSource().construct());
        Pair<Long, Long> firstSegment = new Pair<Long, Long>(0L, 1L);
        Pair<Long, Long> secondSegment = new Pair<Long, Long>(1L, 2L);

        mediaFile.addSegment(secondSegment);
        mediaFile.insertSegment(0, firstSegment);

        assertEquals(mediaFile.getSegments(), Arrays.asList(firstSegment, secondSegment));
    }

    @Test
    public void canRemoveSegment() {
        MediaFile mediaFile = new MediaFile(create.mediaSource().construct());
        Pair<Long, Long> firstSegment = new Pair<Long, Long>(0L, 1L);
        Pair<Long, Long> secondSegment = new Pair<Long, Long>(1L, 2L);

        mediaFile.addSegment(firstSegment);
        mediaFile.addSegment(secondSegment);
        mediaFile.removeSegment(0);

        assertEquals(mediaFile.getSegments(), Arrays.asList(secondSegment));
    }

    @Test
    public void addDefaultSegment() {
        VideoFormatFake mediaFormat = new VideoFormatFake();
        mediaFormat.setDuration(1000);
        MediaFile mediaFile = new MediaFile(create.mediaSource().with(mediaFormat).construct());

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.start();

        assertEquals(Arrays.asList(new Pair<Long, Long>(0L, 1000L)), mediaFile.getSegments());
    }

    @Test
    public void addDefaultSegmentWithMaxTrackDuration() {
        VideoFormatFake videoFormat = new VideoFormatFake();
        videoFormat.setDuration(1000);
        AudioFormatFake audioFormat = new AudioFormatFake("audio/mp3", 0, 0);
        audioFormat.setDuration(2000);
        MediaFile mediaFile = new MediaFile(create.mediaSource()
                                                .with(videoFormat)
                                                .with(audioFormat)
                                                .construct());

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.getMediaSource().selectTrack(1);
        mediaFile.start();

        assertEquals(Arrays.asList(new Pair<Long, Long>(0L, 2000L)), mediaFile.getSegments());
    }

    @Test
    public void canNotSelectSameTrackTwice() {
        MediaFile mediaFile = new MediaFile(create.mediaSource().construct());

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.getMediaSource().selectTrack(0);

        assertEquals(1, mediaFile.getMediaSource().getSelectedTracks().size());
        Assert.assertThat(mediaFile.getMediaSource().getSelectedTracks(), hasItem(0));
    }

    @Test
    public void shouldSeekOnSegmentStart() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.addSegment(new Pair<Long, Long>(800L, 1200L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);

        assertEquals(1000 - 800, frame.getSampleTime());
    }

    @Test
    public void shouldReturnEofAfterSegmentEnd() {
        MediaSource mediaSource = create.mediaSource()
            .with(create.frame().withTimeStamp(0).construct())
            .with(create.frame().withTimeStamp(1000).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.addSegment(new Pair<Long, Long>(0L, 500L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);

        assertEquals(0, frame.getSampleTime());
        assertThat(mediaSource.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.EndOfFile, 0));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfPullAfterSegmentEnd() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.addSegment(new Pair<Long, Long>(0L, 500L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);
        mediaFile.getMediaSource().pull(frame);
    }

    @Test
    public void shouldSkipFramesOutOfTwoSegments() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct()).with(create.frame().withTimeStamp(2000).construct()).with(create.frame().withTimeStamp(3000).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.addSegment(new Pair<Long, Long>(900L, 1100L));
        mediaFile.addSegment(new Pair<Long, Long>(1800L, 2200L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);
        assertEquals(1000 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals(100 + (2000 - 1800), frame.getSampleTime());
    }

    @Test
    public void canPullTwiAdjacentFramesFromSegment() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct()).with(create.frame().withTimeStamp(1010).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.addSegment(new Pair<Long, Long>(900L, 1100L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);
        assertEquals(1000 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals(1010 - 900, frame.getSampleTime());
    }

    @Test
    public void shouldShiftFramesTimeStampBasedOnLastFrame() {
        MediaSource mediaSource = create.mediaSource()
            .withVideoTrack(0).withAudioTrack(1).with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct()).with(create.frame().withTimeStamp(1010).withTrackId(1).construct()).with(create.frame().withTimeStamp(2000).construct()).with(create.frame().withTimeStamp(3000).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.addSegment(new Pair<Long, Long>(900L, 1200L));
        mediaFile.addSegment(new Pair<Long, Long>(1800L, 2200L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);
        assertEquals(1000 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals(1010 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals(110 + (2000 - 1800), frame.getSampleTime());
    }

    @Test
    public void canPullTwoAdjacentFramesFromThirdSegment() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct()).with(create.frame().withTimeStamp(1010).construct()).with(create.frame().withTimeStamp(2020).construct()).with(create.frame().withTimeStamp(2030).construct()).with(create.frame().withTimeStamp(3040).construct()).with(create.frame().withTimeStamp(3050).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.addSegment(new Pair<Long, Long>(900L, 1100L));
        mediaFile.addSegment(new Pair<Long, Long>(1900L, 2100L));
        mediaFile.addSegment(new Pair<Long, Long>(2900L, 3100L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);
        assertEquals(1000 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals(1010 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals((1010 - 900) + 2020 - 1900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals((1010 - 900) + 2030 - 1900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals((1010 - 900 + 2030 - 1900) + 3040 - 2900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals((1010 - 900 + 2030 - 1900) + 3050 - 2900, frame.getSampleTime());
    }

    @Test
    public void canPullFramesFromSecondSegment() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTimeStamp(0).construct()).with(create.frame().withTimeStamp(1000).construct()).with(create.frame().withTimeStamp(2000).construct()).with(create.frame().withTimeStamp(3000).construct())
            .construct();
        MediaFile mediaFile = new MediaFile(mediaSource);

        mediaFile.getMediaSource().selectTrack(0);
        mediaFile.addSegment(new Pair<Long, Long>(900L, 1100L));
        mediaFile.addSegment(new Pair<Long, Long>(2900L, 3100L));
        mediaFile.start();

        Frame frame = create.frame().construct();
        mediaFile.getMediaSource().pull(frame);
        assertEquals(1000 - 900, frame.getSampleTime());

        mediaFile.getMediaSource().pull(frame);
        assertEquals((1000 - 900) + 3000 - 2900, frame.getSampleTime());
    }
}
