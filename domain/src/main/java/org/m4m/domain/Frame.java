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

import java.nio.ByteBuffer;

public class Frame {
    private static final Frame eofFrame = new EofFrame();
    private static final Frame emptyFrame = new Frame(null, 0, 0, 0, 0, 0);;
    private final IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
    protected ByteBuffer byteBuffer;
    protected int trackId;
    private int bufferIndex;
    private boolean skipFrame = false;

    public Frame(ByteBuffer byteBuffer, int length, long sampleTime, int bufferIndex, int flags, int trackId) {
        this.byteBuffer = byteBuffer;
        this.trackId = trackId;
        this.bufferInfo.flags = flags;
        this.bufferInfo.presentationTimeUs = sampleTime;
        this.bufferIndex = bufferIndex;
        this.bufferInfo.size = length;
    }

    public void set(ByteBuffer byteBuffer, int length, long sampleTime, int bufferIndex, int flags, int trackId)
    {
        this.byteBuffer = byteBuffer;
        this.trackId = trackId;
        this.bufferInfo.flags = flags;
        this.bufferInfo.presentationTimeUs = sampleTime;
        this.bufferIndex = bufferIndex;
        this.bufferInfo.size = length;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public int getLength() {
        return bufferInfo.size;
    }

    public void setLength(int length) {
        this.bufferInfo.size = length;
    }

    public long getSampleTime() {
        return bufferInfo.presentationTimeUs;
    }

    public void setSampleTime(long sampleTime) {
        bufferInfo.presentationTimeUs = sampleTime;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public static Frame EOF() {
        return eofFrame;
    }

    public static Frame empty() {

        return emptyFrame;
    }

    public void copyInfoFrom(Frame frame) {
        copyBufferInfoFrom(frame);
    }

    public void copyDataFrom(Frame frame) {
        copyBufferInfoFrom(frame);

        ByteBuffer fromByteBuffer = frame.getByteBuffer().duplicate();
        fromByteBuffer.rewind();
        if (frame.getLength() >= 0) {
            fromByteBuffer.limit(frame.getLength());
        }

        byteBuffer.rewind();
        byteBuffer.put(fromByteBuffer);
    }

    private void copyBufferInfoFrom(Frame frame) {
        this.bufferInfo.size = frame.getLength();
        this.bufferInfo.presentationTimeUs = frame.getSampleTime();
        this.bufferInfo.flags = frame.getFlags();
        this.trackId = frame.getTrackId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Frame)) return false;

        Frame frame = (Frame) o;

        return equals(frame);
    }

    private boolean equals(Frame frame) {
        if (frame instanceof EofFrame) return ((EofFrame) frame).equals(this);

        if (bufferInfo.size == 0 && frame.bufferInfo.size == 0) return true;
        if (bufferInfo.size != frame.bufferInfo.size) return false;
        if (bufferInfo.presentationTimeUs != frame.bufferInfo.presentationTimeUs) return false;
        if (!byteBuffer.equals(frame.byteBuffer)) return false;
        if (trackId != frame.trackId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bufferInfo.hashCode();
        if (byteBuffer != null) result = 31 * result + byteBuffer.hashCode();
        result = 31 * result + trackId;
        result = 31 * result + bufferIndex;
        return result;
    }

    public int getBufferIndex() {
        return bufferIndex;
    }

    public int getFlags() {
        return bufferInfo.flags;
    }

    public void setFlags(int flags) {
        bufferInfo.flags = flags;
    }

    public void toSkipFrame(boolean toSkip) {
        this.skipFrame = toSkip;
    }

    public boolean isSkipFrame() {
        return this.skipFrame;
    }
}

