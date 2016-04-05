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

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.view.Surface;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.TextureRenderer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Holds state associated with a Surface used for MediaCodec decoder output.
 * <p/>
 * The (width,height) constructor for this class will prepare GL, create a SurfaceTexture,
 * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
 * MediaCodec.setMediaFormat() to receive decoder output.  When a frame arrives, we latch the
 * texture with updateTexImage, then render the texture with GL to a pbuffer.
 * <p/>
 * The no-arg constructor skips the GL preparation step and doesn't allocate a pbuffer.
 * Instead, it just creates the Surface and SurfaceTexture, and when a frame arrives
 * we just draw it on whatever surface is current.
 * <p/>
 * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
 * can potentially drop frames.
 */
public class OutputSurface implements SurfaceTexture.OnFrameAvailableListener {
    private EGL10 egl;
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;

    private int textureId;
    private SurfaceTexture surfaceTexture;
    private Surface surface;

    private final Object isFrameAvailableSyncGuard = new Object();
    private boolean isFrameAvailable;

    private TextureRenderer textureRender;
    private IEglUtil eglUtil;


    /**
     * Creates an OutputSurface using the current EGL context.  Creates a Surface that can be
     * passed to MediaCodec.setMediaFormat().
     *
     * @param eglUtil
     */
    public OutputSurface(IEglUtil eglUtil) {
        this.eglUtil = eglUtil;
        textureRender = new TextureRenderer(this.eglUtil);
        textureRender.surfaceCreated();

        // Even if we don't access the SurfaceTexture after the constructor returns, we
        // still need to keep a reference to it.  The Surface doesn't retain a reference
        // at the Java level, so if we don't either then the object can get GCed, which
        // causes the native finalizer to run.
        //if (VERBOSE) //Log.d(TAG, "textureID=" + mTextureRender.getTextureId());
        textureId = this.eglUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        surfaceTexture = new SurfaceTexture(textureId);

        // This doesn't work if OutputSurface is created on the thread that CTS started for
        // these test cases.
        //
        // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
        // create a Handler that uses it.  The "frame available" message is delivered
        // there, but since we're not a Looper-based thread we'll never see it.  For
        // this to do anything useful, OutputSurface must be created on a thread without
        // a Looper, so that SurfaceTexture uses the main application Looper instead.
        //
        // Java language note: passing "this" out of a constructor is generally unwise,
        // but we should be able to get away with it here.
        surfaceTexture.setOnFrameAvailableListener(this);
        surface = new Surface(surfaceTexture);
    }

    public void setInputSize(int width, int height) {
        textureRender.setInputSize(width, height);
    }

    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    public void release() {
        if (egl != null) {
            if (egl.eglGetCurrentContext().equals(eglContext)) {
                // Clear the current context and surface to ensure they are discarded immediately.
                egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                                   EGL10.EGL_NO_CONTEXT);
            }
            egl.eglDestroySurface(eglDisplay, eglSurface);
            egl.eglDestroyContext(eglDisplay, eglContext);
            //mEGL.eglTerminate(mEGLDisplay);

            // added line
            //egl.eglTerminate(eglDisplay);
            // added line
        }

        surface.release();

        // this causes a bunch of warnings that appear harmless but might confuse someone:
        //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
        //mSurfaceTexture.release();
        surfaceTexture.release();
        eglUtil = null;

        // null everything out so future attempts to use this object will cause an NPE
        eglDisplay = null;
        eglContext = null;
        eglSurface = null;
        egl = null;

        textureRender = null;
        surface = null;
        surfaceTexture = null;
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {

        String message = "Failed to set up EGL context and surface.";

        if (egl == null) {
            throw new RuntimeException(message);
        }
        eglUtil.checkEglError("before makeCurrent");
        if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException(message);
        }
    }

    /**
     * Returns the Surface that we draw onto.
     */
    public Surface getSurface() {
        return surface;
    }

    public int getTextureId() {
        return textureId;
    }

    public void getTransformMatrix(float[] transformMatrix) {
        surfaceTexture.getTransformMatrix(transformMatrix);
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the OutputSurface object, after the onFrameAvailable callback has signaled that new
     * data is available.
     */
    public void awaitNewImage() {
        final int TIMEOUT_MS = 500;
        int timeout = 0;

        synchronized (isFrameAvailableSyncGuard) {
            while (!isFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    isFrameAvailableSyncGuard.wait(TIMEOUT_MS);
                    if (!isFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        timeout++;
                        //Log.w("OutputSurface", "awaitNewImage "+TIMEOUT_MS*timeout+"ms timeout");

                        if (timeout>20) {
                            throw new RuntimeException("Frame wait timed out.");
                        }
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            isFrameAvailable = false;
        }
    }

    public void updateTexImage() {
        surfaceTexture.updateTexImage();
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public void drawImage() {
        textureRender.drawFrameOES(new SurfaceTextureWrapper(surfaceTexture), textureId, 0, TextureRenderer.FillMode.PreserveAspectFit);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        //if (VERBOSE) //Log.d(TAG, "new frame available");
        synchronized (isFrameAvailableSyncGuard) {
            if (isFrameAvailable) {
//                throw new RuntimeException("Failed to notify on a new frame available.");
            }
            isFrameAvailable = true;
            isFrameAvailableSyncGuard.notifyAll();
        }
    }

    public SurfaceTexture getSurfaceTexture() { return surfaceTexture;}

    public TextureRenderer getTextureRender() {return textureRender;}
}
