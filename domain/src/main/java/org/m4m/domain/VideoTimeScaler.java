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

import org.m4m.domain.graphics.TextureRenderer;

import java.io.IOException;

public class VideoTimeScaler extends MediaCodecPlugin {
    private IAndroidMediaObjectFactory factory = null;

    private ISurface encoderSurface;
    private IEffectorSurface internalSurface;

    private float[] matrix = new float[16];
    IPreview previewRender;
    IFrameBuffer frameBuffer;
    private PreviewContext previewContext;
    private Resolution resolution;

    private boolean awaitCurrentFileSample = false;
    private boolean previewRenderMode = false;
    private int outputAngle = 0;
    private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;
    private IPreviewTranscode previewTranscode;

    private int timeScale;
    private FileSegment segment = new FileSegment(0l, 0l);

    public VideoTimeScaler(IMediaCodec mediaCodec, IAndroidMediaObjectFactory factory, int timeScale, FileSegment segment) {
        super(mediaCodec);
        this.factory = factory;
        this.timeScale = timeScale;
        this.segment = segment;
    }

    @Override
    protected void initInputCommandQueue() {
        getInputCommandQueue().queue(Command.NeedInputFormat, getTrackId());
    }

    public void reInitInputCommandQueue() {
        getInputCommandQueue().queue.clear();
        getInputCommandQueue().queue(Command.NeedInputFormat, getTrackId());
    }

    public void enablePreview(IPreview preview) {
        previewRender = preview;
    }

    @Override
    protected void feedMeIfNotDraining() {
        feedMeIfNotDraining(getTrackId());
    }

    protected void hasData(Frame frame) {
        if(frameCount < 2) {
            renderSurfaceForFrame(frame);
        }
        super.hasData();
    }

    private void renderSurfaceForFrame(Frame frame) {
        if (!previewRenderMode) {
            // Workaround. We should do that in effector-encoder handler.
            encoderSurface.setPresentationTime(1000 * frame.getSampleTime());
            encoderSurface.swapBuffers();
            frameCount++;
        } /*else {
            previewTranscode.renderSurfaceFromDecoderSurface(getInputIndex(), getEffectorSurface());
            previewTranscode.setPresentationTime(frame.getSampleTime());
        }*/
    }

    @Override
    public void push(Frame frame) {
        if (!frame.equals(Frame.empty())) {
            applyEffectorOperations(frame);
        } else {
            configureNextData();
        }
    }

    private long frames = 0;

    private void applyEffectorOperations(Frame frame) {
        initFrameBuffer();

        long time = frame.getSampleTime();
        long newTime;
        if (time < segment.pair.left) {
            newTime = time;
        } else {
            newTime = segment.pair.left;
            if (time < segment.pair.right) {
                frames++;
                newTime += (time - segment.pair.left) / timeScale;
                if (frames % timeScale != 0) {
                    feedMeIfNotDraining();
                    return;
                }
            } else {
                newTime += (segment.pair.right - segment.pair.left) / timeScale + (time - segment.pair.right);
            }
        }
        apply();
        frame.setSampleTime(newTime);

        updatePreview();
        hasData(frame);
    }

    private void configureNextData(){
        if (awaitCurrentFileSample){
            setTransitionTrack();
            awaitCurrentFileSample = false;
        }
        feedMeIfNotDraining();
    }

    private void setTransitionTrack(){
        if (getTrackId() == 0) setTrackId(1);
        else setTrackId(0);
    }


    private void needData(int trackId) {
        if (state != PluginState.Draining && state != PluginState.Drained) {
            getInputCommandQueue().queue(Command.NeedData, trackId);
        }
    }

    private void apply() {
        prepareSurface();
        bindFB();
        internalSurface.drawImage(getInputIndex(), matrix, fillMode);
        unbindFB();

        renderOntoEncoderContext(fillMode);
    }

    private void prepareSurface() {
        if (previewRender == null) {
            internalSurface.awaitAndCopyNewImage();
            internalSurface.getTransformMatrix(matrix);
        }
        else {
            matrix = previewContext.previewTexture.getTransformMatrix();
        }
    }

    private void feedMeIfNotDraining(int trackId) {
        if (frameCount < 2) {
            needData(trackId);
        }
    }

    private boolean isFrameInSegment(long pts, FileSegment segment) {
        if ((segment.pair.left <= pts && pts <= segment.pair.right) || (segment.pair.left == 0 && segment.pair.right == 0)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFrameBeforeSegment(long pts, FileSegment segment) {
        if (segment.pair.left > pts) {
            feedMeIfNotDraining();
            return true;
        } else {
            return false;
        }
    }

    private void updatePreview() {
        if (previewRender == null) {
            return;
        }
        previewRender.renderSurfaceFromFrameBuffer(frameBuffer.getTextureId());
    }

    private void renderOntoEncoderContext(TextureRenderer.FillMode fillMode) {
        if (frameBuffer == null) {
            return;
        }

        // should draw here to emulate drawing in usual transcoding when we are drawing to internal
        // surface and when it
        internalSurface.drawImage2D(frameBuffer.getTextureId(), matrix, outputAngle, fillMode);
    }

    private int getInputIndex() {
        if (previewRender == null) {
            return internalSurface.getSurfaceId();
        }
        return previewContext.previewTextureId;
    }

    public int getOutputPreviewIndex() {
        return internalSurface.getSurfaceId();
    }

    private void initFrameBuffer() {
        if (previewRender != null && frameBuffer == null) {
            frameBuffer = factory.createFrameBuffer();
            frameBuffer.setResolution(this.resolution);
        }
    }

    private void unbindFB() {
        if (previewRender != null) {
            frameBuffer.unbind();
        }
    }

    private void bindFB() {
        if (frameBuffer != null) {
            frameBuffer.bind();
        }
    }

    @Override
    public void checkIfOutputQueueHasData() {
    }

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {
        frameCount--;
        feedMeIfNotDraining();
    }

    @Override
    public void pull(Frame frame) {}

    @Override
    public Frame getFrame() {
        if (state == PluginState.Drained) {
            throw new RuntimeException("Out of order operation.");
        }

        return new Frame(null, 1, 1, 0, 0, 0);
    }

    @Override
    public boolean isLastFile() {
        return false;
    }

    @Override
    public void start() {
        initInputCommandQueue();

        if (encoderSurface == null && !previewRenderMode) {
            throw new RuntimeException("Encoder surface not set.");
        }
        else if (encoderSurface != null) {
            encoderSurface.makeCurrent();
        }

        internalSurface = factory.createEffectorSurface();
    }

    @Override
    public void close() throws IOException {
        stop();
        super.close();
    }

    @Override
    public void stop() {
        super.stop();
        if (previewRender != null) {
            //switch to internal rendering
            previewRender.setListener(null);
            //in case of no any frames reached transcoding pipe
            previewRender.requestRendering();
            previewRender = null;
        }

        if (frameBuffer != null) {
            frameBuffer.release();
            frameBuffer = null;
        }

        if (internalSurface != null) {
            internalSurface.release();
            internalSurface = null;
        }
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {}

    @Override
    public void setOutputSurface(ISurface surface) {
        encoderSurface = surface;
    }

    @Override
    public ISurface getSurface() {
        if (internalSurface == null) {
            throw new RuntimeException("Effector surface not set.");
        }
        return internalSurface;
    }

    @Override
    public void waitForSurface(long pts) {}

    @Override
    public void configure() {
    }

    @Override
    public boolean canConnectFirst(IOutputRaw connector) {
        return false;
    }

    /*
     * Input Surface available callback, render effect will create it's own surface
     */
    public void onSurfaceAvailable(final ISurfaceListener listener) {
        if (previewRender == null) {
            listener.onSurfaceAvailable(factory.getCurrentEglContext());
            return;
        }

        if(previewRenderMode == true) {
            //previewContext = previewTranscode.getSharedContext();
            listener.onSurfaceAvailable(previewContext.eglContext);
        } else {
            previewContext = previewRender.getSharedContext();
            //___ATTENTION__
            //don't change order
            listener.onSurfaceAvailable(previewContext.eglContext);
            //___ATTENTION__
            //internalSurface.setSurfaceTexture(previewContext.previewTexture);

            //internalSurface.setSurfaceId(previewContext.previewTextureId);
        }
    }

    @Override
    public void setInputResolution(Resolution resolution) {
        this.resolution = resolution;
        super.setInputResolution(resolution);
    }

    public IPreview getPreview() {
        return previewRender;
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public void setPreviewRenderMode(boolean previewRenderMode) {
        this.previewRenderMode = previewRenderMode;
    }

    public void setPreviewTranscode(IPreviewTranscode previewTranscode) {
        this.previewTranscode = previewTranscode;
    }

    public IEffectorSurface getEffectorSurface() {
        if (internalSurface == null) {
            throw new RuntimeException("Effector surface not set.");
        }
        return internalSurface;
    }

    @Override
    public void drain(int bufferIndex) {
        getInputCommandQueue().clear();
        getOutputCommandQueue().queue(Command.EndOfFile, 0);
    }

    public void setFillMode(TextureRenderer.FillMode fillMode) {
        this.fillMode = fillMode;
    }

    public TextureRenderer.FillMode getFillMode() {
        return fillMode;
    }

}

