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

import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.Program;
import org.m4m.domain.graphics.TextureRenderer;
import org.m4m.domain.graphics.TextureType;
import org.m4m.domain.pipeline.TriangleVerticesCalculator;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import java.nio.FloatBuffer;

public class EglUtil implements IEglUtil {
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final TriangleVerticesCalculator scaleCalculator = new TriangleVerticesCalculator();

    private EglUtil() { }

    private static class EglUtilSingletonHolder {
        private static final EglUtil INSTANCE = new EglUtil();
    }

    public static EglUtil getInstance() {
        return EglUtilSingletonHolder.INSTANCE;
    }

    @Override
    public Resolution getCurrentSurfaceResolution() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        EGLSurface readSurface = egl.eglGetCurrentSurface(EGL14.EGL_READ);

        int[] width = {0};
        egl.eglQuerySurface(display, readSurface, EGL10.EGL_WIDTH, width);

        int[] height = {0};
        egl.eglQuerySurface(display, readSurface, EGL10.EGL_HEIGHT, height);

        return new Resolution(width[0], height[0]);
    }

    @Override
    public Program createProgram(String vertexShader, String fragmentShader) {
        ShaderProgram shaderProgram = createShaderProgram(vertexShader, fragmentShader);

        if (shaderProgram.getProgramHandle() == 0) {
            throw new RuntimeException("Failed to create shader program.");
        }

        Program program = new Program();
        program.programHandle = shaderProgram.getProgramHandle();
        program.positionHandle = shaderProgram.getAttributeLocation("aPosition");
        program.textureHandle = shaderProgram.getAttributeLocation("aTextureCoord");
        program.mvpMatrixHandle = shaderProgram.getAttributeLocation("uMVPMatrix");
        program.stMatrixHandle = shaderProgram.getAttributeLocation("uSTMatrix");
        return program;
    }

    @Override
    public int createTexture(int textureType) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        int textureId = textures[0];
        GLES20.glBindTexture(textureType, textureId);
        checkEglError("glBindTexture mTextureID");

        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(textureType, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(textureType, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkEglError("glTexParameter");
        return textureId;
    }

    @Override
    public void drawFrameStart(
            Program program,
            FloatBuffer triangleVertices,
            float[] mvpMatrix,
            float[] stMatrix,
            float angle,
            TextureType textureType,
            int textureId,
            Resolution inputResolution,
            TextureRenderer.FillMode fillMode
    ) {
        checkEglError("onDrawFrame start");

        Resolution out;
        switch (fillMode) {
            case PreserveSize:
                out = inputResolution;
                break;
            case PreserveAspectFit:
            case PreserveAspectCrop:
                out = getCurrentSurfaceResolution();
                break;
            default:
                out = new Resolution(0, 0);
                break;
        }

        GLES20.glViewport(0, 0, out.width(), out.height());

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program.programHandle);
        checkEglError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkEglError("glActiveTexture");

        int textureTypeId;
        if (textureType == TextureType.GL_TEXTURE_2D) {
            textureTypeId = GLES20.GL_TEXTURE_2D;
        } else {
            textureTypeId = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        }

        GLES20.glBindTexture(textureTypeId, textureId);
        checkEglError("glBindTexture");

        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(program.positionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        checkEglError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(program.positionHandle);
        checkEglError("glEnableVertexAttribArray maPositionHandle");

        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(program.textureHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        checkEglError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(program.textureHandle);
        checkEglError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mvpMatrix, 0);

        float scale[];
        switch (fillMode) {
            case PreserveAspectFit:
                scale = scaleCalculator.getScale_PreserveAspectFit((int) angle, inputResolution.width(), inputResolution.height(), out.width(), out.height());
                Matrix.scaleM(mvpMatrix, 0, scale[0], scale[1], 1);
                Matrix.rotateM(mvpMatrix, 0, -angle, 0.f, 0.f, 1.f);
                break;
            case PreserveAspectCrop:
                scale = scaleCalculator.getScale_PreserveAspectCrop((int) angle, inputResolution.width(), inputResolution.height(), out.width(), out.height());
                Matrix.scaleM(mvpMatrix, 0, scale[0], scale[1], 1);
                Matrix.rotateM(mvpMatrix, 0, -angle, 0.f, 0.f, 1.f);
                break;
            case PreserveSize:
                if (angle == 90 || angle == 270) {
                    Matrix.rotateM(mvpMatrix, 0, 180, 0.f, 0.f, 1.f);
                }
                break;
            default:
                break;
        }

        GLES20.glUniformMatrix4fv(program.mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(program.stMatrixHandle, 1, false, stMatrix, 0);
    }

    @Override
    public void drawFrameFinish() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkEglError("glDrawArrays");
        GLES20.glFinish();
    }

    @Override
    public void drawFrame(
            Program program,
            FloatBuffer triangleVertices,
            float[] mvpMatrix,
            float[] stMatrix,
            float angle,
            TextureType textureType,
            int textureId,
            Resolution resolution,
            TextureRenderer.FillMode fillMode
    ) {
        drawFrameStart(program, triangleVertices, mvpMatrix, stMatrix, angle, textureType, textureId, resolution, fillMode);
        drawFrameFinish();
    }

    private ShaderProgram createShaderProgram(String vertexSource, String fragmentSource) {
        ShaderProgram shaderProgram = new ShaderProgram(this);
        shaderProgram.create(vertexSource, fragmentSource);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(shaderProgram.getProgramHandle(), GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(shaderProgram.getProgramHandle());
        }
        return shaderProgram;
    }

    @Override
    public void checkEglError(String operation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(operation + ": glError " + error);
        }
    }

    @Override
    public void setIdentityMatrix(float[] stMatrix, int smOffset) {
        Matrix.setIdentityM(stMatrix, 0);
    }
}



