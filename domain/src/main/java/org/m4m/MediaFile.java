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

package org.m4m;

import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.Pair;

import java.io.FileDescriptor;
import java.util.Collection;

/**
 * This class contains information about a media file: formats of tracks, file duration and other information.
 */
public class MediaFile {
    private MediaSource mediaSource;

    /**
     * Instantiate a MediaFile with platform dependent MediaSource.
     *
     * @param mediaSource Platform dependent MediaSource.
     */
    public MediaFile(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
    }

    /**
     * Gets MediaSource object.
     *
     * @return MediaSource object
     */
    public MediaSource getMediaSource() {
        return mediaSource;
    }

    /**
     * Add a media file segment to a collection of file segments.
     *
     * @param segment Pair indicating the beginning (start) and the end (stop) of the file segment.
     */
    public void addSegment(Pair segment) {
        mediaSource.add(segment);
    }

    /**
     * Gets the collection of file segments.
     *
     * @return Collection of segments (collection of pairs).
     */
    public Collection<Pair<Long, Long>> getSegments() {
        return mediaSource.getSegments();
    }

    /**
     * Inserts a segment into the collection of file segments.
     *
     * @param index   Index to be used for the new segment.
     * @param segment Segment to be inserted.
     */
    public void insertSegment(int index, Pair segment) {
        mediaSource.insert(segment, index);
    }

    /**
     * Removes an indicated segment from the collection of file segments.
     *
     * @param index Index of the segment.
     */
    public void removeSegment(int index) {
        mediaSource.removeSegment(index);
    }

    /**
     * Returns the number of audio tracks in a the media file.
     *
     * @return Number of tracks.
     */
    /*public int getVideoTracksCount() {
        return 0;
    }*/

    /**
     * Returns the number of audio tracks in a the media file.
     *
     * @return Number of tracks.
     */
    /*public int getAudioTracksCount() {
        return 0;
    }*/

    /**
     * Returns VideoFormat for an indicated video track.
     *
     * @param index Video track index.
     * @return VideoFormat for the indicated video track.
     */
    public VideoFormat getVideoFormat(int index) {
        return (VideoFormat) mediaSource.getMediaFormatByType(MediaFormatType.VIDEO);
    }

    /**
     * Returns AudioFormat for an indicated audio track.
     *
     * @param index Audio track index.
     * @return AudioFormat for the indicated audio track.
     */
    public AudioFormat getAudioFormat(int index) {
        return (AudioFormat) mediaSource.getMediaFormatByType(MediaFormatType.AUDIO);
    }

    /**
     * Selects audio track
     *
     * @param index Index of an audio track to be selected.
     */
    public void setSelectedAudioTrack(int index) {
    }

    /**
     * Returns duration of a media file in microseconds.
     *
     * @return Duration value in microseconds.
     */
    public long getDurationInMicroSec() {
        return mediaSource.getDurationInMicroSec();
    }

    /**
     * Returns the total duration in microseconds of media file segments.
     *
     * @return Duration value in microseconds.
     */
    public long getSegmentsDurationInMicroSec() {
        return mediaSource.getSegmentsDurationInMicroSec();
    }

    /**
     * Starts processing.
     */
    public void start() {
        mediaSource.start();
    }

    /**
     * Returns the video rotation angle in degrees. Possible return values: 0, 90, 180, or 270 degrees.
     */
    public int getRotation() {
        return mediaSource.getRotation();
    }

    /**
     * Gets the media file name.
     *
     * @return File name. String class object.
     */
    public String getFilePath() {
        return mediaSource.getFilePath();
    }

    /**
     * Gets the media file descriptor.
     *
     * @return File descriptor. FileDescriptor class object.
     */
    public FileDescriptor getFileDescriptor() {
        return mediaSource.getFileDescriptor();
    }

    /**
     * Gets the media file URI.
     *
     * @return File URI. Uri class object.
     */
    public Uri getUri() {
        return mediaSource.getUri();
    }
}
