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

import org.m4m.IVideoEffect;
import org.m4m.domain.graphics.TextureRenderer;
import java.io.IOException;
import java.util.LinkedList;

public class VideoEffector extends MediaCodecPlugin {
    private IAndroidMediaObjectFactory factory = null;
    private LinkedList<IVideoEffect> videoEffects = new LinkedList<IVideoEffect>();
    private ISurface encoderSurface;
    private IEffectorSurface internalSurface;
    private IEffectorSurface internalOverlappingSurface;

    private float[] matrix = new float[16];
    IPreview previewRender;
    IFrameBuffer frameBuffer;
    private PreviewContext previewContext;
    private Resolution resolution;

    private long previousPts = 0L;
    private final long deltaPts = 1000000L;
    private boolean awaitCurrentFileSample = false;
    private Frame saved;
    private boolean firstDecoderConnected = false;
    private int skipTransitionEffect = 0;
    private boolean previewRenderMode = false;
    private int outputAngle = 0;
    private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;

    private int timeScale = 1;
    private FileSegment segment = new FileSegment(0l, 0l);

    public VideoEffector(IMediaCodec mediaCodec, IAndroidMediaObjectFactory factory) {
        super(mediaCodec);
        this.factory = factory;
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

    public LinkedList<IVideoEffect> getVideoEffects() {
        return videoEffects;
    }

    @Override
    protected void feedMeIfNotDraining() {
        feedMeIfNotDraining(getTrackId());
    }

    protected void hasData(Frame frame) {
        if (saved == null) {
            if(frameCount < 2) {
                renderSurfaceForFrame(frame);
            }
            super.hasData();
        }
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
        applyEffects(frame);

        frame.setSampleTime(newTime);

        updatePreview();
        hasData(frame);
    }

    private void configureNextData(){
        if (awaitCurrentFileSample){
            setTransitionTrack();
            exchangeTransitionSurface();
            awaitCurrentFileSample = false;
            skipTransitionEffect++;
        }

        feedMeIfNotDraining();
    }

    private void exchangeTransitionSurface(){
        IEffectorSurface tmp = internalOverlappingSurface;
        internalOverlappingSurface = internalSurface;
        internalSurface = tmp;
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

    private IVideoEffect applyEffects(Frame frame) {
        boolean effectWasApplied = false;
        IVideoEffect appliedEffect = null;

        // in case we are waiting for internalOverlappingSurface and can't await
        // new image from internal surface
        if (saved != null)
            return appliedEffect;
        prepareSurface();

        long pts = frame.getSampleTime();
        for (IVideoEffect effect : videoEffects) {
            FileSegment segment = effect.getSegment();

            if (isFrameInSegment(pts, segment)) {

                if ((pts - previousPts) > deltaPts){
                    FileSegment oldSegment = effect.getSegment();
                    effect.setSegment(new FileSegment(pts, pts + oldSegment.pair.right - oldSegment.pair.left));
                }

                bindFB();

                effect.applyEffect(getInputIndex(), pts, matrix);
                outputAngle = effect.getAngle();

                unbindFB();

                effectWasApplied = true;
                appliedEffect = effect;
                break;
            }
        }
        previousPts = pts;

        if (!effectWasApplied) {
            bindFB();
            internalSurface.drawImage(getInputIndex(), matrix, fillMode);
            unbindFB();
        }

        renderOntoEncoderContext(fillMode);

        return appliedEffect;
    }

    private void prepareSurface() {
        // TODO: questionable???
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

    private int getSecondInputIndex() {
        if (previewRender == null) {
            return internalOverlappingSurface.getSurfaceId();
        }
        return previewContext.previewTextureId;
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

//        Logger.getLogger("AMP").info("Frame VideoEffector savedFrame sampletime: " + savedFrame.getSampleTime());
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
        internalOverlappingSurface = factory.createEffectorSurface();

        for (IVideoEffect effect : videoEffects) {
            effect.start();
        }
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
            //restoring fit to context in effect
            for (IVideoEffect videoEffect : videoEffects) {
                //videoEffect.setInputResolution(this.resolution);
                videoEffect.setFillMode(TextureRenderer.FillMode.PreserveAspectFit);
            }

            //switch to internal rendering
            previewRender.setListener(null);
            //in case of no any frames reached transcoding pipe
            previewRender.requestRendering();
            previewRender = null;
        }

       /* if (previewTranscode != null) {
            previewTranscode = null;
        }
*/
        if (frameBuffer != null) {
            frameBuffer.release();
            frameBuffer = null;
        }

        if (internalSurface != null) {
            internalSurface.release();
            internalSurface = null;
        }

        if (internalOverlappingSurface != null) {
            internalOverlappingSurface.release();
            internalOverlappingSurface = null;
        }

        /*if (encoderSurface != null) {
            encoderSurface.release();
            encoderSurface = null;
        }*/
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

    public ISurface getOverlappingSurface() {
        if (internalOverlappingSurface == null) {
            throw new RuntimeException("Effector overlapping surface not set.");
        }
        return internalOverlappingSurface;
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

        //prevent effect stretch logic since will use 1 to 1 copy by frame buffer
        if (previewRender != null) {
            //resolution = new Resolution(0, 0);
            for (IVideoEffect videoEffect : videoEffects) {
                videoEffect.setFillMode(TextureRenderer.FillMode.PreserveSize);
            }
        }

        super.setInputResolution(resolution);

        for (IVideoEffect videoEffect : videoEffects) {
            videoEffect.setInputResolution(resolution);
        }
    }

    public IPreview getPreview() {
        return previewRender;
    }

    public boolean isConnectedToFirstDecoder() {
        return firstDecoderConnected;
    }

    public void setConnectionToFirstDecoder(boolean connection) {
        firstDecoderConnected = connection;
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
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

    public void setTimeScale(int timeScale) {
        this.timeScale = timeScale;
    }

    public void setTimeScalerSegment(FileSegment segment) {
        this.segment = segment;
    }
}

