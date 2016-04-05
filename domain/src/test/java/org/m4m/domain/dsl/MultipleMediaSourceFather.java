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

import org.m4m.MediaFile;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;

public class MultipleMediaSourceFather {
    private final Father create;
    private MultipleMediaSource multipleMediaSource = new MultipleMediaSource();

    public MultipleMediaSourceFather(Father create) {
        this.create = create;
    }

    public MultipleMediaSourceFather with(MediaSource mediaSource) throws RuntimeException {
        multipleMediaSource.add(new MediaFile(mediaSource));
        return this;
    }

    public MultipleMediaSourceFather with(MediaSourceFather mediaSourceFather) throws RuntimeException {
        multipleMediaSource.add(new MediaFile(mediaSourceFather.construct()));
        return this;
    }

    public MultipleMediaSourceFather withFrameSize(int width, int height) {
        with(create.mediaSource().with(create.videoFormat().withFrameSize(width, height).construct()).construct());
        return this;
    }

    public MultipleMediaSource construct() throws RuntimeException {
        if (multipleMediaSource.files().isEmpty()) {
            with(create.mediaSource().construct());
        }
        return multipleMediaSource;
    }
}
