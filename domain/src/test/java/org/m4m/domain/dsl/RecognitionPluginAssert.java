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
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class RecognitionPluginAssert {
    private IRecognitionPlugin plugin;

    public RecognitionPluginAssert(IRecognitionPlugin plugin) {this.plugin = plugin;}

    public void received(Frame frame) {
        ArgumentCaptor<IRecognitionPlugin.RecognitionInput> actualRecognitionInput = ArgumentCaptor.forClass(IRecognitionPlugin.RecognitionInput.class);
        verify(plugin, atLeastOnce()).recognize(actualRecognitionInput.capture());
        byte[] expectedBytes = frame.getByteBuffer().array();
        ByteBuffer actualBytes = actualRecognitionInput.getValue().getFrame().getByteBuffer();
        for (int i = 0; i < expectedBytes.length; i++) {
            assertEquals(expectedBytes[i], actualBytes.get(i));
        }
    }
}
