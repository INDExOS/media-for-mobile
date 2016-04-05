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

import java.io.IOException;
import java.util.ArrayList;

public abstract class Encoder extends MediaCodecPlugin implements ITransform, ISurfaceCreator {
    private ISurface surface;
    ArrayList<IOnSurfaceReady> listeners = new ArrayList();

    public Encoder(IMediaCodec mediaCodec) {
        super(mediaCodec);
        initInputCommandQueue();
    }

    @Override
    public ISurface getSurface() {
        if (surface == null) {
            surface = mediaCodec.createInputSurface();

            for (IOnSurfaceReady listener : listeners) {
                listener.onSurfaceReady();
            }
        }
        return surface;
    }

    @Override
    public ISurface getSimpleSurface(IEglContext eglContext) {
        if (surface == null) {
            surface = mediaCodec.createSimpleInputSurface(eglContext);
        }
        return surface;
    }

    @Override
    public void checkIfOutputQueueHasData() {
        while (IMediaCodec.INFO_TRY_AGAIN_LATER != getOutputBufferIndex());
    }

    @Override
    public void push(Frame frame) {
        feedMeIfNotDraining();
    }

    @Override
    public void configure() {
        mediaCodec.configure(mediaFormat, null, IMediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    @Override
    public void onSurfaceAvailable(IOnSurfaceReady listener) {
        listeners.add(listener);
    }

    @Override
    public void pull(Frame frame) {
        throw new UnsupportedOperationException("Unexpected call of pull() in Encoder.");
    }

    @Override
    public void releaseOutputBuffer(int outputBufferIndex) {
        mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (surface != null){
            surface.release();
            surface = null;
        }
    }
}
