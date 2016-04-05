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

import org.m4m.domain.mediaComposer.ProgressListenerFake;
import org.junit.Assert;
import org.junit.Test;
import org.m4m.domain.Command;
import org.m4m.domain.CommandHandlerFactory;
import org.m4m.domain.ICommandHandler;
import org.m4m.domain.IHandlerCreator;
import org.m4m.domain.MediaSource;
import org.m4m.domain.Pair;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import static org.hamcrest.CoreMatchers.instanceOf;

public class CommandHandlerFactoryTest extends TestBase {
    @Test
    public void canConfigureFactoryWithHandler() {
        final MediaSource mediaSource = create.mediaSource().construct();
        final VideoDecoder decoder = create.videoDecoder().construct();

        CommandHandlerFactory commandHandlerFactory = new CommandHandlerFactory();
        commandHandlerFactory.register(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new IHandlerCreator() {
            @Override
            public ICommandHandler create() {
                return new PushDataCommandHandler(mediaSource, decoder, decoder);
            }

        });

        ICommandHandler commandHandler = commandHandlerFactory.create(new Pair<Command, Integer>(Command.HasData, 0), new Pair<Command, Integer>(Command.NeedData, 0), new ProgressListenerFake());
        Assert.assertThat(commandHandler, instanceOf(PushDataCommandHandler.class));
    }
}
