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

package org.m4m.domain.pipeline;

import org.junit.Test;
import org.m4m.domain.AudioDecoder;
import org.m4m.domain.AudioEffector;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.CommandProcessor;
import org.m4m.domain.ISurface;
import org.m4m.domain.Pipeline;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PipelineTest extends TestBase {
    @Test
    public void canConnectDecoderAndEncoderSurfaces() {
        VideoDecoder decoder = create.videoDecoder().construct();
        ISurface surface = create.surface().construct();
        VideoEncoder encoder = create.videoEncoder().with(surface).construct();

        encoder.setMediaFormat(create.videoFormat().construct());

        Pipeline pipeline = create.pipeline().withVideoDecoder(decoder).with(encoder).construct();
        pipeline.resolve();

        assertEquals(surface, decoder.getSurface());
    }


    @Test
    public void connectorFactoryWithoutAudioFormat_ConnectAudioDecoderEncoder_UnsupportedOperationException() {
        CommandProcessor commandProcessor = create.commandProcessor().construct();
        ConnectorFactory connectorFactory = new ConnectorFactory(commandProcessor, null);
        AudioDecoder audioDecoder = create.audioDecoder().construct();
        AudioEncoder audioEncoder = create.audioEncoder().construct();

        try {
            connectorFactory.connect(audioDecoder, audioEncoder);
        }
        catch(Exception e) {
            assertTrue(e.toString().contains("java.lang.UnsupportedOperationException"));
        }
    }

    @Test
    public void connectorFactoryWithoutAudioFormat_ConnectAudioEffectorEncoder_ThrowsUnsupportedOperationException() {
        CommandProcessor commandProcessor = create.commandProcessor().construct();
        ConnectorFactory connectorFactory = new ConnectorFactory(commandProcessor, null);
        AudioEffector audioEffector = create.audioEffector().construct();
        AudioEncoder audioEncoder = create.audioEncoder().construct();

        try {
            connectorFactory.connect(audioEffector, audioEncoder);
        }
        catch(Exception e) {
            assertTrue(e.toString().contains("java.lang.UnsupportedOperationException"));
        }
    }
}
