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
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.Matrix;

import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.Program;
import org.m4m.domain.graphics.TextureRenderer;
import org.m4m.domain.graphics.TextureType;
import org.m4m.domain.pipeline.TriangleVerticesCalculator;

import org.m4m.domain.Command;
import org.m4m.domain.CommandQueue;
import org.m4m.domain.Frame;
import org.m4m.domain.IInputRaw;
import org.m4m.domain.IOnFrameAvailableListener;
import org.m4m.domain.IPreview;
import org.m4m.domain.ISurface;
import org.m4m.domain.Resolution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraSource extends org.m4m.domain.CameraSource {
    private final IEglUtil eglUtil;
    private Camera camera;
    private ISurface surface;
    private SurfaceTextureManager surfaceTextureManager;
    private Frame currentFrame = new Frame(null, 1, 0, 0, 0, 0);
    private long startTimeStamp;
    private boolean firstFrame = true;
    private long timeStampOffset;
    private IPreview preview;
    private Resolution outputResolution;

    public CameraSource(IEglUtil eglUtil) {
        this.eglUtil = eglUtil;
    }

    @Override
    public Resolution getOutputResolution() {
        return outputResolution;
    }

    /**
     * Code for rendering a texture onto a surface using OpenGL ES 2.0.
     */
    private static class STextureRender {
        private static final int FLOAT_SIZE_BYTES = 4;
        private final float[] triangleVerticesData = TriangleVerticesCalculator.getDefaultTriangleVerticesData();
        private final IEglUtil eglUtil;

        private FloatBuffer triangleVertices;

        private static final String VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "    gl_Position = uMVPMatrix * aPosition;\n" +
                        "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                        "}\n";

        private static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +      // highp here doesn't seem to matter
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";

        private float[] mvpMatrix = new float[16];
        private float[] stMatrix = new float[16];

        private Program program = new Program();
        private int textureId;

        public STextureRender(IEglUtil eglUtil) {
            this.eglUtil = eglUtil;
            triangleVertices = ByteBuffer.allocateDirect(triangleVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            triangleVertices.put(triangleVerticesData).position(0);
            Matrix.setIdentityM(stMatrix, 0);
        }

        public int getTextureId() {
            return textureId;
        }

        /**
         * Initializes GL state.  Call this after the EGL surface has been created and made current.
         */
        public void surfaceCreated() {
            program = eglUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            textureId = eglUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        }

    }

    /**
     * Manages a SurfaceTexture.  Creates SurfaceTexture and TextureRender objects, and provides
     * functions that wait for frames and render them to the current EGL surface.
     * <p/>
     * The SurfaceTexture can be passed to Camera.setPreviewTexture() to receive camera output.
     */
    private class SurfaceTextureManager implements SurfaceTexture.OnFrameAvailableListener {
        private SurfaceTexture surfaceTexture;
        private CameraSource.STextureRender textureRender;
        private CommandQueue commandQueue;
        private final IEglUtil eglUtil;

        private Object syncObject = new Object();     // guards mFrameAvailable
        private int numberOfUnprocessedFrames = 0;
        private float[] matrix = new float[16];

        /* vvbuzove: logic to debug camera streaming sample
        private double fps;
        private double lastTime= 0.0; */

        /**
         * Creates instances of TextureRender and SurfaceTexture.
         */
        public SurfaceTextureManager(CommandQueue commandQueue, IEglUtil eglUtil) {
            this.commandQueue = commandQueue;
            this.eglUtil = eglUtil;
            textureRender = new CameraSource.STextureRender(eglUtil);
            textureRender.surfaceCreated();

            //if (VERBOSE) //Log.d(TAG, "textureID=" + mTextureRender.getTextureId());
            surfaceTexture = new SurfaceTexture(textureRender.getTextureId());

            // This doesn't work if this object is created on the thread that CTS started for
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
        }

        public void release() {
            // this causes a bunch of warnings that appear harmless but might confuse someone:
            //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
            // mSurfaceTexture.release();

            textureRender = null;
            surfaceTexture = null;
        }

        /**
         * Returns the SurfaceTexture.
         */
        public SurfaceTexture getSurfaceTexture() {
            return surfaceTexture;
        }

        /**
         * Latches the next buffer into the texture.  Must be called from the thread that created
         * the OutputSurface object.
         */
        public void prepareAvailableFrame() {
            synchronized (syncObject) {
                //if (numberOfUnprocessedFrames != 1) {
                //throw new RuntimeException("More than one frame available. Or we have no available frames");
                //}

                if (numberOfUnprocessedFrames > 0) {
                    numberOfUnprocessedFrames--;
                }
            }

            // Latch the data.
            eglUtil.checkEglError("before updateTexImage");
            surfaceTexture.updateTexImage();
        }

        /**
         * Draws the data from SurfaceTexture onto the current EGL surface.
         */
        public void drawImage() {
            surfaceTexture.getTransformMatrix(matrix);
            eglUtil.drawFrameStart(
                    textureRender.program,
                    textureRender.triangleVertices,
                    textureRender.mvpMatrix,
                    textureRender.stMatrix,
                    0,
                    TextureType.GL_TEXTURE_EXTERNAL_OES,
                    textureRender.getTextureId(),
                    outputResolution,
                    TextureRenderer.FillMode.PreserveAspectFit);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            synchronized (syncObject) {

                /* vvbuzove: logic to debug camera streaming sample
                double current = (double)System.currentTimeMillis();
                if (lastTime == 0) lastTime = current - 1;
                fps = 1000.0 / (current - lastTime);
                lastTime = current;
                Log.d("Camera", "fps = " + fps); */

                commandQueue.queue(Command.HasData, 0);
                numberOfUnprocessedFrames++;
                syncObject.notifyAll();
            }
        }
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        if (camera != null) {
            //if (VERBOSE) //Log.d(TAG, "releasing camera");
            if (preview == null) {
                camera.stopPreview();
            }
            preview = null;
            //mCamera.release();
            camera = null;
        }
    }

    /**
     * Configures SurfaceTexture for camera preview.  Initializes mStManager, and sets the
     * associated SurfaceTexture as the Camera's "preview texture".
     * <p/>
     * Configure the EGL surface that will be used for output before calling here.
     */
    private void prepareSurfaceTexture() {
        surfaceTextureManager = new SurfaceTextureManager(getOutputCommandQueue(), eglUtil);
        SurfaceTexture surfaceTexture = surfaceTextureManager.getSurfaceTexture();
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare EGL surface texture.", e);
        }
    }

    @Override
    public void setPreview(IPreview preview) {
        this.preview = preview;
    }

    /**
     * Releases the SurfaceTexture.
     */
    private void releaseSurfaceTexture() {
        if (surfaceTextureManager != null) {
            surfaceTextureManager.release();
            surfaceTextureManager = null;
        }
    }

    @Override
    public void setOutputSurface(ISurface surface) {
        this.surface = surface;
    }

    @Override
    public void setCamera(Object camera) {
        this.camera = (Camera) camera;
        updateOutputResolution();
    }

    private void updateOutputResolution() {
        Camera.Parameters parameters = this.camera.getParameters();
        Camera.Size previewSize = parameters.getPreviewSize();
        outputResolution = new Resolution(previewSize.width, previewSize.height);
    }

    @Override
    public void configure() {
        getOutputCommandQueue().queue(Command.OutputFormatChanged, 0);
        if (preview != null) {
            preview.setListener(new IOnFrameAvailableListener() {
                @Override
                public void onFrameAvailable() {
                    getOutputCommandQueue().queue(Command.HasData, 0);
                }
            });
        } else {
            surface.makeCurrent();
            prepareSurfaceTexture();
            camera.startPreview();
        }

        updateOutputResolution();
        camera.startPreview();

        getOutputCommandQueue().queue(Command.OutputFormatChanged, 0);
    }

    @Override
    public Frame getFrame() {
        if (surfaceTextureManager != null) {
            SurfaceTexture st = surfaceTextureManager.getSurfaceTexture();

            surfaceTextureManager.prepareAvailableFrame();
            surfaceTextureManager.drawImage();

            if (firstFrame) {
                long fromStartToFirstFrame = (System.currentTimeMillis() - startTimeStamp) * 1000000; // timeStampOffset in nanosecond
                timeStampOffset = st.getTimestamp() - fromStartToFirstFrame;
                firstFrame = false;
            }
            surface.setPresentationTime(st.getTimestamp() - timeStampOffset);
        }


        long sampleTime = (System.currentTimeMillis() - startTimeStamp) * 1000;
        //Log.d("AMP Camera Issue", "Video rame PTS = " + sampleTime);

        //Log.d("AMP Camera Issue", " " + sampleTime);
        currentFrame.setSampleTime(sampleTime);


        return currentFrame;
    }

    @Override
    public ISurface getSurface() {
        return surface;
    }

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return true;
    }

    @Override
    public void fillCommandQueues() {
    }

    @Override
    public void close() {
        releaseCamera();
        releaseSurfaceTexture();
    }

    @Override
    public void start() {
        startTimeStamp = System.currentTimeMillis();
        super.start();
    }
}
