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
import android.opengl.EGLContext;
import android.view.Surface;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.Resolution;

public class SimpleSurface implements ISurface {
    private InputSurface inputSurface;
    private Surface androidSurface;
    private int width;
    private int height;

    public SimpleSurface(MediaCodec mediaCodec, EGLContext eglSharedCtx) {
        androidSurface = mediaCodec.createInputSurface();
        inputSurface = new InputSurface(androidSurface, eglSharedCtx);
    }

    @Override
    public void awaitNewImage() {
        // should not be called
    }

    @Override
    public void drawImage() {
        // should not be called
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
        return AndroidMediaObjectFactory.Converter.convert(inputSurface.getSurface());
    }

    @Override
    public void setProjectionMatrix(float[] projectionMatrix) {
        //mInputSurface.setProjectionMatrix(projectionMatrix);
    }

    @Override
    public void setViewport() {
        //mInputSurface.setViewPort();
    }

    @Override
    public void setInputSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Resolution getInputSize() {
        return new Resolution(width, height);
    }

    public Surface getNativeSurface() {
        return androidSurface;
    }

    @Override
    public void release(){
        inputSurface.release();
        androidSurface.release();

        inputSurface = null;
        androidSurface = null;
    }

    @Override
    public void updateTexImage() {

    }

    @Override
    public void awaitAndCopyNewImage() {

    }
}
