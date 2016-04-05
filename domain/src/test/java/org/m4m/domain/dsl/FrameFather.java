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

import org.m4m.domain.Frame;

import java.nio.ByteBuffer;

public class FrameFather {
    private final Father create;
    private ByteBuffer bytes = null;
    private long pts = 0;
    private int length = 0;
    private int inputBufferIndex;
    private int flags = 0;
    private int trackid;

    public FrameFather(Father create) {
        this.create = create;
        bytes = create.byteBuffer();
        length = 0;
    }

    public FrameFather withBuffer(int... bytes) {
        this.bytes = create.byteBuffer(bytes);
        length = bytes.length;
        return this;
    }

    public FrameFather withTimeStamp(long pts) {
        this.pts = pts;
        return this;
    }

    public FrameFather withInputBufferIndex(int inputBufferIndex) {
        this.inputBufferIndex = inputBufferIndex;
        return this;
    }

    public FrameFather withTrackId(int trackid) {
        this.trackid = trackid;
        return this;
    }

    public FrameFather withFlag(int flags) {
        this.flags = flags;
        return this;
    }

    public FrameFather withLength(int length) {
        this.bytes = create.byteBuffer(new int[length]);
        this.length = length;
        return this;
    }

    public Frame construct() {
        return new Frame(bytes, length, pts, inputBufferIndex, flags, trackid);
    }
}
