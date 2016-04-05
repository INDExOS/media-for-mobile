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
import org.m4m.domain.ISurfaceTexture;
import org.m4m.domain.Wrapper;

public class SurfaceTextureWrapper extends Wrapper<SurfaceTexture> implements ISurfaceTexture {
    public SurfaceTextureWrapper(SurfaceTexture surfaceTexture) {
        super(surfaceTexture);
    }

    @Override
    public float[] getTransformMatrix() {
        float[] transformMatrix = new float[16];
        getNativeObject().getTransformMatrix(transformMatrix);
        return transformMatrix;
    }
}
