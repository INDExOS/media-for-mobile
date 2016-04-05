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

package org.m4m.android;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import org.m4m.IVideoEffect;
import org.m4m.domain.AudioRender;
import org.m4m.domain.FileSegment;
import org.m4m.domain.IEffectorSurface;
import org.m4m.domain.IPreviewTranscode;
import org.m4m.domain.ISurfaceListener;
import org.m4m.domain.Resolution;

import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.TextureRenderer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PreviewTranscode implements IPreviewTranscode, GLSurfaceView.Renderer{

    final static String TAG = "PreviewTranscode";

    private GLSurfaceView glView;
    private IEglUtil eglUtil;
    private TextureRenderer textureRender;
    private EGLContext eglContext;

    private int frameBufferTextureId = -1;
    private long sampleTime = 0;

    private AudioRender audioRender = null;

    private final Object activeEffectGuard = new Object();
    private final float[] stMatrix = new float[16];
    private LinkedList<IVideoEffect> videoEffects = new LinkedList<IVideoEffect>();

    private IEffectorSurface effectorSurface;
    private boolean isSurfaceCreated = false;
    private Object syncObject = new Object();
   
    private boolean rendered;
    private long neededPosition = 0;
    private boolean inSkipState = false;

    private boolean saved = false;
    private boolean awaitCurrentFileSample = false;

    private IEffectorSurface effectorOverlappingSurface;
    private long savedSampleTime = 0l;

    protected int trackId;
    private static final int SKIP_FRAME_DELTA = 100000;

    private long currentRealTime = 0l;
    private long videoRealTimeOffset = 0l;
    private long globalRealTimeOffset = 0l;


    private long lastDrawnPts = -1;
    private boolean isPaused = false;
    private boolean seekRequest = false;
    private long speedMultiplicity = 1;
    private int previousTrack = 0;


    public void renderSurfaceFromDecoderSurface(int id, long sampleTime) {
        frameBufferTextureId = id;
        this.sampleTime = sampleTime;
        rendered = false;

        if(isSkipDrawToWaitFrames() || checkIfSeekRequest()) return;

        prepareSurface();
        glView.requestRender();
        //Log.d("Preview Transcode", "Request render" + ++nRequests);

        synchronized (activeEffectGuard) {
            if (rendered) return;
            try {
                activeEffectGuard.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isSkipDrawToWaitFrames() {
        if (inSkipState) {
            if (((sampleTime - neededPosition) < SKIP_FRAME_DELTA) && ((sampleTime - neededPosition) > -SKIP_FRAME_DELTA)) {
                inSkipState = false;
                return false;
            } else {
                rendered = true;
                return true;
            }
        }

        return false;
    }

    boolean checkIfSeekRequest() {
        if (seekRequest) {
            if (previousTrack != getTrackId()) {
                exchangeTransitionSurface();
            }

            seekRequest = false;
            return true;
        }

        return false;
    }

    public PreviewTranscode(GLSurfaceView glView,
                            IEglUtil eglUtil) {
        this.glView = glView;
        this.eglUtil = eglUtil;
        textureRender = new TextureRenderer(eglUtil);

        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(true);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void setInputResolutions() {
        textureRender.setInputSize(glView.getWidth(), glView.getHeight());
        if (!videoEffects.isEmpty()) {
            for (IVideoEffect videoEffect : videoEffects) {
                videoEffect.setInputResolution(new Resolution(glView.getWidth(), glView.getHeight()));
            }
        }
    }


    public void prepareRender(ISurfaceListener listener) {
        eglContext = EGL14.eglGetCurrentContext();

        synchronized (syncObject) {
            try {
                if (!isSurfaceCreated)
                    syncObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        listener.onSurfaceAvailable(new EGLContextWrapper(eglContext));
    }


    public void createEffectorSurface(IEglUtil eglUtil) {
        effectorSurface = new EffectorSurface(eglUtil);
    }

    public void createOverlappingSurface(IEglUtil eglUtil) {
        effectorOverlappingSurface = new EffectorSurface(eglUtil);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        textureRender.surfaceCreated();
        createEffectorSurface(eglUtil);
        createOverlappingSurface(eglUtil);

        synchronized (syncObject){
            isSurfaceCreated = true;
            syncObject.notifyAll();
        }
    }

    public boolean isSurfaceCreated() {
        return isSurfaceCreated;
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        setInputResolutions();
    }

    @Override
    public void addVideoEffect(IVideoEffect videoEffect) {
        synchronized (activeEffectGuard) {
            videoEffects.add(videoEffect);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        eglContext = EGL14.eglGetCurrentContext();
        if (frameBufferTextureId == -1) return;
        
        if (lastDrawnPts == sampleTime && !isPaused) return;
        lastDrawnPts = sampleTime;

        if (currentRealTime == 0) {
            currentRealTime = System.nanoTime() / 1000;
            videoRealTimeOffset = currentRealTime - sampleTime;

            audioRender.syncSampleTimes(videoRealTimeOffset);
            globalRealTimeOffset = audioRender.getRealTimeOffset();

        } else {
            currentRealTime = System.nanoTime() / 1000;
        }

        synchronized (activeEffectGuard) {

            if (currentRealTime - globalRealTimeOffset < sampleTime) {
                try {
                    TimeUnit.MICROSECONDS.sleep(sampleTime - (currentRealTime - globalRealTimeOffset));
                } catch (InterruptedException ie) { }
            }


            drawFrame();
            rendered = true;
            activeEffectGuard.notifyAll();
        }
    }

    private void drawFrame() {
        applyEffects();
    }

    private void applyEffects() {
        boolean effectWasApplied = false;

        if(saved) return;
        copySurfaceFromDecoder();

        for (IVideoEffect effect : videoEffects) {
            FileSegment segment = effect.getSegment();

            if (isFrameInSegment(sampleTime, segment)) {

                effect.applyEffect(frameBufferTextureId, sampleTime, stMatrix);
                effectWasApplied = true;
                break;
            }
        }

        if (!effectWasApplied) {
            textureRender.drawFrameOES(stMatrix, frameBufferTextureId, 0, TextureRenderer.FillMode.PreserveAspectFit);
        }
    }

    private void prepareSurface() {
        effectorSurface.awaitNewImage();
        effectorSurface.getTransformMatrix(stMatrix);
    }

    private boolean isFrameInSegment(long pts, FileSegment segment) {
        if ((segment.pair.left <= pts && pts <= segment.pair.right) || (segment.pair.left == 0 && segment.pair.right == 0)) {
            return true;
        } else {
            return false;
        }
    }

    private void copySurfaceFromDecoder() {
        effectorSurface.updateTexImage();
        effectorSurface.getTransformMatrix(stMatrix);
    }

    @Override
    public IEffectorSurface getSurface() {
        return effectorSurface;
    }

    @Override
    public IEffectorSurface getOverlappingSurface() {
        return effectorOverlappingSurface;
    }

    @Override
    public void setPresentationTime(long time) {
    }

    @Override
    public LinkedList<IVideoEffect> getVideoEffects() {
        return videoEffects;
    }

    @Override
    public void setVideoSpeed(int speedMultiplicity) {
        this.speedMultiplicity = speedMultiplicity;
    }

    @Override
    public void waitFramesNearPosition(long position) {
        resetCurrentRealTime();
        //Log.d("Preview Transcode", "set skip state = true" );
        inSkipState = true;
        this.neededPosition = position;
    }

    public boolean inSkipState() {
        return inSkipState;
    }

    @Override
    public void prepareForNewInputData() {
        // TODO check if this case is false - may be we need still exchange surfaces
        if (awaitCurrentFileSample){
            setTransitionTrack();
            exchangeTransitionSurface();
            awaitCurrentFileSample = false;
        }
    }

    @Override
    public void resume() {
        isPaused = false;
        resetCurrentRealTime();
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    private void resetCurrentRealTime() {
        currentRealTime = 0l;
    }

    private int getInputIndex() {
        return effectorSurface.getSurfaceId();
    }

    private int getSecondInputIndex() {
        return effectorOverlappingSurface.getSurfaceId();
    }

    private void setTransitionTrack(){
        if (getTrackId() == 0) setTrackId(1);
        else setTrackId(0);
    }

    @Override
    public int getTrackId() {
        return trackId;
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    private void exchangeTransitionSurface(){
        IEffectorSurface tmp = effectorOverlappingSurface;
        effectorOverlappingSurface = effectorSurface;
        effectorSurface = tmp;
    }

    public void connectAudioRender(AudioRender audioRender) {
        this.audioRender = audioRender;
    }

    public void seekTrackExchange() {
        seekRequest = true;
        setTransitionTrack();
    }

    public void seekTrackChange(int trackId) {
        awaitCurrentFileSample = false;
        saved = false;
        seekRequest = true;
        previousTrack = effectorSurface.getSurfaceId() - 1;
        setTrackId(trackId);
    }

    @Override
    public void close() throws IOException {
        resetState();
    }

    private void resetState() {
        resetDrawingTimeVariables();
        resetRealTimeVariables();
        resetPreviewStateVariables();

        audioRender = null;
    }

    private void resetRealTimeVariables() {
        currentRealTime = 0l;
        videoRealTimeOffset = 0l;
        globalRealTimeOffset = 0l;
    }

    private void resetDrawingTimeVariables() {
        frameBufferTextureId = -1;
        sampleTime = 0;
        neededPosition = 0;
        savedSampleTime = 0l;
        lastDrawnPts = -1;
    }

    private void resetPreviewStateVariables() {
        rendered = false;
        inSkipState = false;
        saved = false;
        awaitCurrentFileSample = false;
        isPaused = false;
    }
}
