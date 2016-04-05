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

package org.m4m.android.graphics;

import org.m4m.IVideoEffect;
import org.m4m.domain.FileSegment;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.Program;
import org.m4m.domain.graphics.TextureRenderer;
import org.m4m.domain.graphics.TextureType;
import org.m4m.domain.pipeline.TriangleVerticesCalculator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VideoEffect implements IVideoEffect {
    protected static final int FLOAT_SIZE_BYTES = 4;
    protected static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    protected static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    protected static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    protected Resolution inputResolution = new Resolution(0, 0);
    private FileSegment segment = new FileSegment(0l, 0l);
    protected IEglUtil eglUtil;
    protected Program eglProgram = new Program();
    // OpenGL handlers
    protected boolean wasStarted;
    protected float[] mvpMatrix = new float[16];
    private FloatBuffer triangleVertices;
    private int angle;
    protected ShaderProgram shaderProgram;
    private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;
    private String vertexShader =  IEglUtil.VERTEX_SHADER;
    private String fragmentShader =  IEglUtil.FRAGMENT_SHADER_OES;

    public VideoEffect(int angle, IEglUtil eglUtil) {
        this.angle = angle;
        this.eglUtil = eglUtil;
    }
    public void setVertexShader(String verexShader) {
        this.vertexShader = verexShader;
    }

    public void setFragmentShader(String fragmentShader) {
        this.fragmentShader = fragmentShader;
    }

    @Override
    public FileSegment getSegment() {
        return segment;
    }

    @Override
    public void setSegment(FileSegment segment) {
        this.segment = segment;
    }

    protected void addEffectSpecific() {
    }

    /**
     * Initializes GL state.  Call this after the encoder EGL surface has been created and made current.
     */
    @Override
    public void start() {
        triangleVertices = ByteBuffer
                .allocateDirect(TriangleVerticesCalculator.getDefaultTriangleVerticesData().length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        createProgram(vertexShader, fragmentShader);

        eglProgram.programHandle = shaderProgram.getProgramHandle();
        eglProgram.positionHandle = shaderProgram.getAttributeLocation("aPosition");
        eglProgram.textureHandle = shaderProgram.getAttributeLocation("aTextureCoord");
        eglProgram.mvpMatrixHandle = shaderProgram.getAttributeLocation("uMVPMatrix");
        eglProgram.stMatrixHandle = shaderProgram.getAttributeLocation("uSTMatrix");

        wasStarted = true;
    }

    @Override
    public void setInputResolution(Resolution resolution) {
        inputResolution = resolution;
    }

    @Override
    public void applyEffect(int inputTextureId, long timeProgress, float[] transformMatrix) {
        if (!wasStarted) {
            start();
        }
        triangleVertices.clear();
        triangleVertices.put(TriangleVerticesCalculator.getDefaultTriangleVerticesData()).position(0);

        eglUtil.drawFrameStart(
                eglProgram,
                triangleVertices,
                mvpMatrix,
                transformMatrix,
                angle,
                TextureType.GL_TEXTURE_EXTERNAL_OES,
                inputTextureId,
                inputResolution,
                fillMode
        );
        addEffectSpecific();
        eglUtil.drawFrameFinish();
    }

    @Override
    public void setFillMode(TextureRenderer.FillMode fillMode) {
        this.fillMode = fillMode;
    }

    @Override
    public TextureRenderer.FillMode getFillMode() {
        return fillMode;
    }

    @Override
    public void setAngle(int degrees) {
        angle = degrees;
    }

    @Override
    public int getAngle() {
        return angle;
    }

    protected int createProgram(String vertexSource, String fragmentSource) {
        shaderProgram = new ShaderProgram(eglUtil);
        shaderProgram.create(vertexSource, fragmentSource);
        return shaderProgram.getProgramHandle();
    }

    protected void checkGlError(String component) {
        eglUtil.checkEglError(component);
    }

    protected void checkGlError() {
        eglUtil.checkEglError("VideoEffect");
    }
}
