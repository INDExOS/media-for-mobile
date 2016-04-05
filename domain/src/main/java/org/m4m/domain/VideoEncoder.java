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

import org.m4m.VideoFormat;

public class VideoEncoder extends Encoder implements ITransform {
    public VideoEncoder(IMediaCodec mediaCodec) {
        super(mediaCodec);
    }

    @Override
    public void setMediaFormat(MediaFormat inputMediaFormat) {
        this.mediaFormat = inputMediaFormat;
        getVideoFormat().setColorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
    }

    public void setBitRateInKBytes(int bitRate) {
        getVideoFormat().setVideoBitRateInKBytes(bitRate);
    }

    public int getBitRateInKBytes() {
        return getVideoFormat().getVideoBitRateInKBytes();
    }

    public void setFrameRate(int frameRate) {
        getVideoFormat().setVideoFrameRate(frameRate);
    }

    public int getFrameRate() {
        return getVideoFormat().getVideoFrameRate();
    }

    public void setIFrameInterval(int iFrameInterval) {
        getVideoFormat().setVideoIFrameInterval(iFrameInterval);
    }

    public int getIFrameInterval() {
        return getVideoFormat().getVideoIFrameInterval();
    }

    private VideoFormat getVideoFormat() {
        return (VideoFormat) mediaFormat;
    }

    @Override
    public boolean isLastFile() {
        return false;
    }

    @Override
    public void setOutputSurface(ISurface surface) {}

    @Override
    public void waitForSurface(long pts) {}

    @Override
    public void drain(int bufferIndex) {
        if (state != PluginState.Normal) return;

        getInputCommandQueue().clear();
        mediaCodec.signalEndOfInputStream();
    }

    @Override
    protected void feedMeIfNotDraining() {
        if (frameCount < 2) {
            if (state != PluginState.Draining && state != PluginState.Drained ) {

                Pair<Command, Integer> command = getInputCommandQueue().first();

                if (command == null || command.left != Command.NeedData ) {
                    getInputCommandQueue().queue(Command.NeedData, getTrackId());
                }
            }
        }
    }

    @Override
    public void push(Frame frame) {
        //Logger.getLogger("AMP").info("VideoEncoder frame gets pushed: pts=" + frame.getSampleTime() + ", trackId=" + frame.getTrackId() + ", flags=" + frame.getFlags() + ", length=" + frame.getLength());

        //Logger.getLogger("AMP").info("VideoEncoder queue size: " + getInputCommandQueue().size() + " frameCount = " + frameCount);
        super.push(frame);
    }

    @Override
    public void notifySurfaceReady(ISurface surface) {

        if (frameCount < 2) {
            surface.swapBuffers();
            frameCount++;
        }

        //Logger.getLogger("AMP").info("VideoEncoder frameCount++ = " + frameCount);
    }

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {
        super.releaseOutputBuffer(outputBufferIndex);
        frameCount--;

        //Logger.getLogger("AMP").info("VideoEncoder frameCount-- = " + frameCount);
    }
}
