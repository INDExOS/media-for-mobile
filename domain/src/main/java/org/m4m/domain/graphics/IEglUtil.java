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

import org.m4m.domain.Resolution;

import java.nio.FloatBuffer;

public interface IEglUtil {
    static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uSTMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_Position = uMVPMatrix * aPosition;\n" +
            "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
            "}\n";

    static final String FRAGMENT_SHADER_OES =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +      // highp here doesn't seem to matter
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    Resolution getCurrentSurfaceResolution();

    Program createProgram(String vertexShader, String fragmentShader);

    int createTexture(int textureType);

    void drawFrameStart(
            Program program,
            FloatBuffer triangleVertices,
            float[] mvpMatrix,
            float[] stMatrix,
            float angle,
            TextureType textureType,
            int textureId,
            Resolution inputResolution,
            TextureRenderer.FillMode fillMode
    );

    void drawFrameFinish();

    void drawFrame(
            Program program,
            FloatBuffer triangleVertices,
            float[] mvpMatrix,
            float[] stMatrix,
            float angle,
            TextureType textureType,
            int textureId,
            Resolution resolution,
            TextureRenderer.FillMode fillMode
    );

    void checkEglError(String operation);

    void setIdentityMatrix(float[] stMatrix, int smOffset);
}
