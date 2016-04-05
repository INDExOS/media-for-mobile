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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

class FrameBuffer {
    private int numberOfTracks;
    private final Set<Integer> configuredTrackIndexes = new HashSet<Integer>();
    private final Queue<Frame> frames = new LinkedList<Frame>();

    public FrameBuffer(int numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public void configure(int trackIndex) {
        configuredTrackIndexes.add(trackIndex);
    }

    public boolean areAllTracksConfigured() {
        return numberOfTracks == configuredTrackIndexes.size();
    }

    public void push(Frame frame) {
        frames.add(frame);
    }

    public boolean canPull() {
        return areAllTracksConfigured() && !frames.isEmpty();
    }

    public Frame pull() {
        return frames.poll();
    }

    public void addTrack() {
        numberOfTracks++;
    }
}
