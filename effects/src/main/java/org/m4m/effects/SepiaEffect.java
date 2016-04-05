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

package org.m4m.effects;

import android.opengl.GLES20;
import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;

public class SepiaEffect extends VideoEffect {
    public SepiaEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
        setFragmentShader(getFragmentShader());
    }

    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform mat3 uWeightsMatrix;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                "  vec3 color_new = min(uWeightsMatrix * color.rgb, 1.0);\n" +
                "  gl_FragColor = vec4(color_new.rgb, color.a);\n" +
                "}\n";

    }

    protected float[] getWeights() {
        return new float[]{
                805.0f / 2048.0f, 715.0f / 2048.0f, 557.0f / 2048.0f,
                1575.0f / 2048.0f, 1405.0f / 2048.0f, 1097.0f / 2048.0f,
                387.0f / 2048.0f, 344.0f / 2048.0f, 268.0f / 2048.0f
        };
    }

    protected int weightsMatrixHandle;

    @Override
    public void start() {
        super.start();
        weightsMatrixHandle = shaderProgram.getAttributeLocation("uWeightsMatrix");
    }

    @Override
    protected void addEffectSpecific() {
        GLES20.glUniformMatrix3fv(weightsMatrixHandle, 1, false, getWeights(), 0);
    }
}
