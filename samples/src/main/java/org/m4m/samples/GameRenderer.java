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

package org.m4m.samples;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import org.m4m.IProgressListener;
import org.m4m.StreamingParameters;
import org.m4m.android.graphics.EglUtil;
import org.m4m.android.graphics.FrameBuffer;
import org.m4m.android.graphics.FullFrameTexture;
import org.m4m.android.graphics.ShaderProgram;
import org.m4m.domain.Resolution;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GameRenderer implements GLSurfaceView.Renderer {
    final int TARGET_FPS = 30;

    public enum RenderingMethod {
        RenderTwice,
        FrameBuffer
    }

    static float TRIANGLE_COORDINATES[] = {
        0.0f, 0.622008459f, 0.0f,
        1.0f, 0.0f, 0.0f, 1.0f,

        -0.5f, -0.311004243f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f,

        0.5f, -0.311004243f, 0.0f,
        0.0f, 1.0f, 0.0f, 1.0f
    };

    private static final String VERTEX_SHADER =
        "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec4 a_Color;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "v_Color = a_Color;\n" +
            "gl_Position = u_MVPMatrix * a_Position;}";

    private static final String FRAGMENT_SHADER =
        "precision mediump float;\n" +
            "varying vec4 v_Color;\n" +
            "void main() {\n" +
            "gl_FragColor = v_Color; }";

    private VideoCapture videoCapture;

    private FrameBuffer frameBuffer = new FrameBuffer(EglUtil.getInstance());
    private ShaderProgram shader = new ShaderProgram(EglUtil.getInstance());

    private FullFrameTexture texture;

    private FPSCounter fpsCounter;
    private Handler handler;

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    private float[] projectionMatrix = new float[16];

    private final FloatBuffer vertices;

    private int width;
    private int height;
    private Rect videoViewport;

    private long frameCount = 0;

    RenderingMethod renderingMethod;

    public GameRenderer(Context context, Handler handler, IProgressListener progressListener) {
        this.handler = handler;
        this.videoCapture = new VideoCapture(context, progressListener);

        fpsCounter = new FPSCounter(20);

        vertices = ByteBuffer.allocateDirect(TRIANGLE_COORDINATES.length * 4).
            order(ByteOrder.nativeOrder()).
            asFloatBuffer();

        vertices.put(TRIANGLE_COORDINATES).position(0);
    }

    public void setRenderingMethod(RenderingMethod method) {
        renderingMethod = method;
    }

    public void startCapturing(StreamingParameters params) throws IOException {
        if (videoCapture == null) {
            return;
        }
        synchronized (videoCapture) {
            videoCapture.start(params);
        }
    }

    public void startCapturing(String videoPath) throws IOException {
        if (videoCapture == null) {
            return;
        }
        synchronized (videoCapture) {
            videoCapture.start(videoPath);
        }
    }

    public void stopCapturing() {
        if (videoCapture == null) {
            return;
        }
        synchronized (videoCapture) {
            if (videoCapture.isStarted()) {
                videoCapture.stop();
            }
        }
    }

    public boolean isCapturingStarted() {
        return videoCapture.isStarted();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        shader.create(VERTEX_SHADER, FRAGMENT_SHADER);

        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, -2.0f, 0.0f, 1.0f, 0.0f);

        if (texture != null) {
            texture.release();
            texture = null;
        }

        texture = new FullFrameTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        final float ratioDisplay = (float) width / height;

        Matrix.frustumM(projectionMatrix, 0, -ratioDisplay, ratioDisplay, -1.0f, 1.0f, 1.0f, 10.0f);

        this.width = width;
        this.height = height;

        frameBuffer.setResolution(new Resolution(this.width, this.height));

        videoViewport = new Rect();

        videoViewport.left = 0;
        videoViewport.top = 0;

        // Landscape
        if (ratioDisplay > 1.0f) {
            videoViewport.right = videoCapture.getFrameWidth();
            videoViewport.bottom = (int) (videoCapture.getFrameWidth() / ratioDisplay);
        } else {
            videoViewport.bottom = videoCapture.getFrameHeight();
            videoViewport.right = (int) (videoCapture.getFrameHeight() * ratioDisplay);
        }

        videoViewport.offsetTo((videoCapture.getFrameWidth() - videoViewport.right) / 2, (videoCapture.getFrameHeight() - videoViewport.bottom) / 2);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        update();

        GLES20.glViewport(0, 0, width, height);

        if (!videoCapture.isStarted()) {
            renderScene();
        } else {
            if (renderingMethod == RenderingMethod.RenderTwice) {
                renderScene();

                synchronized (videoCapture) {
                    if (videoCapture.beginCaptureFrame()) {
                        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                        GLES20.glViewport(videoViewport.left, videoViewport.top, videoViewport.width(), videoViewport.height());

                        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
                        GLES20.glScissor(videoViewport.left, videoViewport.top, videoViewport.width(), videoViewport.height());

                        renderScene();

                        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

                        videoCapture.endCaptureFrame();
                    }
                }
            } else {
                frameBuffer.bind();

                renderScene();

                frameBuffer.unbind();

                texture.draw(frameBuffer.getTextureId());

                synchronized (videoCapture) {
                    if (videoCapture.beginCaptureFrame()) {
                        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                        GLES20.glViewport(videoViewport.left, videoViewport.top, videoViewport.width(), videoViewport.height());

                        texture.draw(frameBuffer.getTextureId());

                        videoCapture.endCaptureFrame();
                    }
                }
            }
        }

        if (fpsCounter.update()) {
            handler.sendMessage(handler.obtainMessage(GameCapturing.UPDATE_FPS, fpsCounter.fps(), 0));
        }
    }

    private void update() {
        float angleInDegrees = 360 * (frameCount % 60) / 60f;

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);

        frameCount++;
    }

    private void renderScene() {
        drawTriangle(vertices);
    }

    private void drawTriangle(final FloatBuffer triangle) {
        shader.use();

        int positionHandle = shader.getAttributeLocation("a_Position");
        int colorHandle = shader.getAttributeLocation("a_Color");
        int MVPMatrixHandle = shader.getAttributeLocation("u_MVPMatrix");

        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        triangle.position(0);

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 7 * 4, triangle);
        GLES20.glEnableVertexAttribArray(positionHandle);

        triangle.position(3);

        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 7 * 4, triangle);
        GLES20.glEnableVertexAttribArray(colorHandle);

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        shader.unUse();
    }
}
