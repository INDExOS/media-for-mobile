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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class WhenPullFrameFromMediaSource extends TestBase {
    private IMediaExtractor mediaExtractor;

    @Before
    public void setUp() {
        mediaExtractor = create.mediaExtractor().construct();
    }

    @Test
    public void canGetFrame() {
        MediaSource mediaSource = create.mediaSource()
            .with(create.frame(1, 2, 3).construct())
            .construct();
        mediaSource.start();
        Frame frame = create.frame(0, 0, 0).construct();
        mediaSource.pull(frame);

        Assert.assertThat(frame.getByteBuffer().array(), is(equalTo(new byte[] {1, 2, 3})));
    }

    @Test
    public void timeStampTakenFromExtractor() {
        Frame frame = create.frame(1, 2, 3, 4).withTimeStamp(100500).construct();
        mediaExtractor = create.mediaExtractor()
            .withFrame(frame)
            .construct();

        MediaSource mediaSource = new MediaSource(mediaExtractor);
        mediaSource.start();
        Frame pulledFrame = create.frame(0, 0, 0, 0).construct();
        mediaSource.pull(pulledFrame);

        assertEquals(100500, pulledFrame.getSampleTime());
    }

    @Test
    public void eosPulled() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().withTrackId(0).construct())
            .construct();
        mediaSource.start();

        Frame frame = create.frame().construct();
        mediaSource.pull(frame);

        assertThat(mediaSource.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.EndOfFile, 0));
    }

    @Test(expected = IllegalStateException.class)
    public void canNotPullFrameAfterEof() {
        MediaSource mediaSource = create.mediaSource().with(create.frame().construct()).with(create.frame().withTrackId(-1).construct())
            .construct();
        mediaSource.start();

        Frame frame = create.frame().construct();
        mediaSource.pull(frame);
        mediaSource.pull(frame);
    }

    @Test
    public void mediaExtractorAdvanceMethodIsCalled() {
        Frame frame = create.frame(1, 2, 3, 4).withTimeStamp(100500).construct();
        Frame frame2 = create.frame(1, 2, 3, 4).withTimeStamp(100500).construct();

        IMediaExtractor extractor = create.mediaExtractor().withFrame(frame).withFrame(Frame.EOF()).construct();
        MediaSource mediaSource = create.mediaSource().with(extractor).construct();
        mediaSource.start();
        mediaSource.pull(frame2);

        verify(extractor).advance();
    }
}
