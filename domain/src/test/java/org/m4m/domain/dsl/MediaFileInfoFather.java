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

import org.m4m.MediaFileInfo;
import org.m4m.domain.MediaSource;
import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;

public class MediaFileInfoFather extends FatherOf<MediaFileInfo> {
    AndroidMediaObjectFactoryFake androidMediaObjectFactoryFake;

    public MediaFileInfoFather(Father create) {
        super(create);
        androidMediaObjectFactoryFake = new AndroidMediaObjectFactoryFake(create);
    }

    public MediaFileInfoFather with(MediaSource mediaSource) {
        androidMediaObjectFactoryFake.withMediaSource(mediaSource);
        return this;
    }

    @Override
    public MediaFileInfo construct() {
        return new MediaFileInfo(androidMediaObjectFactoryFake);
    }

}
