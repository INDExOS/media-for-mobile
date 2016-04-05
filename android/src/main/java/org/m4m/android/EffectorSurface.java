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

import org.m4m.domain.IEffectorSurface;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.TextureRenderer;

public class EffectorSurface implements IEffectorSurface {
    OutputSurface outputSurface;
    private int width;
    private int height;
    private SurfaceTexture surfaceTexture;

    public EffectorSurface(IEglUtil eglUtil) {
        outputSurface = new OutputSurface(eglUtil);
    }

    @Override
    public void release() {
        outputSurface.release();
    }

    @Override
    public void updateTexImage() {
        outputSurface.updateTexImage();
    }

    @Override
    public void awaitAndCopyNewImage() {
        awaitNewImage();
        updateTexImage();
    }

    @Override
    public void awaitNewImage() {
        outputSurface.awaitNewImage();
    }

    @Override
    public void drawImage() {
        outputSurface.drawImage();
    }

    @Override
    public void setPresentationTime(long presentationTimeInNanoSeconds) {
    }

    @Override
    public void swapBuffers() {
    }

    @Override
    public void makeCurrent() {
        outputSurface.makeCurrent();
    }

    @Override
    public void setProjectionMatrix(float[] projectionMatrix) {
    }

    @Override
    public void getTransformMatrix(float[] transformMatrix) {
        outputSurface.getTransformMatrix(transformMatrix);
    }

    @Override
    public ISurfaceWrapper getCleanObject() {
        return AndroidMediaObjectFactory.Converter.convert(outputSurface.getSurface());
    }

    @Override
    public int getSurfaceId() {
        return outputSurface.getTextureId();
    }

    @Override
    public void drawImage(int textureIdx, float[] matrix, TextureRenderer.FillMode fillMode) {
        //TODO: figureout where to get trans    formation matrix
        outputSurface.getTextureRender().drawFrameOES(matrix, textureIdx, 0, fillMode);
    }

    @Override
    public void drawImage2D(int textureIdx, float[] matrix) {
        outputSurface.getTextureRender().drawFrame2D(matrix, textureIdx, 0, TextureRenderer.FillMode.PreserveAspectFit);
    }

    @Override
    public void drawImage2D(int textureIdx, float[] matrix, int angle, TextureRenderer.FillMode fillMode) {
        outputSurface.getTextureRender().drawFrame2D(matrix, textureIdx, angle, fillMode);
    }

    @Override
    public void setViewport() {
    }

    @Override
    public void setInputSize(int width, int height) {
        this.width = width;
        this.height = height;
        outputSurface.getTextureRender().setInputSize(width, height);
    }

    @Override
    public Resolution getInputSize() {
        return new Resolution(width, height);
    }
}
