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

import org.m4m.AudioFormat;
import org.m4m.IProgressListener;
import org.m4m.VideoFormat;

import java.io.IOException;
import java.util.ArrayList;

public class MuxRender extends Render {

    private final IMediaMuxer notReadyMuxer;
    private IMediaMuxer muxer;
    private final IProgressListener progressListener;
    private final ProgressTracker progressTracker;
    private int connectedPluginsCount = 0;
    private int tracksCount = 0;
    private int drainCount = 0;
    private int videoTrackId = -1;
    private int audioTrackId = -1;
    private ArrayList<IPluginOutput> releasersList = new ArrayList();
    private FrameBuffer frameBuffer = new FrameBuffer(0);
    private boolean zeroFramesReceived = true;

    public MuxRender(IMediaMuxer muxer, IProgressListener progressListener, ProgressTracker progressTracker) {
        super();
        this.notReadyMuxer = muxer;
        this.progressListener = progressListener;
        this.progressTracker = progressTracker;
    }

    @Override
    protected void initInputCommandQueue() {
    }

    @Override
    public void push(Frame frame) {
        //Logger.getLogger("AMP").info("Render frame presentationTimeUs = " + frame.getSampleTime());

        if(zeroFramesReceived == true) {
            zeroFramesReceived = false;
        }

        if (frameBuffer.areAllTracksConfigured()) {
            writeBufferedFrames();
            writeSampleData(frame);
            feedMeIfNotDraining();
        } else {
            frameBuffer.push(frame);
            getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        }
    }


    public void pushWithReleaser(Frame frame, IPluginOutput releaser) {
        //Logger.getLogger("AMP").info("Render frame presentationTimeUs = " + frame.getSampleTime());

        if(zeroFramesReceived == true) {
            zeroFramesReceived = false;
        }

        if (frameBuffer.areAllTracksConfigured()) {
            writeBufferedFrames();
            writeSampleData(frame);
            releaser.releaseOutputBuffer(frame.getBufferIndex());
            feedMeIfNotDraining();
        } else {
            frameBuffer.push(frame);
            releasersList.add(releaser);
            getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        }
    }

    private void writeBufferedFrames() {
        while (frameBuffer.canPull()) {
            Frame bufferedFrame = frameBuffer.pull();
            writeSampleData(bufferedFrame);
            releasersList.get(0).releaseOutputBuffer(bufferedFrame.getBufferIndex());
            releasersList.remove(0);
        }
    }

    private void writeSampleData(Frame frame) {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.size = frame.getLength();

        muxer.writeSampleData(frame.getTrackId(), frame.getByteBuffer(), bufferInfo);

        progressTracker.track(frame.getSampleTime());
        progressListener.onMediaProgress(progressTracker.getProgress());
    }

    @Override
    public void drain(int bufferIndex) {
        drainCount++;

        if (drainCount == connectedPluginsCount) {

            closeRender();

            progressListener.onMediaStop();
            if (onStopListener != null) {
                onStopListener.onStop();
            }
            getInputCommandQueue().clear();
            setState(PluginState.Drained);
        }
        //TODO: no unit tests yet, please help
        if (frameBuffer.areAllTracksConfigured()) {
            feedMeIfNotDraining();
        } else {
            getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        }
    }

    @Override
    public void configure() {
        connectedPluginsCount++;
        getInputCommandQueue().queue(Command.NeedInputFormat, 0);
        frameBuffer.addTrack();
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        int trackIndex = notReadyMuxer.addTrack(mediaFormat);
        if (mediaFormat instanceof VideoFormat) videoTrackId = trackIndex;
        if (mediaFormat instanceof AudioFormat) audioTrackId = trackIndex;

        frameBuffer.configure(tracksCount);
        tracksCount++;
    }

    @Override
    public int getTrackIdByMediaFormat(MediaFormat mediaFormat) {
        if (mediaFormat instanceof VideoFormat) {
            if (videoTrackId == -1) throw new IllegalStateException("Video track not initialised");
            return videoTrackId;
        } else if (mediaFormat instanceof AudioFormat) {
            if (audioTrackId == -1) throw new IllegalStateException("Audio track not initialised");
            return audioTrackId;
        }

        return -1;
    }

    @Override
    public void start() {
        if (connectedPluginsCount == tracksCount) {

            notReadyMuxer.start();
            muxer = notReadyMuxer;

            for (int track = 0; track < tracksCount; track++) {
                feedMeIfNotDraining();
            }
        }
    }

    @Override
    public boolean canConnectFirst(IOutputRaw connector) {
        return true;
    }

    @Override
    public void fillCommandQueues() {}

    public void close() throws IOException {
        closeRender();
    }

    private void closeRender() {
        if (muxer != null) {
            try {
                muxer.stop();
                muxer.release();
                muxer = null;
            }
            catch (Exception e) {
                if(zeroFramesReceived == false) {

                    throw new RuntimeException("Failed to close the render.", e);
                }
            }
        }
    }
}
