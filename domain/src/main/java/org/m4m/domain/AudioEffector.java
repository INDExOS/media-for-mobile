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

import org.m4m.IAudioEffect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

public class AudioEffector extends MediaCodecPlugin {
    private LinkedList<IAudioEffect> audioEffects = new LinkedList<IAudioEffect>();

    private LinkedList<Frame> framesPool = new LinkedList<Frame>();
    private LinkedList<Frame> framesOutput = new LinkedList<Frame>();
    private int capacity = 24 * 1024;

    public AudioEffector(IMediaCodec mediaCodec) {
        super(mediaCodec);
        initInputCommandQueue();

        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        ByteBuffer buffer1 = ByteBuffer.allocate(capacity);
        ByteBuffer buffer2 = ByteBuffer.allocate(capacity);
        framesPool.add(new Frame(buffer, capacity, 0, 0, 0, 0));
        framesPool.add(new Frame(buffer1, capacity, 0, 0, 0, 0));
        framesPool.add(new Frame(buffer2, capacity, 0, 0, 0, 0));
    }

    public LinkedList<IAudioEffect> getAudioEffects() {
        return audioEffects;
    }

    @Override
    protected void initInputCommandQueue() {
        feedMeIfNotDraining();
    }

    @Override
    protected void feedMeIfNotDraining() {
        if (state != PluginState.Draining && state != PluginState.Drained) {
            getInputCommandQueue().queue(Command.NeedData, getTrackId());
        }
    }

    @Override
    public void push(Frame frame) {
        super.push(frame);

        if (!frame.equals(Frame.empty()) && !frame.equals(Frame.EOF())) {
            applyEffects(frame);
        }

        if (framesPool.size() > 0) {
            feedMeIfNotDraining();
        }

        if (!frame.equals(Frame.empty())) {
            hasData(); //effector always has same frame on output
        }
    }

    private void applyEffects(Frame frame) {
        for (IAudioEffect effect : audioEffects) {
            Pair<Long, Long> segment = effect.getSegment();

            if (segment == null || (segment.left <= frame.getSampleTime() &&
                    segment.right >= frame.getSampleTime())) {

                effect.applyEffect(frame.getByteBuffer(), frame.getSampleTime());
//                frame.setLength(frame.getByteBuffer().limit());

                mediaFormat = effect.getMediaFormat();
            }
        }
    }

    @Override
    public void checkIfOutputQueueHasData() {}

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {}

    @Override
    public void pull(Frame frame) {}

    @Override
    public Frame findFreeFrame() {
        if (framesPool.size() > 0) {
            Iterator<Frame> iterator = framesPool.iterator();
            Frame frame = iterator.next();
            framesOutput.add(frame);
            iterator.remove();
            return frame;
        }
        return null;
    }

    @Override
    public Frame getFrame() {
        Frame frame = null;
        if (framesOutput.size() > 0) {
            Iterator<Frame> iterator = framesOutput.iterator();
            frame = iterator.next();
            framesPool.add(frame);
            iterator.remove();
        }

        if (framesPool.size() > 0) {
            feedMeIfNotDraining();
        }
        return frame;
    }

    private void outputFormatChanged() {
        getOutputCommandQueue().queue(Command.OutputFormatChanged, 0);
    }

    @Override
    public void setInputMediaFormat(MediaFormat mediaFormat) {
        outputMediaFormat = mediaFormat;
        outputFormatChanged();
    }

    @Override
    public boolean isLastFile() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void start() {
        setState(PluginState.Normal);
    }

    @Override
    public void stop() {
        setState(PluginState.Paused);
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    @Override
    public void configure() {}

    @Override
    public void setOutputSurface(ISurface surface) {}

    @Override
    public ISurface getSurface() {
        return null;
    }

    @Override
    public void waitForSurface(long pts) {}

    @Override
    public void close() throws IOException {
        //TODO: remove mediacodec dependency since not used
    }

    @Override
    public MediaFormat getOutputMediaFormat() {
        return mediaFormat;
    }

    public void reInitInputCommandQueue() {
        getInputCommandQueue().queue.clear();
        feedMeIfNotDraining();
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }
}
