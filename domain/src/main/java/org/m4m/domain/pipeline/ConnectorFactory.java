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

import org.m4m.AudioFormat;
import org.m4m.domain.AudioDecoder;
import org.m4m.domain.AudioEffector;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Encoder;
import org.m4m.domain.ICameraSource;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.ICommandProcessor;
import org.m4m.domain.IInputRaw;
import org.m4m.domain.IMediaSource;
import org.m4m.domain.IMicrophoneSource;
import org.m4m.domain.IOutputRaw;
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.IsConnectable;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Plugin;
import org.m4m.domain.Render;
import org.m4m.domain.SurfaceRender;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEffector;
import org.m4m.domain.VideoEncoder;
import org.m4m.domain.VideoTimeScaler;

import java.util.Collection;
import java.util.LinkedList;

import static org.m4m.domain.pipeline.ManyToOneConnectable.ManyToOneConnections;
import static org.m4m.domain.pipeline.OneToManyConnectable.OneToManyConnection;
import static org.m4m.domain.pipeline.OneToOneConnectable.OneToOneConnection;

public class ConnectorFactory {
    private final ICommandProcessor commandProcessor;
    private final AudioFormat audioMediaFormat;

    public ConnectorFactory(ICommandProcessor commandProcessor, AudioFormat audioMediaFormat) {
        this.commandProcessor = commandProcessor;
        this.audioMediaFormat = audioMediaFormat;
    }

    public void connect(IOutputRaw source, IInputRaw transform) {

        if (source instanceof IMediaSource && transform instanceof Plugin) {
            new PluginConnector(commandProcessor).connect((IMediaSource) source, (Plugin) transform);
            return;
        }

        if (source instanceof IMediaSource && transform instanceof Render) {
            new PluginConnector(commandProcessor).connect((IMediaSource) source, (Render) transform);
            return;
        }

        if (source instanceof VideoDecoder && transform instanceof VideoEncoder) {
            new PluginConnector(commandProcessor).connect((VideoDecoder) source, (VideoEncoder) transform);
            return;
        }

        if (source instanceof AudioDecoder && transform instanceof AudioEncoder) {
            new PluginConnector(commandProcessor).connect((AudioDecoder) source, (AudioEncoder) transform, audioMediaFormat);
            return;
        }

        if (source instanceof VideoEffector && transform instanceof VideoEncoder) {
            new PluginConnector(commandProcessor).connect((VideoEffector) source, (VideoEncoder) transform);
            return;
        }

        if (source instanceof VideoDecoder && transform instanceof VideoEffector) {
            new PluginConnector(commandProcessor).connect((VideoDecoder) source, (VideoEffector) transform);
            return;
        }

        if (source instanceof VideoDecoder  && transform instanceof SurfaceRender) {
            new PluginConnector(commandProcessor).connect((VideoDecoder  ) source, (SurfaceRender) transform);
        }

        if (source instanceof AudioEffector && transform instanceof AudioEncoder) {
            new PluginConnector(commandProcessor).connect((AudioEffector) source, (AudioEncoder) transform, audioMediaFormat);
            return;
        }

        if (source instanceof AudioDecoder && transform instanceof AudioEffector) {
            new PluginConnector(commandProcessor).connect((AudioDecoder) source, (AudioEffector) transform);
            return;
        }

        if (source instanceof ICaptureSource && transform instanceof Encoder) {
            new PluginConnector(commandProcessor).connect((ICaptureSource) source, (Encoder) transform);
            return;
        }

        if (source instanceof ICameraSource && transform instanceof Encoder) {
            new PluginConnector(commandProcessor).connect((ICameraSource) source, (Encoder) transform);
            return;
        }

        if (source instanceof ICameraSource && transform instanceof VideoEffector) {
            new PluginConnector(commandProcessor).connect((ICameraSource) source, (VideoEffector) transform);
            return;
        }

        if (source instanceof IMicrophoneSource && transform instanceof AudioEncoder) {
            new PluginConnector(commandProcessor).connect((IMicrophoneSource) source, (AudioEncoder) transform);
            return;
        }

        if (source instanceof IMicrophoneSource && transform instanceof AudioEffector) {
            new PluginConnector(commandProcessor).connect((IMicrophoneSource) source, (AudioEffector) transform);
            return;
        }

        if (source instanceof IPluginOutput && transform instanceof Render) {
            new PluginConnector(commandProcessor).connect((IPluginOutput) source, (Render) transform);
            return;
        }

        if (source instanceof VideoTimeScaler && transform instanceof VideoEncoder) {
            new PluginConnector(commandProcessor).connect((VideoTimeScaler) source, (VideoEncoder) transform);
            return;
        }

        if (source instanceof VideoDecoder && transform instanceof VideoTimeScaler) {
            new PluginConnector(commandProcessor).connect((VideoDecoder) source, (VideoTimeScaler) transform);
            return;
        }

        throw new RuntimeException("No connection between " + source.getClass().toString() + " and " + transform.getClass().toString());
    }

    public Collection<IsConnectable> createConnectionRules() {
        Collection<IsConnectable> collection = new LinkedList<IsConnectable>();
        collection.add(OneToOneConnection(ICaptureSource.class, VideoEncoder.class));
        collection.add(OneToOneConnection(ICaptureSource.class, VideoEffector.class));
        collection.add(OneToOneConnection(ICameraSource.class, VideoEncoder.class));
        collection.add(OneToOneConnection(ICameraSource.class, VideoEffector.class));
        collection.add(OneToOneConnection(VideoDecoder.class, VideoEncoder.class));
        collection.add(OneToOneConnection(VideoDecoder.class, VideoEffector.class));
        collection.add(OneToOneConnection(VideoDecoder.class, SurfaceRender.class));
        collection.add(OneToOneConnection(VideoEncoder.class, Render.class));
        collection.add(OneToOneConnection(VideoEffector.class, VideoEncoder.class));
        collection.add(OneToOneConnection(AudioEffector.class, AudioEncoder.class));
        collection.add(OneToOneConnection(IMicrophoneSource.class, AudioEffector.class));
        collection.add(OneToOneConnection(IMicrophoneSource.class, AudioEncoder.class));
        collection.add(OneToOneConnection(IMediaSource.class, Render.class));

        collection.add(OneToOneConnection(VideoDecoder.class, VideoTimeScaler.class));
        collection.add(OneToOneConnection(VideoTimeScaler.class, VideoEncoder.class));

        //collection.add(OneToOneConnection(MultipleMediaSource.class, Render.class));
        collection.add(new OneToOneConnectable<AudioDecoder, AudioEffector>(AudioDecoder.class, AudioEffector.class) {
            @Override
            public boolean additionalCheck(IOutputRaw output, IInputRaw input) {
                MediaFormatType mediaFormatType = ((AudioDecoder) output).getMediaFormatType();
                return mediaFormatType == MediaFormatType.AUDIO;
            }
        });
        collection.add(new OneToOneConnectable<AudioDecoder, AudioEncoder>(AudioDecoder.class, AudioEncoder.class) {
            @Override
            public boolean additionalCheck(IOutputRaw output, IInputRaw input) {
                MediaFormatType mediaFormatType = ((AudioDecoder) output).getMediaFormatType();
                return mediaFormatType == MediaFormatType.AUDIO;
            }
        });

        collection.add(ManyToOneConnections(
            new ManyTypes(
                PassThroughPlugin.class,
                Encoder.class,
                AudioEncoder.class),
            Render.class));
        collection.add(OneToManyConnection(
            MediaSource.class,
            new ManyTypes(
                VideoDecoder.class,
                AudioDecoder.class)));
        collection.add(OneToManyConnection(
            IMediaSource.class,
            new ManyTypes(
                VideoDecoder.class,
                AudioDecoder.class,
                PassThroughPlugin.class)));

        return collection;
    }
}
