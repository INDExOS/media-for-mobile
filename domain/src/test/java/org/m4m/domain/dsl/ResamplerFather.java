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

import org.m4m.AudioFormat;
import org.m4m.domain.Resampler;

public class ResamplerFather extends FatherOf<Resampler> {
    private Resampler resampler = null;

    public ResamplerFather(Father father) {
        super(father);
        resampler = new Resampler(create.audioFormat().construct());
    }

    public ResamplerFather withTargetAudioFormat(AudioFormat audioFormat) {
        resampler.setTargetParameters(audioFormat);
        return this;
    }

    public ResamplerFather withInputAudioFormat(AudioFormat audioFormat) {
        resampler.setInputParameters(audioFormat);
        return this;
    }

    public Resampler construct() {
        return resampler;
    }
}
