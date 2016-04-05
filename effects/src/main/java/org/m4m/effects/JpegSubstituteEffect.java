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

import android.graphics.*;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class JpegSubstituteEffect extends VideoEffect {
    private static final float[] TEX_VERTICES = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static final float[] POS_VERTICES = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    private Bitmap resizedBitmap;
    private int[] textures = new int[1];
    private int textureCoordinateHandle;
    private int posCoordinateHandle;
    private FloatBuffer textureVertices;
    private FloatBuffer posVertices;

    public JpegSubstituteEffect(String fileName, Resolution out, int rotation, IEglUtil eglUtil) {
        super(0, eglUtil);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        float value;
        resizedBitmap = Bitmap.createBitmap(out.width(), out.height(), Bitmap.Config.ARGB_8888);
        Canvas wideBmpCanvas = new Canvas(resizedBitmap);
        RectF rect = new RectF();

        if (rotatedBitmap.getHeight() > rotatedBitmap.getWidth()) {
            value = ((float) out.width() - ((float) out.height() / (float) rotatedBitmap.getHeight()) * (float) rotatedBitmap.getWidth()) / (float) 2;
            rect.left = value;
            rect.right = out.width() - value;
            rect.top = 0;
            rect.bottom = out.height();
            wideBmpCanvas.drawBitmap(rotatedBitmap, null, rect, null);
        } else {
            float dFrameAR = (float) rotatedBitmap.getWidth() / (float) rotatedBitmap.getHeight();
            float dPixelAR = (float) out.width() / dFrameAR;

            if (dPixelAR < out.height()) {
                value = (float) out.width() / ((float) out.height() / (((float) out.height() - dPixelAR) / (float) 2));
                rect.left = 0;
                rect.right = out.width();
                rect.top = value;
                rect.bottom = out.height() - value;
            } else {
                value = (float) out.width() / ((float) out.width() / (((float) out.width() - (float) out.height() * dFrameAR) / (float) 2));
                rect.left = value;
                rect.right = out.width() - value;
                rect.top = 0;
                rect.bottom = out.height();
            }
            wideBmpCanvas.drawBitmap(rotatedBitmap, null, rect, null);
        }

        setFragmentShader(getFragmentShader());
        setVertexShader(getVertexShader());
        //bitmap.recycle(); // Leads to memory corruption in libc: signal 11 (SIGSEGV).
    }

    protected String getVertexShader() {
        return "attribute vec4 a_position;\n" +
                "attribute vec2 a_texcoord;\n" +
                "varying vec2 v_texcoord;\n" +
                "void main() {\n" +
                "  gl_Position = a_position;\n" +
                "  v_texcoord = a_texcoord;\n" +
                "}\n";
    }

    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D tex_sampler;\n" +
                "varying vec2 v_texcoord;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
                "}\n";
    }

    public void start() {
        createProgram(getVertexShader(), getFragmentShader());
        eglProgram.programHandle = shaderProgram.getProgramHandle();

        textureCoordinateHandle = shaderProgram.getAttributeLocation("a_texcoord");
        posCoordinateHandle = shaderProgram.getAttributeLocation("a_position");

        textureVertices = ByteBuffer.allocateDirect(TEX_VERTICES.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices.put(TEX_VERTICES).position(0);
        posVertices = ByteBuffer.allocateDirect(POS_VERTICES.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        posVertices.put(POS_VERTICES).position(0);

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        //load bitmap
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, resizedBitmap, 0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void applyEffect(int inputTextureId, long timeProgress, float[] transformMatrix) {
        // Use our shader program
        GLES20.glUseProgram(shaderProgram.getProgramHandle());
        checkGlError();

        // Set the vertex attributes
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureVertices);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(posCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, posVertices);
        GLES20.glEnableVertexAttribArray(posCoordinateHandle);
        checkGlError();

        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        checkGlError();

        // Draw
        GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
