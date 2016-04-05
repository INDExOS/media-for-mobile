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

import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.IPreview;
import org.m4m.domain.VideoEffector;
import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;


public class VideoEffectorFather extends FatherOf<VideoEffector> {
    private IAndroidMediaObjectFactory factory;
    private IPreview preview;

    public VideoEffectorFather(Father create) {
        super(create);
    }

    public VideoEffectorFather withPreview(IPreview preview) {
        this.preview = preview;
        return this;
    }

    public VideoEffectorFather withFactory(IAndroidMediaObjectFactory factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public VideoEffector construct() {
        VideoEffector videoEffector = new VideoEffector(this.create.mediaCodec().construct(),
                                                        factory != null ? factory : new AndroidMediaObjectFactoryFake(create));
        if (preview != null) {
            videoEffector.enablePreview(preview);
        }
        return videoEffector;
    }
}
