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
import org.m4m.MediaFileInfo;
import org.m4m.Uri;
import org.m4m.VideoFormat;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileDescriptor;

import static org.junit.Assert.assertEquals;

public class WhenGetFileInfo extends TestBase {
    @Test
    public void basicTest() throws Exception {
        MediaSource mediaSource = create.mediaSource()
            .with(create.videoFormat().withFrameSize(1024, 768).withBitRate(100).construct())
            .with(create.audioFormat().withBitRate(200).construct())
            .construct();

        MediaFileInfo mediaFileInfo = create.mediaFileInfo().with(mediaSource).construct();
        mediaFileInfo.setFileName("");

        VideoFormat videoFormat = (VideoFormat) mediaFileInfo.getVideoFormat();
        Assert.assertEquals(100, videoFormat.getVideoBitRateInKBytes());

        AudioFormat audioFormat = (AudioFormat) mediaFileInfo.getAudioFormat();
        Assert.assertEquals(200, audioFormat.getAudioBitrateInBytes());

        Assert.assertEquals(0xFFFFFF, mediaFileInfo.getDurationInMicroSec());

        int rotation = mediaFileInfo.getRotation();
    }

    @Test
    public void getFileNameReturnsNull() throws Exception {
        MediaFileInfo mediaFileInfo = create.mediaFileInfo().construct();
        assertEquals(null, mediaFileInfo.getFileName());
    }

    @Test
    public void getFileDescriptorReturnsNull() throws Exception {
        MediaFileInfo mediaFileInfo = create.mediaFileInfo().construct();
        assertEquals(null, mediaFileInfo.getFileDescriptor());
    }

    @Test
    public void getUriReturnsNull() throws Exception {
        MediaFileInfo mediaFileInfo = create.mediaFileInfo().construct();
        assertEquals(null, mediaFileInfo.getUri());
    }

    @Test
    public void addFileNameByFileName() throws Exception {
        MediaFileInfo mediaFileInfo = create.mediaFileInfo().construct();
        mediaFileInfo.setFileName("1.mp4");

        String filename = mediaFileInfo.getFileName();
        assertEquals("1.mp4", mediaFileInfo.getFileName());
        assertEquals(null, mediaFileInfo.getFileDescriptor());
        assertEquals(null, mediaFileInfo.getUri());
    }

    @Test
    public void addFileNameByFileDescriptor() throws Exception {
        FileDescriptor fileDescriptor = new FileDescriptor();

        MediaFileInfo mediaFileInfo = create.mediaFileInfo().construct();
        mediaFileInfo.setFileDescriptor(fileDescriptor);

        assertEquals(fileDescriptor, mediaFileInfo.getFileDescriptor());
        assertEquals(null, mediaFileInfo.getFileName());
        assertEquals(null, mediaFileInfo.getUri());
    }

    @Test
    public void addFileNameByUri() throws Exception {
        MediaFileInfo mediaFileInfo = create.mediaFileInfo().construct();
        mediaFileInfo.setUri(new Uri("uri://mp4"));

        assertEquals("uri://mp4", mediaFileInfo.getUri().getString());
        assertEquals(null, mediaFileInfo.getFileName());
        assertEquals(null, mediaFileInfo.getFileDescriptor());
    }
}

