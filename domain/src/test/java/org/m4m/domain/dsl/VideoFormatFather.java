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

public class VideoFormatFather {
    private VideoFormatFake mediaFormat = new VideoFormatFake();

    public VideoFormatFather() {}

    public VideoFormatFather withMimeType(String mimeType) {
        mediaFormat.setMimeType(mimeType);
        return this;
    }

    public VideoFormatFather withBitRate(int bitRate) {
        mediaFormat.setVideoBitRateInKBytes(bitRate);
        return this;
    }

    public VideoFormatFather withFrameRate(int frameRate) {
        mediaFormat.setVideoFrameRate(frameRate);
        return this;
    }

    public VideoFormatFather withIFrameInterval(int iFrameInterval) {
        mediaFormat.setVideoIFrameInterval(iFrameInterval);
        return this;
    }

    public VideoFormatFather withVideoBitRateInKBytes(int bitRateInKBytes) {
        mediaFormat.setVideoBitRateInKBytes(bitRateInKBytes);
        return this;
    }

    public VideoFormatFather audio() {
        withMimeType("audio");
        return this;
    }

    public VideoFormatFather withDuration(int durationInMiliseconds) {
        mediaFormat.setDuration(durationInMiliseconds);
        return this;
    }

    public VideoFormatFather withFrameSize(int width, int height) {
        mediaFormat.setVideoFrameSize(width, height);
        return this;
    }

    public VideoFormatFake construct() {
        return mediaFormat;
    }
}
