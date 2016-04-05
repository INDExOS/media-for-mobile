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

import org.m4m.IVideoEffect;
import org.m4m.domain.FileSegment;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.TextureRenderer;

public class JNIVideoEffect implements IVideoEffect {
    private long tag;
    private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;

    public JNIVideoEffect(long tag) {
        this.tag = tag;
    }
    @Override
    public FileSegment getSegment() {
        return getSegmentJNI(tag);
    }

    @Override
    public void setSegment(FileSegment segment) {
        //nomansland - no one will call this method from java
    }

    @Override
    public void start() {
        startJNI(tag);
    }

    @Override
    public void applyEffect(int inTextureId, long timeProgress, float[] transformMatrix) {
        applyEffectJNI(tag, inTextureId, timeProgress, transformMatrix);
    }

    @Override
    public void setInputResolution(Resolution resolution) {
        setInputResolutionJNI(tag, resolution);
    }

    @Override
    public void setFillMode(TextureRenderer.FillMode fillMode) {
        this.fillMode = fillMode;
        fitToCurrentSurfaceJNI(tag, fillMode == TextureRenderer.FillMode.PreserveAspectFit);
    }

    public TextureRenderer.FillMode getFillMode() {
        return fillMode;
    }

    @Override
    public void setAngle(int degrees) {

    }

    @Override
    public int getAngle() {
        return 0;
    }

    private native FileSegment getSegmentJNI(long thisListener);
    private native void startJNI(long tag);
    private native void applyEffectJNI(long thisListener, int inTextureId, long timeProgress, float[] transformMatrix);
    private native void setInputResolutionJNI(long thisListener, Resolution resolution);
    private native boolean fitToCurrentSurfaceJNI(long thisListener, boolean should);
}
