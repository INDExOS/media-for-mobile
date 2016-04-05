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

import org.m4m.domain.dsl.VideoFormatFake;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoFormatTest extends TestBase {
    @Test
    public void setTargetVideoBitRateInKBytes_ConvertsToBytes() {
        VideoFormatFake videoFormat = new VideoFormatFake();
        videoFormat.setVideoFrameSize(1024, 768);

        videoFormat.setVideoBitRateInKBytes(1);

        assertEquals(1 * 1024, (int) videoFormat.values.get("bitrate"));
    }

    @Test
    public void getTargetVideoBitRateInKBytes_ConvertsToKBytes() {
        VideoFormatFake videoFormat = new VideoFormatFake();
        videoFormat.setVideoFrameSize(1024, 768);

        videoFormat.setVideoBitRateInKBytes(1);

        assertEquals(1, videoFormat.getVideoBitRateInKBytes());
    }

    @Test
    public void setTargetVideoBitRateInKBytes_FixesTooBigBitrate() {
        VideoFormatFake videoFormat = new VideoFormatFake();
        videoFormat.setVideoFrameSize(100, 100);

        videoFormat.setVideoBitRateInKBytes(1000);

        assertEquals((int) (100 * 100 * 30 * 2 * 0.00007), videoFormat.getVideoBitRateInKBytes());
    }
}
