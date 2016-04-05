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

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class FileSegment {
    public Pair<Long, Long> pair;

    private Hashtable<Integer, Integer> trackIdMap = new Hashtable<Integer, Integer>();
    private Hashtable<MediaFormatType, Integer> trackTypeMap = new Hashtable<MediaFormatType, Integer>();


    public FileSegment(long left, long right) {
        this.pair = new Pair<Long, Long>(left, right);
    }

    public Long left() {
        return pair.left;
    }

    public Long right() {
        return pair.right;
    }

    public FileSegment(FileSegment fileSegment) {
        this.pair = new Pair<Long, Long>(fileSegment.pair.left, fileSegment.pair.right);

        for (Integer key : fileSegment.getTrackIdMap().keySet()) {
            this.trackIdMap.put(key, fileSegment.getTrackIdMap().get(key));
        }

        for (MediaFormatType key : fileSegment.getTrackTypeMap().keySet()) {
            this.trackTypeMap.put(key, fileSegment.getTrackTypeMap().get(key));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSegment)) return false;

        FileSegment that = (FileSegment) o;

        if (!pair.equals(that.pair)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pair.hashCode();
        return result;
    }

    public void addTrackToMap(int extractorTrackId, int decoderTrackId) {
        trackIdMap.put(extractorTrackId, decoderTrackId);
    }

    public void addTypeToMap(MediaFormatType type, int decoderTrackId) {
        trackTypeMap.put(type, decoderTrackId);
    }

    public int getDecoderTrackIdByType(MediaFormatType type) {
        if (trackTypeMap.get(type) != null) {
            return trackTypeMap.get(type);
        }

        return -1;
    }

    public int getDecoderTrackIdByExtractorTrackId(int extractorTrackId) {
        return trackIdMap.get(extractorTrackId) != null ? trackIdMap.get(extractorTrackId) : extractorTrackId;
    }

    public int getExtractorByDecoderTrackId(int decoderTrackId) {
        Set<Map.Entry<Integer, Integer>> trackIdEntries = trackIdMap.entrySet();

        for (Map.Entry<Integer, Integer> entry : trackIdEntries) {
            if (entry.getValue() == decoderTrackId) {
                return entry.getKey();
            }
        }

        return -1;
    }

    public Hashtable<Integer, Integer> getTrackIdMap() {
        return trackIdMap;
    }

    public Hashtable<MediaFormatType, Integer> getTrackTypeMap() {
        return trackTypeMap;
    }

    public boolean isInsideSegment(long sampleTime) {
        if(sampleTime < pair.right && sampleTime >= pair.left) return true;
        return false;
    }

    public boolean isBeforeSegment(long sampleTime) {
        if(sampleTime <= pair.left) return true;
        return false;
    }

    public long getSegmentDuration() {
        long duration = pair.right - pair.left;

        if(duration > 0) return duration;
        return 0;
    }
}

