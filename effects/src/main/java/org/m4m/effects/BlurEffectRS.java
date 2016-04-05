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

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.TextureRenderer;
import org.m4m.domain.pipeline.TriangleVerticesCalculator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BlurEffectRS extends VideoEffect {
    protected FloatBuffer triangleVertices2D;
    private IntBuffer buffer;
    private int width, height;
    private RenderScript renderScript;
    private Allocation allocationIn;
    private Allocation allocationOut;
    private Bitmap inputBitmap;
    private Bitmap outputBitmap;
    private TextureRenderer textureRenderer;
    private int rsOutTexture;
    private ScriptIntrinsicBlur intrinsicBlur;
    private int radius;

    public BlurEffectRS(int angle, Context context, IEglUtil eglUtil, int radius) {
        super(angle, eglUtil);
        this.radius = radius;
        renderScript = RenderScript.create(context);
        textureRenderer = new TextureRenderer(eglUtil);
    }

    @Override
    public void start() {
        super.start();
        prepareDrawOutput();
    }

    @Override
    public void setInputResolution(Resolution resolution) {

        super.setInputResolution(resolution);
        textureRenderer.setInputSize(resolution.width(), resolution.height());
    }

    @Override
    public void applyEffect(int inputTextureId, long l, float[] floats) {

        TextureRenderer.FillMode prevFillMode = getFillMode();
        setFillMode(TextureRenderer.FillMode.PreserveSize);

        super.applyEffect(inputTextureId, l, floats);

        setFillMode(prevFillMode);

        if (inputResolution.width() != width || inputResolution.height() != height) {
            width = inputResolution.width();
            height = inputResolution.height();
            outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            inputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            allocationOut = Allocation.createFromBitmap(renderScript, outputBitmap);
            buffer = IntBuffer.wrap(new int[width * height]);
        }

        buffer.position(0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        buffer.rewind();

        checkGlError("GLES20.glReadPixels");

        applyRenderScriptEffect();

        textureRenderer.drawFrame2D(floats, rsOutTexture, 0f, prevFillMode);
    }

    private void prepareDrawOutput() {

        textureRenderer.surfaceCreated();

        triangleVertices2D = ByteBuffer.allocateDirect(TriangleVerticesCalculator.getDefaultTriangleVerticesData().length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        triangleVertices2D.put(TriangleVerticesCalculator.getDefaultTriangleVerticesData()).position(0);

        rsOutTexture = eglUtil.createTexture(GLES20.GL_TEXTURE_2D);

        intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
    }

    private void applyRenderScriptEffect() {
        inputBitmap.copyPixelsFromBuffer(buffer);

        allocationIn = Allocation.createFromBitmap(renderScript, inputBitmap);

        intrinsicBlur.setRadius(radius);
        intrinsicBlur.setInput(allocationIn);
        intrinsicBlur.forEach(allocationOut);

        allocationOut.copyTo(outputBitmap);

        GLES20.glViewport(0, 0, width, height);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, rsOutTexture);
        checkGlError("GLES20.glBindTexture");
        //for the debug purpose, you may try to output the inputBitmap
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, inputBitmap, 0);
        checkGlError("GLUtils.texImage2D");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }
}
