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

import org.m4m.IRecognitionPlugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RecognitionPipelineTest extends TestBase {
    @Rule
    public ExpectedException expectedEx_withNullPluginOrSource = ExpectedException.none();

    @Test
    public void create_withNullOutput_throwsIllegalArgumentException() {
        expectedEx_withNullPluginOrSource.expect(IllegalArgumentException.class);
        expectedEx_withNullPluginOrSource.expectMessage("Plugin or Source can't be null");

        new RecognitionPipeline(null, mock(IRecognitionPlugin.class));
    }

    @Test
    public void create_withNullRecognitionPlugin_throwsIllegalArgumentException() {
        expectedEx_withNullPluginOrSource.expect(IllegalArgumentException.class);
        expectedEx_withNullPluginOrSource.expectMessage("Plugin or Source can't be null");

        new RecognitionPipeline(mock(IOutput.class), null);
    }

    @Test
    public void create_withBothNullArguments_throwsIllegalArgumentException() {
        expectedEx_withNullPluginOrSource.expect(IllegalArgumentException.class);
        expectedEx_withNullPluginOrSource.expectMessage("Plugin or Source can't be null");

        new RecognitionPipeline(null, null);
    }

    @Rule
    public ExpectedException expectedEx_withStoppingRecognition = ExpectedException.none();

    @Test
    public void start_callsAtLeastOnceRecognize() throws Exception {
        IRecognitionPlugin plugin = mock(IRecognitionPlugin.class);
        final RecognitionPipeline recognitionPipeline = create.recognitionPipeline().withPlugin(plugin).construct();

        startAsync(recognitionPipeline, 20);
        recognitionPipeline.stop();

        verify(plugin, atLeastOnce()).recognize(any(IRecognitionPlugin.RecognitionInput.class));
    }

    @Test
    public void start_pullsFrameFromMediaSourceToRecognitionPlugin() throws Exception {
        Frame frame = a.frame(123).construct();
        IRecognitionPlugin plugin = a.recognitionPlugin();
        final RecognitionPipeline recognitionPipeline = create.recognitionPipeline()
            .withSourceFrame(frame)
            .withPlugin(plugin)
            .construct();

        startAsync(recognitionPipeline, 100);
        recognitionPipeline.stop();

        assertThat(plugin).received(frame);
    }

    private void startAsync(final RecognitionPipeline recognitionPipeline, long timeToWait) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                recognitionPipeline.start();
            }
        });
        thread.start();
        thread.join(timeToWait);
        assertTrue(thread.isAlive());
    }
}