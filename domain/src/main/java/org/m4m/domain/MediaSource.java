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

import org.m4m.Uri;
import org.m4m.VideoFormat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.*;

public class MediaSource implements IMediaSource {
    //private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    private final IMediaExtractor mediaExtractor;
    private CommandQueue commandQueue = new CommandQueue();
    private int lastTrackId = 0;
    private Set<Integer> selectedTracks = new HashSet<Integer>();
    private PluginState state = PluginState.Drained;
    private Segments segments = new Segments(new ArrayList<Pair<Long, Long>>());
    private long seekPosition = 0l;
    private boolean seekedOutsideNeededSegment = false;


    public MediaSource(IMediaExtractor mediaExtractor) {
        this.mediaExtractor = mediaExtractor;
    }

    public void selectTrack(int trackIndex) {
        if (trackIndex > (mediaExtractor.getTrackCount() - 1)) {
            throw new RuntimeException("Attempt to select non-existing track.");
        }
        mediaExtractor.selectTrack(trackIndex);
        selectedTracks.add(trackIndex);
    }

    public void unselectTrack(int trackIndex) {
        if (trackIndex > (mediaExtractor.getTrackCount() - 1)) {
            throw new RuntimeException("Attempt to unselect non-existing track.");
        }
        mediaExtractor.unselectTrack(trackIndex);
        selectedTracks.remove(trackIndex);
    }

    @Override
    public CommandQueue getOutputCommandQueue() {
        return commandQueue;
    }

    @Override
    public void fillCommandQueues() {
    }

    @Override
    public void close() throws IOException {
        mediaExtractor.release();
    }

    @Override
    public void pull(Frame frame) {
        if (state != PluginState.Normal) {
            throw new IllegalStateException("Attempt to pull frame from not started media source or after EOF.");
        }

        readSampleData(frame);
        //log.debug("MediaSource reads frame pts=" + frame.getSampleTime() + ", trackId=" + frame.getTrackId() + ", flags=" + frame.getFlags() + ", length=" + frame.getLength());

        if (!frame.equals(Frame.EOF())) {
            mediaExtractor.advance();
            checkIfHasData();
        }
    }

    private void readSampleData(Frame frame) {
        frame.setSampleTime(getSampleTime());
        frame.setTrackId(getTrackId());
        frame.setFlags(mediaExtractor.getSampleFlags());
        frame.setLength(mediaExtractor.readSampleData(frame.getByteBuffer()));
        frame.getByteBuffer().position(0);
        frame.toSkipFrame(getSkipDecision());
    }

    private boolean getSkipDecision() {
        if (mediaExtractor.getSampleTime() < getSeekPosition()) return true;
        return false;
    }

    private void checkIfHasData() {
        if (mediaExtractor.getSampleTrackIndex() == -1) {
            drain();
            return;
        }

        if(!reachedSeekPosition(mediaExtractor.getSampleTime())) {
            hasData();
            return;
        } else if(segments.isInsideSegment(mediaExtractor.getSampleTime())) {
            hasData();
            return;
        }

        Pair<Long, Long> segmentAfter = segments.getSegmentAfter(mediaExtractor.getSampleTime());
        if (segmentAfter == null) {
            drain();
            return;
        } else {
            seek(segmentAfter.left);
        }

    }

    private boolean reachedSeekPosition(long sampleTime) {
        if(sampleTime < getSeekPosition()) return false;
        return true;
    }

    private void hasData() {
        commandQueue.queue(Command.HasData, mediaExtractor.getSampleTrackIndex());
        lastTrackId = mediaExtractor.getSampleTrackIndex();
    }

    private int getTrackId() {
        int trackId = mediaExtractor.getSampleTrackIndex();
        return trackId == -1 ? lastTrackId : trackId;
    }

    private long getSampleTime() {
        long sampleTime = mediaExtractor.getSampleTime();

        if (!reachedSeekPosition(sampleTime)) return sampleTime;

        segments.saveSampleTime(sampleTime);
        return segments.shift(sampleTime);
    }

    private void drain() {
        state = PluginState.Draining;
        commandQueue.clear();
        commandQueue.queue(Command.EndOfFile, lastTrackId);
    }

    public Iterable<MediaFormat> getMediaFormats() {
        LinkedList<MediaFormat> result = new LinkedList<MediaFormat>();
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            result.add(mediaExtractor.getTrackFormat(i));
        }
        return result;
    }

    @Override
    public MediaFormat getMediaFormatByType(MediaFormatType mediaFormatType) {
        for (MediaFormat mediaFormat : getMediaFormats()) {
            if (mediaFormat.getMimeType().startsWith(mediaFormatType.toString())) {
                return mediaFormat;
            }
        }
        return null;
    }

    @Override
    public boolean isLastFile() {
        return true;
    }

    @Override
    public void incrementConnectedPluginsCount() {
    }

    @Override
    public void start() {
        state = PluginState.Normal;
        if (segments.isEmpty()) {
            segments.add(new Pair<Long, Long>(0L, getDurationInMicroSec()));
        } else {
            removeOutOfBoundSegments();
        }
        seek(segments.first().left);
    }

    private void removeOutOfBoundSegments() {
        segments.removeOutOfBoundSegments(getDurationInMicroSec());
    }

    public int getTrackIdByMediaType(MediaFormatType mediaFormatType) {
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            if (mediaExtractor.getTrackFormat(i) != null &&
                    mediaExtractor.getTrackFormat(i).getMimeType() != null &&
                    mediaExtractor.getTrackFormat(i).getMimeType().startsWith(mediaFormatType.toString())) {
                return i;
            }
        }
        return -1;
    }

    public long getDurationInMicroSec() {
        long duration = getMaxSelectedTracksDuration();
        if (duration == 0) {
            duration = getMaxAllTracksDuration();
        }
        return duration;
    }

    private long getMaxSelectedTracksDuration() {
        long maxDuration = 0;
        for (int trackIndex : selectedTracks) {
            if (mediaExtractor.getTrackFormat(trackIndex) != null
            && (mediaExtractor.getTrackFormat(trackIndex).getDuration() > maxDuration)) {
                maxDuration = mediaExtractor.getTrackFormat(trackIndex).getDuration();
            }
        }
        return maxDuration;
    }

    private long getMaxAllTracksDuration() {
        long maxDuration = 0;
        int i = 0;
        for (MediaFormat ignored : getMediaFormats()) {
            if (mediaExtractor.getTrackFormat(i).getDuration() > maxDuration) {
                maxDuration = mediaExtractor.getTrackFormat(i).getDuration();
            }
            i++;
        }
        return maxDuration;
    }

    public Set<Integer> getSelectedTracks() {
        return selectedTracks;
    }

    public void seek(long seekPosition) {
        mediaExtractor.seekTo(seekPosition, IMediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        commandQueue.clear();

        // Fixing dental drill by removing audio frame comming before video
        // TODO: need to buffer all audio frames and compare timestamps with 1st video frame
        if (hasVideoTrack()) {
            while (!isVideoTrack()) {
                mediaExtractor.advance();
            }
        }

        setSeekPosition(seekPosition);
        checkIfHasData();
    }

    private boolean hasVideoTrack() {
        for (Integer selectedTrack : selectedTracks) {
            if (isVideoTrack(selectedTrack)) return true;
        }
        return false;
    }

    private boolean isVideoTrack() {
        return isVideoTrack(getTrackId());
    }

    private boolean isVideoTrack(int trackId) {
        String mimeType = mediaExtractor.getTrackFormat(trackId).getMimeType();
        return mimeType.startsWith("video");
    }

    @Override
    public void stop() {
        drain();
    }

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return true;
    }

    public int getRotation() {
        return mediaExtractor.getRotation();
    }

    public String getFilePath() {
        return mediaExtractor.getFilePath();
    }

    public FileDescriptor getFileDescriptor() {
        return mediaExtractor.getFileDescriptor();
    }

    public Uri getUri() {
        return mediaExtractor.getUri();
    }

    public void add(Pair<Long, Long> segment) {
        segments.add(segment);
    }

    public Collection<Pair<Long, Long>> getSegments() {
        return segments.asCollection();
    }

    public void insert(Pair<Long, Long> segment, int index) {
        segments.add(index, segment);
    }

    public void removeSegment(int index) {
        segments.remove(index);
    }

    public long getSegmentsDurationInMicroSec() {
        if (segments.isEmpty()) {
            return getDurationInMicroSec();
        }

        long totalDuration = 0;
        for (Pair<Long, Long> segment : segments.asCollection()) {
            totalDuration += (segment.right - segment.left);
        }
        return totalDuration;
    }

    @Override
    public Resolution getOutputResolution() {
        VideoFormat videoFormat = (VideoFormat) getMediaFormatByType(MediaFormatType.VIDEO);

        if(videoFormat == null) {
            throw new UnsupportedOperationException("Failed to get output resolution.");
        }

        return videoFormat.getVideoFrameSize();
    }

    private void setSeekPosition(long seekPosition) {
        this.seekPosition = seekPosition;
    }

    private long getSeekPosition() {
        return this.seekPosition;
    }
}
