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

package org.m4m.domain.dsl;

import org.m4m.Uri;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaExtractor;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaSource;

import java.io.FileDescriptor;

public class MediaSourceFather {
    private final Father create;
    private MediaExtractorFather mediaExtractor;
    private IMediaExtractor extractor = null;
    private int lastNumber;
    private int videoTrackId = 0;
    private int audioTrackId = 1;

    public MediaSourceFather(Father create) {
        this.create = create;
        mediaExtractor = create.mediaExtractor();
    }

    public MediaSourceFather with(IMediaExtractor extractor) {
        this.extractor = extractor;
        return this;
    }

    public MediaSourceFather with(MediaFormat mediaFormat) {
        mediaExtractor.withTrack(mediaFormat);
        return this;
    }

    public MediaSourceFather with(int lastNumber) {
        this.lastNumber = lastNumber;
        return this;
    }

    public MediaSourceFather videoFrames() {
        addFramesToTrack(videoTrackId);
        return this;
    }

    public MediaSourceFather audioFrames() {
        addFramesToTrack(audioTrackId);
        return this;
    }

    public MediaSourceFather frame(int... bytes) {
        this.with(create.frame(bytes).construct());
        return this;
    }

    public MediaSourceFather withAudioTrack(int audioTrackId) {
        this.audioTrackId = audioTrackId;
        mediaExtractor.withTrack(create.audioFormat().construct());
        return this;
    }

    public MediaSourceFather withAudioTrack(int audioTrackId, int durationInMiliseconds) {
        this.audioTrackId = audioTrackId;
        mediaExtractor.withTrack(create.audioFormat().withDuration(durationInMiliseconds).construct());
        return this;
    }

    public MediaSourceFather withVideoTrack(int videoTrackId) {
        this.videoTrackId = videoTrackId;
        mediaExtractor.withTrack(create.videoFormat().construct());
        return this;
    }

    public MediaSourceFather withVideoTrack(int videoTrackId, int durationInMiliseconds) {
        this.videoTrackId = videoTrackId;
        mediaExtractor.withTrack(create.videoFormat().withDuration(durationInMiliseconds).construct());
        return this;
    }

    private void addFramesToTrack(int trackId) {
        for (int i = 0; i < lastNumber; i++) {
            this.with(create.frame().withTrackId(trackId).construct());
        }
    }

    public MediaSourceFather withDuration(int durationInMiliseconds) {
        mediaExtractor.withTrack(create.videoFormat().withDuration(durationInMiliseconds).construct());
        return this;
    }

    public MediaSourceFather with(Frame frame) {
        mediaExtractor.withFrame(frame);
        return this;
    }

    public MediaSourceFather withFilePath(String filePath) {
        mediaExtractor.withFilePath(filePath);
        return this;
    }

    public MediaSourceFather withFileDescriptor(FileDescriptor fileDescriptor) {
        mediaExtractor.withFileDescriptor(fileDescriptor);
        return this;
    }

    public MediaSourceFather withUri(Uri uri) {
        mediaExtractor.withUri(uri);
        return this;
    }

    public MediaSourceFather withInfinite(Frame frame) {
        mediaExtractor.withInfinite(frame);
        return this;
    }

    public MediaSource construct() {
        if (extractor != null) {
            return new MediaSource(extractor);
        }
        return new MediaSource(mediaExtractor.construct());
    }
}
