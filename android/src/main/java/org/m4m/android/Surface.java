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

import android.media.MediaCodec;
import android.opengl.EGL14;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.IEglUtil;

public class Surface implements ISurface {
    private final OutputSurface outputSurface;
    private final InputSurface inputSurface;
    private int width;
    private int height;

    public Surface(MediaCodec mediaCodec, IEglUtil eglUtil) {
        this.inputSurface = new InputSurface(mediaCodec.createInputSurface(), EGL14.eglGetCurrentContext());
        this.inputSurface.makeCurrent();
        this.outputSurface = new OutputSurface(eglUtil);
    }

    @Override
    public void awaitNewImage() {
        outputSurface.awaitNewImage();
    }

    @Override
    public void updateTexImage() {
        outputSurface.updateTexImage();
    }


    @Override
    public void awaitAndCopyNewImage() {
        outputSurface.awaitNewImage();
        outputSurface.updateTexImage();
    }

    @Override
    public void drawImage() {
        outputSurface.drawImage();
    }

    @Override
    public void setPresentationTime(long presentationTimeInNanoSeconds) {
        inputSurface.setPresentationTime(presentationTimeInNanoSeconds);
    }

    @Override
    public void swapBuffers() {
        inputSurface.swapBuffers();
    }

    @Override
    public void makeCurrent() {
        inputSurface.makeCurrent();
    }

    @Override
    public ISurfaceWrapper getCleanObject() {
        return AndroidMediaObjectFactory.Converter.convert(outputSurface.getSurface());
    }

    @Override
    public void setProjectionMatrix(float[] projectionMatrix) {
        inputSurface.setProjectionMatrix(projectionMatrix);
    }

    @Override
    public void setViewport() {
        inputSurface.setViewPort();
    }

    @Override
    public void setInputSize(int width, int height) {
        this.width = width;
        this.height = height;
        outputSurface.setInputSize(width, height);
    }

    @Override
    public Resolution getInputSize() {
        return new Resolution(width, height);
    }

    public OutputSurface getOutputSurface() {
        return outputSurface;
    }

    public InputSurface getInputSurface() {
        return inputSurface;
    }

    @Override
    public void release(){
    }
}
