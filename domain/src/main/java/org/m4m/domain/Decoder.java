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
import java.util.ArrayList;

abstract class Decoder extends MediaCodecPlugin implements IFrameAllocator, ITransform {
    //Logger log = Logger.getLogger(getClass().getSimpleName());

    private final MediaFormatType mediaFormatType;
    private ISurface outputSurface;
    private ISurfaceWrapper clearOutputSurface; // Wrapped android surface without any methods
    private ArrayList<Long> framesPTSToSkip = new ArrayList<Long>();

    public Decoder(IMediaCodec mediaCodec, MediaFormatType mediaFormatType) {
        super(mediaCodec);
        this.mediaFormatType = mediaFormatType;
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    @Override
    public void setOutputSurface(ISurface surface) {
        this.outputSurface = surface;
        this.clearOutputSurface = surface.getCleanObject();
    }

    public void setOutputSurface(ISurfaceWrapper surface) {
        this.outputSurface = null;
        this.clearOutputSurface = surface;
    }

    @Override
    public void push(Frame frame) {
        super.push(frame);
        //log.info("Decoder gets frame pts=" + frame.getSampleTime() + ", trackId=" + frame.getTrackId() + ", flags=" + frame.getFlags() + ", length=" + frame.getLength());
        mediaCodec.queueInputBuffer(frame.getBufferIndex(), 0, frame.getLength(), frame.getSampleTime(), frame.getFlags());

        // Allowing to pass only needed frames (example - cut segments)
        if (frame.isSkipFrame()) {
            framesPTSToSkip.add(frame.getSampleTime());
        }

        getOutputBufferIndex();
        feedMeIfNotDraining();
    }

    @Override
    public void pull(Frame frame) {
        IMediaCodec.BufferInfo info = new IMediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, timeout);
        //log.info("" + mediaFormatType + ", dequeueOutputBuffer " + outputBufferIndex);
        if (outputBufferIndex >= 0) {
            ByteBuffer[] buffers = mediaCodec.getOutputBuffers();

            frame.setSampleTime(info.presentationTimeUs);
            frame.setFlags(info.flags);
            frame.setLength(info.size);

            ByteBuffer fromByteBuffer = buffers[outputBufferIndex].duplicate();
            fromByteBuffer.position(0);

            if (frame.getLength() >= 0) {
                fromByteBuffer.limit(frame.getLength());
            }

            frame.getByteBuffer().position(0);
            frame.getByteBuffer().put(buffers[outputBufferIndex]);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            //log.info("releaseOutputBuffer " + outputBufferIndex);
        } else {
            if (outputBufferIndex == IMediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //...
            }
            if (outputBufferIndex == IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //...
            }
        }
    }

    @Override
    public boolean isLastFile() {
        return false;
    }

    @Override
    public void waitForSurface(long pts) {
        outputSurface.awaitAndCopyNewImage();
        outputSurface.drawImage();
        outputSurface.setPresentationTime(pts * 1000);
    }

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {
        boolean doRender = false;
        if (clearOutputSurface != null) {
            doRender = true;
        }
        mediaCodec.releaseOutputBuffer(outputBufferIndex, doRender);
        //log.info("releaseOutputBuffer " + outputBufferIndex);
    }

    @Override
    public void stop() {
        super.stop();
        outputBufferInfos.clear();
        outputBufferIndexes.clear();
        inputBufferIndexes.clear();
        getOutputCommandQueue().clear();
    }

    @Override
    public void configure() {
        mediaCodec.configure(mediaFormat, clearOutputSurface, 0);
    }

    @Override
    public ISurface getSurface() {
        return outputSurface;
    }

    @Override
    public void drain(int bufferIndex) {
        getInputCommandQueue().clear();
        mediaCodec.queueInputBuffer(bufferIndex, 0, 0, 0, IMediaCodec.BUFFER_FLAG_END_OF_STREAM);
    }

    @Override
    public MediaFormatType getMediaFormatType() {
        return mediaFormatType;
    }

    @Override
    public void recreate() {
        mediaCodec.recreate();
    }

    @Override
    protected void hasData() {
        super.hasData();
        getOutputCommandQueue().queue(Command.NextPair, 0);
    }

    private void outputFormatChanged() {
        getOutputCommandQueue().queue(Command.OutputFormatChanged, 0);
    }

    private int addOutputBuffer(int outputBufferIndex, IMediaCodec.BufferInfo bufferInfo) {
        if (!framesPTSToSkip.contains(bufferInfo.presentationTimeUs) || bufferInfo.isEof()) {
            outputBufferIndexes.add(outputBufferIndex);
            outputBufferInfos.add(bufferInfo);

            return outputBufferIndex;
        }

        framesPTSToSkip.remove(bufferInfo.presentationTimeUs);

        if (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
        }
        return IMediaCodec.INFO_TRY_AGAIN_LATER;
    }

    @Override
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

            outputBufferIndex = addOutputBuffer(outputBufferIndex, bufferInfo);
//            outputBufferIndexes.add(outputBufferIndex);
//            outputBufferInfos.add(bufferInfo);
        }

        if (outputBufferIndex >= 0) {
            hasData();
        }

        if (bufferInfo.isEof() && state != PluginState.Drained) {
            setState(PluginState.Draining);
            getOutputCommandQueue().queue(Command.EndOfFile, outputTrackId);
        }

        if (outputBufferIndex == IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            this.outputMediaFormat = mediaCodec.getOutputFormat();
            outputFormatChanged();
        }

        return outputBufferIndex;
    }

}
