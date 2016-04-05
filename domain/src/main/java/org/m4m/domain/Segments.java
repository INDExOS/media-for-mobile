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

import java.util.*;

class Segments {
    private ArrayList<Pair<Long, Long>> segments = new ArrayList<Pair<Long, Long>>();
    private Dictionary<Pair<Long, Long>, Long> lastSegmentSampleTime = new Hashtable<Pair<Long, Long>, Long>();

    SegmentListener segmentListener = new SegmentListener();

    public class SegmentListener{
        public void segmentAdd(){
            if (segments.size() == 1){
                currentSegment = segments.get(0);
            }
        }

        public void segmentRemove(){

        }
    }

    private Pair<Long, Long> currentSegment = null;

    public Segments(List<Pair<Long, Long>> segments) {
        for (Pair<Long, Long> segment : segments) {
            add(segment);
        }
    }

    public boolean isInsideSegment(long sampleTime) {
        if (segments.isEmpty()) return true;
        return getSegmentByTime(sampleTime) != null;
    }

    public void saveSampleTime(long sampleTime) {
        Pair<Long, Long> segment = getSegmentByTime(sampleTime);
        if (segment == null) return;
        Long last = lastSegmentSampleTime.get(segment);
        if (last == null) {
            lastSegmentSampleTime.put(segment, sampleTime);
            return;
        }
        if (last < sampleTime) {
            lastSegmentSampleTime.put(segment, sampleTime);
        }
    }

    public long shift(long sampleTime) {
        long shiftedSampleTime = sampleTime;
        if (getCurrentSegmentTimeShift(sampleTime) != 0) {
            shiftedSampleTime -= getCurrentSegmentTimeShift(sampleTime);
            shiftedSampleTime += getPreviousSegmentsTimeShift(sampleTime);
        }
        return shiftedSampleTime;
    }

    public Pair<Long, Long> getSegmentAfter(long sampleTime) {
        for (Pair<Long, Long> segment : segments) {
            if (sampleTime < segment.left) {
                return segment;
            }
        }
        return null;
    }

    private Pair<Long, Long> getSegmentByTime(long sampleTime) {
        for (Pair<Long, Long> segment : segments) {
            if (segment.left <= sampleTime && sampleTime <= segment.right) {
                return segment;
            }
        }
        return null;
    }

    private long getCurrentSegmentTimeShift(long sampleTime) {
        long timeShift = 0;
        Pair<Long, Long> segment = getSegmentByTime(sampleTime);
        if (segment != null) {
            timeShift = segment.left;
        }
        return timeShift;
    }

    private long getPreviousSegmentsTimeShift(long sampleTime) {
        long timeShift = 0;
        for (Pair<Long, Long> previousSegment : segments) {
            if (previousSegment.right < sampleTime) {
                timeShift += lastSegmentSampleTime.get(previousSegment) - previousSegment.left;
            }
        }
        return timeShift;
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    public void add(Pair<Long, Long> pair) {
        Pair<Long, Long> arrangedPair = arrange(pair);
        if (arrangedPair != null) {
            segments.add(arrangedPair);
            segmentListener.segmentAdd();
        }
    }

    private Pair<Long, Long> arrange(Pair<Long, Long> pair) {
        Pair<Long, Long> arrangedPair = new Pair<Long, Long>(pair.left, pair.right);

        Iterator<Pair<Long, Long>> segmentIterator = segments.iterator();
        while (segmentIterator.hasNext()) {
            Pair<Long, Long> segment = segmentIterator.next();
            if (arrangedPair.left <= segment.left && segment.right <= arrangedPair.right) {
                segmentIterator.remove();
            }
        }

        if (getSegmentByTime(pair.left) == null && getSegmentByTime(pair.right) == null) {
            return arrangedPair;
        }

        if (getSegmentByTime(pair.left) == getSegmentByTime(pair.right)) {
            return null;
        }

        Pair<Long, Long> overlappingSegment = getSegmentByTime(pair.left);
        if (overlappingSegment != null) {
            arrangedPair.left = overlappingSegment.right;
        }

        overlappingSegment = getSegmentByTime(pair.right);
        if (overlappingSegment != null) {
            arrangedPair.right = overlappingSegment.left;
        }

        return arrangedPair;
    }

    public Pair<Long, Long> first() {
        return segments.get(0);
    }

    public Collection<Pair<Long, Long>> asCollection() {
        return new ArrayList<Pair<Long, Long>>(segments);
    }

    public void add(int index, Pair<Long, Long> segment) {
        segments.add(index, segment);
        segmentListener.segmentAdd();
    }

    public void remove(int index) {
        segments.remove(index);
    }

    public void removeOutOfBoundSegments(long boundLimit) {
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).left >= boundLimit) {
                segments.remove(i);
            }
        }
    }

    public boolean checkSegmentChanged(long sampleTime){
        // one segment we only have
        if (segments.size() == 1)
            return false;
        else{
            if (currentSegment == getSegmentAfter(sampleTime)){
                currentSegment = getSegmentAfter(sampleTime);
                return true;
            }
            else
                return false;
        }
    }
}