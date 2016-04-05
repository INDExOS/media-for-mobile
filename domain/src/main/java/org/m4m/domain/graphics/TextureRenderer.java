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

package org.m4m.domain.graphics;

import org.m4m.domain.ISurfaceTexture;
import org.m4m.domain.Resolution;
import org.m4m.domain.pipeline.TriangleVerticesCalculator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
public class TextureRenderer {
    private static final int FLOAT_SIZE_BYTES = 4;

    private FloatBuffer triangleVertices;

    private static final String VERTEX_SHADER_SAMPLER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition* vec4(1.0, -1.0, 1.0, 1.0);\n" +
                    "  vTextureCoord = ((uSTMatrix * aTextureCoord) * vec4(1.0, 1.0, 1.0, 1.0) ).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER_SAMPLER =
            "precision mediump float;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "   gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}";

    private float[] mvpMatrix = new float[16];
    private float[] stMatrix = new float[16];

    private IEglUtil eglUtil;
    private Program programWithOES;
    private Program programWithSampler;
    private Resolution resolution;

    public enum FillMode {
        PreserveSize, PreserveAspectFit, PreserveAspectCrop
    }

    public TextureRenderer(IEglUtil eglUtil) {
        this.eglUtil = eglUtil;

        float[] data = TriangleVerticesCalculator.getDefaultTriangleVerticesData();
        triangleVertices = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        putTriangleVertices(data);
        this.eglUtil.setIdentityMatrix(stMatrix, 0);
    }

    public void setInputSize(int widthIn, int heightIn) {
        resolution = new Resolution(widthIn, heightIn);
    }

    private void putTriangleVertices(float[] data) {
        triangleVertices.position(0);
        triangleVertices.put(data);
        triangleVertices.position(0);
    }

    public void drawFrame2D(ISurfaceTexture surfaceTexture, int textureId, float angle, FillMode fillMode) {
        stMatrix = surfaceTexture.getTransformMatrix();
        drawFrame2D(stMatrix, textureId, angle, fillMode);
    }

    public void drawFrame2D(float[] stMatrix, int textureId, float angle, FillMode fillMode) {
        eglUtil.drawFrame(programWithSampler, triangleVertices, mvpMatrix, stMatrix, angle, TextureType.GL_TEXTURE_2D, textureId, resolution, fillMode);
    }

    public void drawFrameOES(ISurfaceTexture surfaceTexture, int textureId, float angle, FillMode fillMode) {
        stMatrix = surfaceTexture.getTransformMatrix();
        drawFrameOES(stMatrix, textureId, angle, fillMode);
    }

    public void drawFrameOES(float[] stMatrix, int textureId, float angle, FillMode fillMode) {
        eglUtil.drawFrame(programWithOES, triangleVertices, mvpMatrix, stMatrix, angle, TextureType.GL_TEXTURE_EXTERNAL_OES, textureId, resolution, fillMode);
    }

    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        programWithOES = eglUtil.createProgram(IEglUtil.VERTEX_SHADER, IEglUtil.FRAGMENT_SHADER_OES);
        programWithSampler = eglUtil.createProgram(VERTEX_SHADER_SAMPLER, FRAGMENT_SHADER_SAMPLER);
    }
}