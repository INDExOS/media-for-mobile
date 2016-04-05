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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public abstract class MediaCodecPlugin extends Plugin {
    protected int timeout = 10;

    protected final IMediaCodec mediaCodec;
    //protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    protected Queue<Integer> outputBufferIndexes = new LinkedList<Integer>();
    protected Queue<Integer> inputBufferIndexes = new LinkedList<Integer>();
    protected Queue<IMediaCodec.BufferInfo> outputBufferInfos = new LinkedList<IMediaCodec.BufferInfo>();
    protected MediaFormat outputMediaFormat = null;
    protected ByteBuffer[] inputBuffers = null;
    protected int outputTrackId;

    protected int frameCount;

    protected HashMap<Integer, Frame> bufferIndexToFrame = new HashMap<Integer, Frame>();

    public MediaCodecPlugin(IMediaCodec mediaCodec) {
        this.mediaCodec = mediaCodec;
    }

    @Override
    public void checkIfOutputQueueHasData() {
        getOutputBufferIndex();
    }

    @Override
    protected void feedMeIfNotDraining() {
        if (state != PluginState.Draining && state != PluginState.Drained) {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(timeout);
            if (inputBufferIndex >= 0) {
                inputBufferIndexes.add(inputBufferIndex);
                super.feedMeIfNotDraining();
            } else {
                if (inputBufferIndexes.size() > 0) {
                    Pair<Command, Integer> command = getInputCommandQueue().first();
                    if (command == null || command.left != Command.NeedData) {
                        super.feedMeIfNotDraining();
                    }
                }
            }
        }
    }

    @Override
    public Frame getFrame() {
        feedMeIfNotDraining();

        Integer outputBufferIndex = outputBufferIndexes.poll();
        IMediaCodec.BufferInfo outputBufferInfo = outputBufferInfos.poll();
        if ((state == PluginState.Draining || state == PluginState.Drained) && outputBufferIndex == null) {
            if (getOutputBufferIndex() >= 0) {
                outputBufferIndex = outputBufferIndexes.poll();
                outputBufferInfo = outputBufferInfos.poll();
            } else {
                return Frame.EOF();
            }
        }

        if (outputBufferIndex == null) {
            return Frame.empty();
        }

        while (isStatusToSkip(outputBufferIndex) && outputBufferIndexes.size() > 0) {
            outputBufferIndex = outputBufferIndexes.poll();
            outputBufferInfo = outputBufferInfos.poll();
        }

        ByteBuffer outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];

        Frame frame;

        if (bufferIndexToFrame.containsKey(outputBufferIndex)) {
            frame = bufferIndexToFrame.get(outputBufferIndex);

            frame.set(outputBuffer, outputBufferInfo.size, outputBufferInfo.presentationTimeUs, outputBufferIndex, outputBufferInfo.flags, outputTrackId);
        } else {
            frame = new Frame(outputBuffer, outputBufferInfo.size, outputBufferInfo.presentationTimeUs, outputBufferIndex, outputBufferInfo.flags, outputTrackId);

            bufferIndexToFrame.put(outputBufferIndex, frame);

            Logger.getLogger("AMP").info("New frame allocated for buffer " + outputBufferIndex);
        }

        checkIfOutputQueueHasData();

        // Workaround: after signalEndOfInputStream encoder may generate EoF frame with large negative pts
        if (frame.equals(Frame.EOF()) && frame.getSampleTime() < -1) {
            frame.setSampleTime(0);
        }

        //Logger.getLogger("AMP").info("Encoder returns output frame pts=" + frame.getSampleTime() + ", trackId=" + frame.getTrackId() + ", flags=" + frame.getFlags() + ", length=" + frame.getLength());
        return frame;
    }

    protected int getOutputBufferIndex() {
        //log.info("+ dequeue output buffer");
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeout);

        if (state == PluginState.Draining && outputBufferIndex == IMediaCodec.INFO_TRY_AGAIN_LATER) {
            state = PluginState.Drained;
        }

        if (outputBufferIndex != IMediaCodec.INFO_TRY_AGAIN_LATER &&
                outputBufferIndex != IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            //ampLogger.info(this.getClass() + " : flags: " + bufferInfo.flags);
            outputBufferIndexes.add(outputBufferIndex);
            outputBufferInfos.add(bufferInfo);
        }

        if (outputBufferIndex >= 0) {
            hasData();
        }

        if (bufferInfo.isEof() && state != PluginState.Drained) {
            getInputCommandQueue().clear();
            setState(PluginState.Draining);
        }

        if (outputBufferIndex == IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            this.outputMediaFormat = mediaCodec.getOutputFormat();
            outputFormatChanged();
        }

        return outputBufferIndex;
    }

    protected boolean isStatusToSkip(Integer outputBufferIndex) {
        return outputBufferIndex == IMediaCodec.INFO_OUTPUT_BUFFERS_CHANGED
                || outputBufferIndex == IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
    }

    private void outputFormatChanged() {
        getOutputCommandQueue().queue(Command.OutputFormatChanged, 0);
        //ampLogger.info(getClass() + ": OutputFormatChanged");
    }

    protected void hasData() {
        getOutputCommandQueue().queue(Command.HasData, 0);
        //ampLogger.info(getClass() + ": HasData");
    }

    @Override
    public void drain(int bufferIndex) {
        super.drain(bufferIndex);
    }

    public Frame findFreeFrame() {

        if (this.state == PluginState.Draining || this.state == PluginState.Drained) {
            return Frame.EOF();
        }

        if (inputBufferIndexes.size() == 0) {
            return null;
        }
        int inputBufferIndex = inputBufferIndexes.poll();
        return new Frame(inputBuffers[inputBufferIndex], 0, 0, inputBufferIndex, 0, 0);
    }

    @Override
    public void setOutputTrackId(int trackId) {
        this.outputTrackId = trackId;
    }

    @Override
    public MediaFormat getOutputMediaFormat() {
        return mediaCodec.getOutputFormat();
    }

    @Override
    public void fillCommandQueues() {
        if (state != PluginState.Normal) {
            return;
        }

        checkIfOutputQueueHasData();
        feedMeIfNotDraining();
    }

    @Override
    public void start() {
        mediaCodec.start();
        inputBuffers = mediaCodec.getInputBuffers();
        setState(PluginState.Normal);
    }

    @Override
    public void stop() {
        setState(PluginState.Paused);
        mediaCodec.stop();
    }

    @Override
    public void close() throws IOException {
        mediaCodec.release();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setInputMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }
}
