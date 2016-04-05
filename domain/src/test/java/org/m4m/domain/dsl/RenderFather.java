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

package org.m4m.domain.dsl;

import org.m4m.IProgressListener;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.MuxRender;
import org.m4m.domain.ProgressTracker;
import org.m4m.domain.Render;
import org.m4m.domain.pipeline.IOnStopListener;


public class RenderFather {
    private final Father create;
    private IMediaMuxer muxer;
    private IProgressListener progressListener;
    private IOnStopListener onStopListener;

    public RenderFather(Father create) {
        this.create = create;
        muxer = create.mediaMuxer().construct();
    }

    public RenderFather with(IMediaMuxer muxer) {
        this.muxer = muxer;
        return this;
    }

    public Render construct() {
        MuxRender muxRender = new MuxRender(muxer, (progressListener == null) ? create.progressListener().construct() : progressListener, new ProgressTracker());
        if (onStopListener != null) muxRender.addOnStopListener(onStopListener);
        return muxRender;
    }

    public RenderFather withProgressListener(IProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public RenderFather withOnStopListener(IOnStopListener onStopListener) {
        this.onStopListener = onStopListener;
        return this;
    }
}
