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

import org.m4m.IRecognitionPlugin;
import org.m4m.domain.Frame;
import org.m4m.domain.IOutput;
import org.m4m.domain.RecognitionPipeline;

import static org.mockito.Mockito.mock;

public class RecognitionPipelineFather extends FatherOf<RecognitionPipeline> {
    private IRecognitionPlugin recognitionPlugin;
    private IOutput output;

    RecognitionPipelineFather(Father create) {
        super(create);
    }

    public RecognitionPipelineFather withPlugin(IRecognitionPlugin recognitionPlugin) {
        this.recognitionPlugin = recognitionPlugin;
        return this;
    }

    public RecognitionPipelineFather withSourceFrame(Frame frame) {
        output = create.mediaSource().withInfinite(frame).construct();
        return this;
    }

    @Override
    public RecognitionPipeline construct() {
        return new RecognitionPipeline(
            output != null ? output : mock(IOutput.class),
            recognitionPlugin != null ? recognitionPlugin : mock(IRecognitionPlugin.class));
    }
}
