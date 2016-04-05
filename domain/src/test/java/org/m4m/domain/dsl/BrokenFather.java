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

import org.m4m.domain.IMediaCodec;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class BrokenFather {
    private final Father create;

    public BrokenFather(Father create) {
        this.create = create;
    }

    public MediaComposerFather mediaComposer() {
        IMediaCodec brokenMediaCodec = mock(IMediaCodec.class);
        doThrow(new RuntimeException()).when(brokenMediaCodec).configure(any(MediaFormat.class), any(ISurfaceWrapper.class), anyInt());
        return new MediaComposerFather(create).withDecoderMediaCodec(brokenMediaCodec);
    }

    public MediaCodecFather mediaCodec() {
        MediaCodecFather mediaCodecFather = new MediaCodecFather(create);
        IMediaCodec brokenMediaCodec = mock(IMediaCodec.class);
        mediaCodecFather.withMediaCodec(brokenMediaCodec);
        doThrow(new RuntimeException()).when(brokenMediaCodec).configure(any(MediaFormat.class), any(ISurfaceWrapper.class), anyInt());
        return mediaCodecFather;
    }
}
