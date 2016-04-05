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

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import org.m4m.IVideoEffect;
import org.m4m.domain.IPreview;
import org.m4m.domain.IOnFrameAvailableListener;
import org.m4m.domain.PreviewContext;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.TextureRenderer;

import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;


public class PreviewRender
        extends Handler
        implements IPreview, GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private IOnFrameAvailableListener listener;
    public static final int MSG_SET_SURFACE_TEXTURE = 0;
    private Camera camera;

    final static String TAG = "PreviewRender";
    private GLSurfaceView glView;
    private IEglUtil eglUtil;
    private TextureRenderer textureRender;

    //private static final boolean VERBOSE = false;

    private final float[] stMatrix = new float[16];

    private SurfaceTexture surfaceTexture;

    private final Object activeEffectGuard = new Object();
    private IVideoEffect activeEffect;
    private EGLContext eglContext;
    private Camera.Size inputRes;
    private int frameBufferTextureId = -1;
    private int textureId;
    private boolean previewTextureSet;
    private boolean requestRendering = true;
    private boolean skipFrame;
    private Surface surface;
    //Matrix taken from back facing camera
    private final float [] ffCameraMatrix_0 = {
            1, 0, 0, 0,
            0,-1, 0, 0,
            0, 0, 1, 0,
            0, 1, 0, 1,
    };

    private final float [] ffCameraMatrix_1 = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };
    private int previewOrientationDegrees = 0;
    private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;

    public PreviewRender(GLSurfaceView glView,
                         IEglUtil eglUtil,
                         Camera camera) {
        this.glView = glView;
        this.eglUtil = eglUtil;
        textureRender = new TextureRenderer(eglUtil);
        this.camera = camera;
        updateCameraParameters();

        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(true);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void onSurfaceCreated(GL10 unused, javax.microedition.khronos.egl.EGLConfig config) {
        //Log.d(TAG, "onSurfaceCreated");

        // Set up the texture blitter that will be used for on-screen display.  This
        // is *not* applied to the recording, because that uses a separate shader.
        textureRender.surfaceCreated();


        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.
        textureId = eglUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        surfaceTexture = new SurfaceTexture(textureId);
        surface = new android.view.Surface(surfaceTexture);

        Context context = glView.getContext();

        // Tell the UI thread to enable the camera preview.
        sendMessage(obtainMessage(MSG_SET_SURFACE_TEXTURE));
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        //Log.d("", "onSurfaceChanged " + width + "x" + height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        //if (VERBOSE) Log.d("", "onDrawFrame tex=" + textureId);

        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.
        surfaceTexture.updateTexImage();
        eglContext = EGL14.eglGetCurrentContext();

        if (inputRes == null) {
            // Texture size isn't set yet.  This is only used for the filters, but to be
            // safe we can just skip drawing while we wait for the various races to resolve.
            // (This seems to happen if you toggle the screen off/on with power button.)
            //Log.i("", "Drawing before incoming texture size set; skipping");
            return;
        }

        if (skipFrame) {
            skipFrame = false;
            return;
        }

        // Draw the video frame.
        synchronized (activeEffectGuard) {
            if (frameBufferTextureId != -1) {
                //front facing camera mirror image for preview texture, but since we are using FB that flip not happened
                //and we have to apply it manually, TODO: probably some users want to control that flip
                textureRender.drawFrame2D(ffCameraMatrix_0, frameBufferTextureId, previewOrientationDegrees, fillMode);
                frameBufferTextureId = -1;
            } else if (activeEffect != null) {
                surfaceTexture.getTransformMatrix(stMatrix);
                activeEffect.setAngle(previewOrientationDegrees);
                if (activeEffect.getFillMode() != fillMode)
                    activeEffect.setFillMode(fillMode);
                activeEffect.applyEffect(textureId, 0, stMatrix);
            } else {
                textureRender.drawFrameOES(new SurfaceTextureWrapper(surfaceTexture), textureId, previewOrientationDegrees, fillMode);
            }
        }
    }

    @Override
    public void requestRendering() {
        glView.requestRender();
    }

    @Override
    public void setActiveEffect(IVideoEffect effectApplied) {
        synchronized (activeEffectGuard) {
            activeEffect = effectApplied;
            updateEffectResolution();
            frameBufferTextureId = -1;
        }
    }

    @Override
    public void renderSurfaceFromFrameBuffer(int id) {
        synchronized (activeEffectGuard) {
            frameBufferTextureId = id;
            requestRendering();
        }
    }

    @Override
    public void setOrientation(int screenOrientationDegrees) {
        previewOrientationDegrees = screenOrientationDegrees;
    }

    @Override
    public int getOrientation() {
        return previewOrientationDegrees;
    }

    @Override
    public void updateCameraParameters() {
        synchronized (activeEffectGuard) {
            inputRes = this.camera.getParameters().getPreviewSize();
            updateEffectResolution();
            textureRender.setInputSize(inputRes.width, inputRes.height);
        }
    }

    private void updateEffectResolution() {
        if (activeEffect != null && inputRes != null) {
            activeEffect.setInputResolution(new Resolution(inputRes.width, inputRes.height));
        }
    }

    @Override
    public void start() {
        requestRendering = true;
        if (previewTextureSet) {
            camera.startPreview();
            //resolution change lead to old frames appeared
            skip1Frame();
        }
    }

    private void skip1Frame() {
        skipFrame=true;
    }

    @Override
    public void stop() {
        requestRendering = false;
        if (previewTextureSet) {
            camera.stopPreview();
        }
    }

    @Override
    public void setListener(IOnFrameAvailableListener listener) {
        synchronized (activeEffectGuard) {
            this.listener = listener;
        }
    }

    public PreviewContext getSharedContext() {
        return new PreviewContext(new SurfaceTextureWrapper(surfaceTexture), textureId, new EGLContextWrapper(eglContext));
    }

    @Override  // runs on UI thread
    public void handleMessage(Message inputMessage) {
        //Log.d(TAG, "CameraHandler [" + this + "]: what=" + inputMessage.what);
        int what = inputMessage.what;

        switch (what) {
            case MSG_SET_SURFACE_TEXTURE:
                try {
                    camera.setPreviewTexture(surfaceTexture);
                    previewTextureSet = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("camera.setPreviewTexture(surfaceTexture)");
                }
                surfaceTexture.setOnFrameAvailableListener(this);

                camera.startPreview();
                break;
            default:
                throw new RuntimeException("unknown msg " + what);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (!requestRendering) return;

        synchronized (activeEffectGuard) {
            if (listener == null) {
                requestRendering();
                return;
            }
            listener.onFrameAvailable();
        }
    }

    @Override
    public void setFillMode(TextureRenderer.FillMode fillMode) {
        this.fillMode = fillMode;
    }

    @Override
    public TextureRenderer.FillMode getFillMode() {
        return fillMode;
    }
}
