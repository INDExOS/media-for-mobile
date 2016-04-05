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
import org.m4m.domain.Command;
import org.m4m.domain.CommandHandlerFactory;
import org.m4m.domain.Encoder;
import org.m4m.domain.ICameraSource;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.ICommandProcessor;
import org.m4m.domain.IEglContext;
import org.m4m.domain.IFrameAllocator;
import org.m4m.domain.IHandlerCreator;
import org.m4m.domain.IMediaSource;
import org.m4m.domain.IMicrophoneSource;
import org.m4m.domain.IOnSurfaceReady;
import org.m4m.domain.IOutput;
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceListener;
import org.m4m.domain.IVideoOutput;
import org.m4m.domain.MediaCodecPlugin;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.OutputInputPair;
import org.m4m.domain.Pair;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Plugin;
import org.m4m.domain.Render;
import org.m4m.domain.SurfaceRender;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEffector;
import org.m4m.domain.VideoEncoder;
import org.m4m.domain.VideoTimeScaler;

class PluginConnector implements IConnector {
    private final ICommandProcessor commandProcessor;

    public PluginConnector(ICommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public void connect(final IMediaSource mediaSource, final Plugin decoder) {
        int trackId = mediaSource.getTrackIdByMediaType(decoder.getMediaFormatType());
        mediaSource.selectTrack(trackId);
        decoder.setTrackId(trackId);

        configureCommandProcessorPush(mediaSource, mediaSource, decoder);
    }

    public void connect(final VideoDecoder decoder, final VideoEncoder videoEncoder) {
        configureCommandProcessorPushSurfaceDecoderEncoder(decoder, videoEncoder);

        videoEncoder.configure();
        ISurface surface = videoEncoder.getSurface();
        decoder.setOutputSurface(surface);
        videoEncoder.start();

        decoder.configure();
        decoder.start();
    }

    public void connect(final AudioDecoder decoder, final AudioEncoder encoder, AudioFormat mediaFormat) {

        if (mediaFormat == null) {
            throw new UnsupportedOperationException("Audio format not specified.");
        }

        configureAudioPipelineCommandProcessorCopy(decoder, encoder);

        decoder.configure();
        decoder.start();

        encoder.configure();
        encoder.start();
    }

    public void connect(final VideoEffector effector, final VideoEncoder encoder) {
        configureCommandProcessorPushSurfaceEffector(effector, encoder);

        effector.onSurfaceAvailable(new ISurfaceListener() {
            @Override
            public void onSurfaceAvailable(IEglContext eglContext) {
                encoder.configure();

                ISurface surface = encoder.getSimpleSurface(eglContext);
                effector.setOutputSurface(surface);

                effector.configure();
                effector.start();
                encoder.start();
            }
        });
    }

    public void connect(VideoDecoder decoder, VideoEffector effector) {
        configureCommandProcessorPushSurfaceEffector(decoder, decoder, effector);

        ISurface surface = effector.getSurface();
        decoder.setOutputSurface(surface);

        decoder.configure();
        decoder.start();
    }

    public void connect(AudioEffector effector, AudioEncoder audioEncoder, AudioFormat mediaFormat) {

        if (mediaFormat == null) {
            throw new UnsupportedOperationException("Audio format not specified.");
        }

        configureAudioPipelineCommandProcessorCopy(effector, audioEncoder);

        /*audioEncoder.setSampleRate(mediaFormat.getAudioSampleRateInHz());
        audioEncoder.setChannelCount(mediaFormat.getAudioChannelCount());*/
        audioEncoder.configure();
        audioEncoder.start();

        effector.configure();
        effector.start();
    }

    public void connect(AudioDecoder decoder, AudioEffector effector) {
        configureAudioPipelineCommandProcessorCopy(decoder, effector);

        decoder.configure();
        decoder.start();
    }

    private void configureCommandProcessorPush(final IOutput mediaSource,
                                               final IVideoOutput videoOutput, final Plugin decoder) {
        if (decoder instanceof IFrameAllocator) {
            CommandHandlerFactory factory = new CommandHandlerFactory();
            factory.register(new Pair<Command, Integer>(Command.HasData, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new PushDataCommandHandler(mediaSource, decoder, (IFrameAllocator) decoder);
                }

            });
            factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new OutputFormatChangedHandler(mediaSource, decoder, (IFrameAllocator) decoder);
                }

            });
            factory.register(new Pair<Command, Integer>(Command.EndOfFile, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new EofCommandHandler(mediaSource, decoder, (IFrameAllocator) decoder);
                }

            });
            factory.register(new Pair<Command, Integer>(Command.EndOfFile, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedInputFormat, decoder.getTrackId()), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new EofCommandHandler(mediaSource, decoder, (IFrameAllocator) decoder);
                }

            });
            factory.register(new Pair<Command, Integer>(Command.HasData, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedInputFormat, decoder.getTrackId()), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new ConfigureVideoDecoderCommandHandler(videoOutput, decoder);
                }

            });
            commandProcessor.add(new OutputInputPair(mediaSource, decoder, factory));
        }
        decoder.setMediaFormat(mediaSource.getMediaFormatByType(decoder.getMediaFormatType()));
        mediaSource.incrementConnectedPluginsCount();
    }

    private void configureCommandProcessorPushSurfaceDecoderEncoder(final IPluginOutput decoder, final MediaCodecPlugin encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandler(decoder, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        commandProcessor.add(new OutputInputPair(decoder, encoder, factory));
    }

    private void configureCommandProcessorPushSurfaceSurfaceRender(final IPluginOutput decoder, final SurfaceRender render) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandlerForSurfaceRender(decoder, render);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(render);
            }

        });
        commandProcessor.add(new OutputInputPair(decoder, render, factory));
    }


    private void configureCommandProcessorPushSurfaceEffector(final IPluginOutput decoder, final MediaCodecPlugin encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandlerForEffector(decoder, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        commandProcessor.add(new OutputInputPair(decoder, encoder, factory));
    }

    private void configureCommandProcessorPushSurfaceEffector(final IPluginOutput decoder, final IVideoOutput videoOutput, final MediaCodecPlugin encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandlerForEffector(decoder, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new ConfigureVideoEffectorCommandHandler(videoOutput, encoder);
            }

        });
        commandProcessor.add(new OutputInputPair(decoder, encoder, factory));
    }

    private void configureAudioPipelineCommandProcessorCopy(final MediaCodecPlugin decoder, final MediaCodecPlugin encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new CopyDataCommandHandler(decoder, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new AudioPipelineOutputFormatChangeCommandHandler(decoder, encoder);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }

        });
        commandProcessor.add(new OutputInputPair(decoder, encoder, factory));
    }

    private void configureCommandProcessorPushSurfaceEffector2(final MediaCodecPlugin scaler, final MediaCodecPlugin effector) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandlerForEffector(scaler, effector);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(effector);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(effector);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new ConfigureVideoTimeScalerVideoEffectorCommand(scaler, effector);
            }
        });
        commandProcessor.add(new OutputInputPair(scaler, effector, factory));
    }

    public void connect(final ICaptureSource source, final Encoder encoder) {
        configureCommandProcessorPushSurfaceDecoderEncoder(source, encoder);

        source.addSetSurfaceListener(new ISurfaceListener() {
            @Override
            public void onSurfaceAvailable(IEglContext eglContext) {
                encoder.configure();
                ISurface surface = encoder.getSurface();
                source.setOutputSurface(surface);
                encoder.start();
            }
        });
    }

    public void connect(final VideoDecoder decoder, final SurfaceRender render) {
        configureCommandProcessorPushSurfaceSurfaceRender(decoder, render);

        render.onSurfaceAvailable(new IOnSurfaceReady() {
            @Override
            public void onSurfaceReady() {
                decoder.setOutputSurface(render.getSurface());
                decoder.configure();
                decoder.start();
            }
        });
    }

    public void connect(final IPluginOutput plugin, final Render render) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        if (plugin instanceof Encoder || plugin instanceof PassThroughPlugin) {
            factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new EncoderMediaFormatChangedCommandHandler((Plugin) plugin, render);
                }

            });
        }
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullDataCommandHandler(plugin, render);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullDataCommandHandler(plugin, render);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainRenderCommandHandler(render);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainRenderCommandHandler(render);
            }

        });
        commandProcessor.add(new OutputInputPair(plugin, render, factory));
        render.configure();
    }

    public void connect(final IMediaSource source, final Render render) {
        CommandHandlerFactory factory = new CommandHandlerFactory();

        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushNewDataCommandHandler(source, render);
            }
        });

        commandProcessor.add(new OutputInputPair(source, render, factory));
        render.setMediaFormat(source.getMediaFormatByType(MediaFormatType.VIDEO));

        render.configure();
        render.getInputCommandQueue().clear();

        render.start();

        int trackId = source.getTrackIdByMediaType(MediaFormatType.VIDEO);
        source.selectTrack(trackId);
    }

    public void connect(final ICameraSource source, final Encoder encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new CaptureSourcePullSurfaceCommandHandler(source, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        commandProcessor.add(new OutputInputPair(source, encoder, factory));

        encoder.configure();
        source.setOutputSurface(encoder.getSurface());
        encoder.start();
        source.configure();
    }

    public void connect(final IMicrophoneSource source, final AudioEffector effector) {
        configureAudioPipelineWithEffectorCommandProcessorCopy(source, effector);

        effector.reInitInputCommandQueue();

        effector.configure();
        effector.start();
    }

    private void configureAudioPipelineWithEffectorCommandProcessorCopy(final IMicrophoneSource source, final MediaCodecPlugin effector) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullFrameToEffectorFromMicrophoneSourceCommandHandler(source, effector);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(effector);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(effector);
            }
        });
        commandProcessor.add(new OutputInputPair(source, effector, factory));
    }

    public void connect(final IMicrophoneSource source, final AudioEncoder encoder) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new MicrophoneSourcePullFrameCommandHandler(source, encoder);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(encoder);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(encoder);
            }
        });
        commandProcessor.add(new OutputInputPair(source, encoder, factory));


        encoder.configure();
        encoder.start();
    }

    public void connect(final ICameraSource camera, final VideoEffector effector) {
        CommandHandlerFactory factory = new CommandHandlerFactory();
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new CaptureSourcePullSurfaceCommandHandler(camera, effector);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new SkipOutputFormatChangeCommandHandler(effector);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new ConfigureVideoEffectorCommandHandler(camera, effector);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new ConfigureVideoEffectorCommandHandler(camera, effector);
            }

        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(effector);
            }
        });
        factory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandler(effector);
            }
        });
        commandProcessor.add(new OutputInputPair(camera, effector, factory));

        effector.onSurfaceAvailable(new ISurfaceListener() {
            @Override
            public void onSurfaceAvailable(IEglContext eglContext) {
                camera.setPreview(effector.getPreview());
                camera.setOutputSurface(effector.getSurface());
                camera.configure();
            }
        });
    }

    public void connect(VideoDecoder decoder, VideoTimeScaler scaler) {

        configureCommandProcessorPushSurfaceEffector(decoder, decoder, scaler);

        ISurface surface = scaler.getSurface();
        decoder.setOutputSurface(surface);

        decoder.configure();
        decoder.start();
    }

    public void connect(final VideoTimeScaler scaler, final VideoEncoder encoder) {
        configureCommandProcessorPushSurfaceEffector(scaler, encoder);

        scaler.onSurfaceAvailable(new ISurfaceListener() {
            @Override
            public void onSurfaceAvailable(IEglContext eglContext) {
                encoder.configure();

                ISurface surface = encoder.getSimpleSurface(eglContext);
                scaler.setOutputSurface(surface);

                scaler.configure();
                scaler.start();
                encoder.start();
            }
        });
    }
}
