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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;

public abstract class OverlayEffect extends VideoEffect {

    private int oTextureHandle;
    private int[] textures = new int[1];

    Bitmap bitmap = null;
    int inputBitmapWidth = 1280; // default resolution
    int inputBitmapHeight = 720;


    public OverlayEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
        bitmap = Bitmap.createBitmap(inputBitmapWidth, inputBitmapHeight, Bitmap.Config.ARGB_8888);
        setFragmentShader(getFragmentShader());
    }

    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "uniform sampler2D oTexture;\n" +
                "void main() {\n" +
                "  vec4 bg_color = texture2D(sTexture, vTextureCoord);\n" +
                "  vec4 fg_color = texture2D(oTexture, vTextureCoord);\n" +
                "  float colorR = (1.0 - fg_color.a) * bg_color.r + fg_color.a * fg_color.r;\n" +
                "  float colorG = (1.0 - fg_color.a) * bg_color.g + fg_color.a * fg_color.g;\n" +
                "  float colorB = (1.0 - fg_color.a) * bg_color.b + fg_color.a * fg_color.b;\n" +
                "  gl_FragColor = vec4(colorR, colorG, colorB, bg_color.a);\n" +
                "}\n";

    }

    @Override
    public void start() {
        super.start();
        oTextureHandle = shaderProgram.getAttributeLocation("oTexture");

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    }

    @Override
    protected void addEffectSpecific() {
        if (bitmap.getWidth()!= inputResolution.width()) {
            bitmap = Bitmap.createBitmap(inputResolution.width(), inputResolution.height(), Bitmap.Config.ARGB_8888);
        } else if (bitmap.getHeight() != inputResolution.height()) {
            bitmap = Bitmap.createBitmap(inputResolution.width(), inputResolution.height(), Bitmap.Config.ARGB_8888);
        }

        bitmap.eraseColor(Color.argb(0, 0, 0, 0));

        Canvas bitmapCanvas = new Canvas(bitmap);

        drawCanvas(bitmapCanvas);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        checkGlError("glBindTexture");


        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        checkGlError("texImage2d");
        GLES20.glUniform1i(oTextureHandle, 1);
        checkGlError("oTextureHandle - glUniform1i");
    }

    protected void finalize() {
    }

    protected abstract void drawCanvas(Canvas canvas);
}
