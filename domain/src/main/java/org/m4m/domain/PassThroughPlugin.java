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

public class PassThroughPlugin extends Plugin implements IFrameAllocator {
    Frame frame;
    int outputTrackId = 0;
    private boolean frameDelivered = true;
    private MediaFormatType mediaFormatType;

    public PassThroughPlugin(int size, MediaFormatType mediaFormatType) {
        this.frame = new Frame(ByteBuffer.allocate(size), size, 0, 0, 0, 0);
        this.mediaFormatType = mediaFormatType;
        getOutputCommandQueue().queue(Command.OutputFormatChanged, getTrackId());
        //ampLogger.info(getClass() + ": OutputFormatChanged");
    }

    @Override
    protected void initInputCommandQueue() {
        feedMeIfNotDraining();
    }

    @Override
    public Frame findFreeFrame() {
        return frame;
    }

    @Override
    public MediaFormatType getMediaFormatType() {
        return this.mediaFormatType;
    }

    @Override
    public void drain(int bufferIndex) {
        super.drain(bufferIndex);
        getOutputCommandQueue().queue(Command.EndOfFile, 0);
        //ampLogger.info(getClass() + ": HasData");
    }

    @Override
    public void push(Frame frame) {
        super.push(frame);
        if (!frame.equals(Frame.EOF())) {
            frameDelivered = false;
            this.frame = frame;
            getOutputCommandQueue().queue(Command.HasData, 0);  // TODO: Render class expect 0 as universal track ID
            //ampLogger.info(getClass() + ": HasData");
        }
    }

    @Override
    public void pull(Frame frame) {
        frame.copyInfoFrom(getFrame());
    }

    @Override
    public Frame getFrame() {
        if (!frameDelivered) {
            frameDelivered = true;
            feedMeIfNotDraining();
            frame.setTrackId(outputTrackId); // track index may be different on input and output
            return frame;
        }
        if (state == PluginState.Draining) {
            return Frame.EOF();
        }
        throw new UnsupportedOperationException("Attempt to pull a frame twice.");
    }

    @Override
    public boolean isLastFile() {
        return false;
    }

    @Override
    public void skipProcessing() {
        getInputCommandQueue().queue(Command.NextPair, getTrackId());
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void configure() {
    }

    public void checkIfOutputQueueHasData() {
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        //inputMediaFormats.add(mediaFormat);
        this.mediaFormat = mediaFormat;
    }

    @Override
    public void setOutputSurface(ISurface surface) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setOutputTrackId(int trackId) {
        this.outputTrackId = trackId;
    }

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {
    }

    @Override
    public ISurface getSurface() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void waitForSurface(long pts) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void fillCommandQueues() {
    }

    @Override
    public void close() throws IOException {

    }
}
