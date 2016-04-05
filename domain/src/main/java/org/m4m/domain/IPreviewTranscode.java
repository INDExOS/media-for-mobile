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

package org.m4m.domain;

import org.m4m.IVideoEffect;

import java.io.Closeable;
import java.util.LinkedList;

public interface IPreviewTranscode extends Closeable {

    void renderSurfaceFromDecoderSurface(int id, long sampleTime);

    void setPresentationTime(long time);

    IEffectorSurface getSurface();

    IEffectorSurface getOverlappingSurface();

    void prepareRender(ISurfaceListener listener);

    void addVideoEffect(IVideoEffect videoEffect);

    boolean isSurfaceCreated();

    LinkedList<IVideoEffect> getVideoEffects();

    void setVideoSpeed(int speedMultiplicity);

    void waitFramesNearPosition(long position);

    public boolean inSkipState();

    int getTrackId();

    void setTrackId(int trackId);

    void prepareForNewInputData();

    void resume();

    void pause();

    void connectAudioRender(AudioRender audioRender);

    void seekTrackExchange();

    void seekTrackChange(int trackId);
}
