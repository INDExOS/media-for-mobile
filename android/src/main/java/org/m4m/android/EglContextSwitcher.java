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

import android.opengl.*;
import org.m4m.domain.IEgl;

public class EglContextSwitcher implements IEgl {
    private EGLDisplay display;
    private EGLSurface drawSurface;
    private EGLSurface readSurface;
    private EGLContext context;

    // Orthographic projection matrix.  Must be updated when the available screen area changes (e.g. when the device is rotated)
    static final float projectionMatrix[] = new float[16];
    private final float savedMatrix[] = new float[16];

    private int width = 0, height = 0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        Matrix.orthoM(projectionMatrix, 0, 0, this.width, 0, this.height, -1, 1);
    }

    @Override
    public void saveEglState() {
        if (width == 0 || height == 0) {
            return;
        }

        System.arraycopy(projectionMatrix, 0, savedMatrix, 0, projectionMatrix.length);
        display = EGL14.eglGetCurrentDisplay();
        drawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        readSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        context = EGL14.eglGetCurrentContext();
    }

    @Override
    public void restoreEglState() {
        if (width == 0 || height == 0) {
            return;
        }

        if (!EGL14.eglMakeCurrent(display, drawSurface, readSurface, context)) {
            throw new RuntimeException("Failed to restore EGL state.");
        }
        System.arraycopy(savedMatrix, 0, projectionMatrix, 0, projectionMatrix.length);
    }

    @Override
    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }
}
