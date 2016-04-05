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
import org.m4m.MediaFile;
import org.m4m.VideoFormat;

import java.io.IOException;
import java.util.*;

public class MultipleMediaSource implements IMediaSource {
    private LinkedList<MediaFile> mediaFiles = new LinkedList<MediaFile>();
    private Iterator<MediaFile> mediaFileIterator = null;
    private MediaFile currentMediaFile = null;
    private Hashtable<Integer, Long> sampleTimeOffsets = new Hashtable<Integer, Long>();
    private Hashtable<Integer, Long> currentSampleTimes = new Hashtable<Integer, Long>();
    private CommandQueue commandQueue = new CommandQueue();
    private boolean isLastFile = true;
    private int connectedPluginsCount = 0;
    private int nextFileRequest = 0;
    private Dictionary<Integer, Integer> trackIdMap = new Hashtable<Integer, Integer>();

    @Override
    public CommandQueue getOutputCommandQueue() {
        return commandQueue;
    }

    @Override
    public void fillCommandQueues() {
    }

    @Override
    public void close() throws IOException {
        for (MediaFile mediaFile : mediaFiles) {
            mediaFile.getMediaSource().close();
        }
    }

    @Override
    public void pull(Frame frame) {
        if (currentMediaFile == mediaFiles.getLast()) {
            isLastFile = true;
        }

        pullFrameFromMediaSource(frame);
        hasData();

        currentSampleTimes.put(frame.getTrackId(), frame.getSampleTime());

        if (isLastFrame() && !isLastFile()) {
            switchToNextFile();
        }
    }

    public void nextFile() {
        nextFileRequest++;

        if (nextFileRequest == connectedPluginsCount) {
            hasData();
            nextFileRequest = 0;
        }
    }

    @Override
    public MediaFormat getMediaFormatByType(MediaFormatType mediaFormatType) {
        for (MediaFormat mediaFormat : currentMediaFile.getMediaSource().getMediaFormats()) {
            if (mediaFormat.getMimeType().startsWith(mediaFormatType.toString())) {
                return mediaFormat;
            }
        }
        return null;
    }

    private void pullFrameFromMediaSource(Frame frame) {
        Pair<Command, Integer> firstCommand = currentMediaFile.getMediaSource().getOutputCommandQueue().dequeue();

        if (firstCommand.left == Command.HasData) {
            currentMediaFile.getMediaSource().pull(frame);
            frame.trackId = mapTrackId(frame.trackId);
            frame.setSampleTime(safeGet(sampleTimeOffsets.get(frame.getTrackId())) + frame.getSampleTime());
        }
    }

    private int mapTrackId(int sourceTrackId) {
        if (trackIdMap.get(sourceTrackId) != null) {
            return trackIdMap.get(sourceTrackId);
        }
        return sourceTrackId;
    }

    private long safeGet(Long value) {
        return value == null ? 0 : value;
    }

    private void switchToNextFile() {
        long maxCurrentTimeOffset = getMaxCurrentTimeOffset();
        for (int key : currentSampleTimes.keySet()) {
            sampleTimeOffsets.put(key, maxCurrentTimeOffset + 1);
        }

        currentMediaFile = mediaFileIterator.next();
        currentMediaFile.start();
    }

    private boolean isLastFrame() {

        CommandQueue queue = currentMediaFile.getMediaSource().getOutputCommandQueue();
        Pair<Command, Integer> command = queue.first();

        if (command == null) return false;

        return queue.size() == 1 && command.left == Command.EndOfFile;
    }

    public boolean isLastFile() {
        return isLastFile;
    }

    @Override
    public void incrementConnectedPluginsCount() {
        connectedPluginsCount++;
    }

    @Override
    public void start() {
        currentMediaFile.start();
        hasData();
    }

    public List<MediaFile> files() {
        return mediaFiles;
    }

    public void add(MediaFile mediaFile) throws RuntimeException {
        validate(mediaFile);

        mediaFiles.add(mediaFile);
        mediaFileIterator = mediaFiles.iterator();
        currentMediaFile = mediaFileIterator.next();
        isLastFile = mediaFiles.size() == 1;
    }

    private void validate(MediaFile mediaFile) throws RuntimeException {
        if (mediaFiles.size() == 0) return;

        AudioFormat newFileAudioFormat = (AudioFormat) mediaFile.getMediaSource().getMediaFormatByType(MediaFormatType.AUDIO);
        AudioFormat firstFileAudioFormat = (AudioFormat) mediaFiles.getFirst().getMediaSource().getMediaFormatByType(MediaFormatType.AUDIO);

        if (firstFileAudioFormat == null) return;

        if (newFileAudioFormat == null) {
            throw new RuntimeException("The stream you are trying to add has no audio track, but the first added stream has audio track. Please select a stream with audio track.");
        }
    }

    public long getMaxCurrentTimeOffset() {
        long max = 0;
        for (long currentTimeOffset : currentSampleTimes.values()) {
            if (currentTimeOffset > max) {
                max = currentTimeOffset;
            }
        }
        return max;
    }

    private void hasData() {
        Pair<Command, Integer> firstCommand = currentMediaFile.getMediaSource().getOutputCommandQueue().first();
        if (firstCommand == null) return;

        firstCommand.right = mapTrackId(firstCommand.right);

        if (firstCommand.left != Command.EndOfFile) {
            commandQueue.queue(firstCommand.left, firstCommand.right);
        } else if (!isLastFile) {
            queueCommand(Command.OutputFormatChanged);
        } else {
            queueCommand(Command.EndOfFile);
        }
    }

    private void queueCommand(Command command) {
        for (int trackId : currentMediaFile.getMediaSource().getSelectedTracks()) {
            commandQueue.queue(command, mapTrackId(trackId));
        }
    }

    @Override
    public int getTrackIdByMediaType(MediaFormatType mediaFormatType) {
        return currentMediaFile.getMediaSource().getTrackIdByMediaType(mediaFormatType);
    }

    @Override
    public void selectTrack(int trackId) {
        //TODO - tracks could be swapped from file to file
        for (MediaFile mediaFile : mediaFiles) {
            mediaFile.getMediaSource().selectTrack(trackId);
        }
    }

    public void setTrackMap(int source, int target) {
        trackIdMap.put(source, target);
    }

    @Override
    public void stop() {
        commandQueue.clear();
        queueCommand(Command.EndOfFile);
    }

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return true;
    }

    public long getSegmentsDurationInMicroSec() {
        long totalDuration = 0;
        for (MediaFile mediaFile : mediaFiles) {
            totalDuration += mediaFile.getSegmentsDurationInMicroSec();
        }
        return totalDuration;
    }

    public void remove(MediaFile mediaFile) {
        mediaFiles.remove(mediaFile);
    }

    public void insertAt(int index, MediaFile mediaFile) {
        mediaFiles.add(index, mediaFile);
    }

    @Override
    public Resolution getOutputResolution() {
        VideoFormat videoFormat = (VideoFormat) getMediaFormatByType(MediaFormatType.VIDEO);
        return (videoFormat == null) ? new Resolution(0, 0) : videoFormat.getVideoFrameSize();
    }

    public boolean hasTrack(MediaFormatType mediaFormatType) {
        return getTrackIdByMediaType(mediaFormatType) != -1;
    }

    public void verify() {

        for (MediaFile mediaFile : mediaFiles) {

            boolean withAudio = true;
            boolean withVideo = true;

            if (mediaFile.getMediaSource().getTrackIdByMediaType(MediaFormatType.VIDEO) == -1) {
                withVideo = false;
            }

            if (mediaFile.getMediaSource().getTrackIdByMediaType(MediaFormatType.AUDIO) == -1) {
                withAudio = true;
            }

            boolean videoNoAudio = false;
            boolean videoAudio = false;
            boolean audioNoVideo = false;

            if (withVideo == true && withAudio == false) videoNoAudio = true;
            if (withVideo == true && withAudio == true) videoAudio = true;
            if (withAudio == true && withVideo == false) audioNoVideo = true;

            if (videoAudio == true && audioNoVideo == true) {
                throw new RuntimeException("Cannot process files with and without video in the same pipeline.");
            }
            if (videoAudio == true && videoNoAudio == true) {
                throw new RuntimeException("Cannot process files with and without audio in the same pipeline.");
            }
            if (videoNoAudio == true && audioNoVideo == true) {
                throw new RuntimeException("Cannot process files with and without video in the same pipeline.");
            }
        }
    }
}
