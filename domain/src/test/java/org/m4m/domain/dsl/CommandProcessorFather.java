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

import org.m4m.IProgressListener;
import org.m4m.domain.Command;
import org.m4m.domain.CommandHandlerFactory;
import org.m4m.domain.Encoder;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.IFrameAllocator;
import org.m4m.domain.IHandlerCreator;
import org.m4m.domain.IInput;
import org.m4m.domain.IOutput;
import org.m4m.domain.IVideoOutput;
import org.m4m.domain.OutputInputPair;
import org.m4m.domain.Pair;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Plugin;
import org.m4m.domain.ProgressListenerStub;
import org.m4m.domain.Render;
import org.m4m.domain.VideoDecoder;

import org.m4m.domain.pipeline.*;

public class CommandProcessorFather {
    private final CommandProcessorSpy processor;
    private final CommandHandlerFactory mediaSourceDecoderCommandHandlerFactory;
    private final CommandHandlerFactory decoderEncoderCommandHandlerFactory;
    private final CommandHandlerFactory mediaSourcePassThroughPluginCommandHandlerFactory;
    private final CommandHandlerFactory pluginRenderCommandHandlerFactory;
    private final IProgressListener progressListener;
    private Runnable onDone;

    public CommandProcessorFather() {

        progressListener = new ProgressListenerStub() {
            @Override
            public void onMediaDone() {
                onDone.run();
            }
        };

        processor = new CommandProcessorSpy(progressListener);

        mediaSourceDecoderCommandHandlerFactory = new CommandHandlerFactory();
        decoderEncoderCommandHandlerFactory = new CommandHandlerFactory();

        mediaSourcePassThroughPluginCommandHandlerFactory = new CommandHandlerFactory();
        pluginRenderCommandHandlerFactory = new CommandHandlerFactory();
    }

    public CommandProcessorFather with(IOutput output, IInput input, CommandHandlerFactory commandHandlerFactory) {
        processor.add(new OutputInputPair(output, input, commandHandlerFactory));
        return this;
    }

    public CommandProcessorFather withPushFrameModel(final IOutput input, final IVideoOutput videoOutput, final VideoDecoder decoder) {
        mediaSourceDecoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushDataCommandHandlerSpy(input, decoder, decoder, processor);
            }
        });
        mediaSourceDecoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.EndOfFile, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new EofCommandHandlerSpy(input, decoder, decoder, processor);
            }
        });
        mediaSourceDecoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedInputFormat, decoder.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new ConfigureVideoDecoderCommandHandlerSpy(videoOutput, decoder, processor);
            }
        });
        mediaSourceDecoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new OutputFormatChangedHandlerSpy(input, decoder, decoder, processor);
            }
        });

        return with(input, decoder, mediaSourceDecoderCommandHandlerFactory);
    }

    public CommandProcessorFather withPushFrameModel(final IOutput output, final Plugin plugin, final IFrameAllocator frameAllocator)
    {
        mediaSourcePassThroughPluginCommandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, plugin.getTrackId()), new Pair<Command, Integer>(Command.NeedData, plugin.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushDataCommandHandlerSpy(output, plugin, frameAllocator, processor);
            }
        });
        mediaSourcePassThroughPluginCommandHandlerFactory.register(new Pair<Command, Integer>(Command.EndOfFile, plugin.getTrackId()), new Pair<Command, Integer>(Command.NeedData, plugin.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new EofCommandHandlerSpy(output, plugin, frameAllocator, processor);
            }
        });

        return with(output, plugin, mediaSourcePassThroughPluginCommandHandlerFactory);
    }

    public CommandProcessorFather withOutputFormatChangedModel(final IOutput input, final VideoDecoder decoder) {
        mediaSourceDecoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, decoder.getTrackId()), new Pair<Command, Integer>(Command.NeedData, decoder.getTrackId()), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new OutputFormatChangedHandlerSpy(input, decoder, decoder, processor);
            }
        });
        return with(input, decoder, mediaSourceDecoderCommandHandlerFactory);
    }

    public CommandProcessorFather withSurfaceModel(final VideoDecoder decoder, final Encoder encoder) {
        decoderEncoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushSurfaceCommandHandlerSpy(decoder, encoder, processor);
            }
        });
        decoderEncoderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainCommandHandlerSpy(decoder, encoder, processor);
            }
        });
        return with(decoder, encoder, decoderEncoderCommandHandlerFactory);
    }

    public CommandProcessorFather withPullFrameModel(final Plugin plugin, final Render render)
    {
        if (plugin instanceof Encoder || plugin instanceof PassThroughPlugin) {
            pluginRenderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.OutputFormatChanged, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
                @Override
                public ICommandHandler create() {
                    return new EncoderMediaFormatChangedCommandHandlerSpy(plugin, render, processor);
                }
            });
        }
        pluginRenderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullDataCommandHandlerSpy(plugin, render, processor);
            }
        });
        pluginRenderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PullDataCommandHandlerSpy(plugin, render, processor);
            }
        });
        pluginRenderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainRenderCommandHandlerSpy(plugin, render, processor);
            }
        });
        pluginRenderCommandHandlerFactory.register(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.NeedInputFormat, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new DrainRenderCommandHandlerSpy(plugin, render, processor);
            }
        });
        return with(plugin, render, pluginRenderCommandHandlerFactory);
    }

    public CommandProcessorFather onDone(Runnable onDone) {
        this.onDone = onDone;
        return this;
    }

    public CommandProcessorSpy construct() {
        return processor;
    }
}
