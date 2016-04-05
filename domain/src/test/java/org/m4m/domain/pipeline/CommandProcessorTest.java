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

import org.m4m.domain.dsl.CommandProcessorSpy;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.m4m.domain.Command;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Render;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;

import javax.naming.OperationNotSupportedException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


public class CommandProcessorTest extends TestBase {
    private boolean[] onDone;

    @Before
    public void SetUp() {
        onDone = new boolean[]{false};
    }

    @Test
    public void frameFromExtractorReachesMuxer() {
        Frame frame = create.frame(1, 2, 3).withFlag(8).withTimeStamp(555).withTrackId(0).construct();
        final MediaSource mediaSource = create.mediaSource()
            .with(frame)
            .with(Frame.EOF())
            .construct();
        mediaSource.selectTrack(0);
        mediaSource.start();

        final PassThroughPlugin passThroughPlugin = new PassThroughPlugin(frame.getLength(), MediaFormatType.VIDEO);
        passThroughPlugin.setMediaFormat(create.videoFormat().construct());
        passThroughPlugin.setTrackId(0);

        IMediaMuxer muxer = create.mediaMuxer().construct();
        final Render render = create.render().with(muxer).construct();
        render.configure();

        final CommandProcessorSpy commandProcessor = create.commandProcessor()
            .withPushFrameModel(mediaSource, passThroughPlugin, passThroughPlugin)
            .withPullFrameModel(passThroughPlugin, render)
            .construct();

        IOnStopListener onStopListener = new IOnStopListener() {
            @Override
            public void onStop() {
                commandProcessor.stop();
            }
        };
        render.addOnStopListener(onStopListener);

        commandProcessor.process();

        assertThat(mediaSource.getOutputCommandQueue()).isEmpty();
        assertThat(passThroughPlugin.getInputCommandQueue()).isEmpty();

        assertThat(passThroughPlugin.getOutputCommandQueue()).isEmpty();
        assertThat(render.getInputCommandQueue()).isEmpty();

        IMediaCodec.BufferInfo info = new IMediaCodec.BufferInfo();
        info.size = 3;
        info.flags = 8;
        info.presentationTimeUs = 555;
        verify(muxer).writeSampleData(eq(0), eq(create.byteBuffer(1, 2, 3)), any(IMediaCodec.BufferInfo.class));

        assertThat(commandProcessor)
                .processed(mediaSource, passThroughPlugin)
                .processed(passThroughPlugin, render);
    }

    @Test
    @Ignore
    public void shouldFeedDecoderWithBothStreams() throws RuntimeException {
        MultipleMediaSource mediaSource = create.multipleMediaSource()
                .with(create.mediaSource().with(1).videoFrames())
                .with(create.mediaSource().with(1).videoFrames())
                .construct();
        mediaSource.selectTrack(0);
        mediaSource.start();
        mediaSource.incrementConnectedPluginsCount();

        IMediaMuxer muxer = create.mediaMuxer().construct();
        final Render render = create.render().with(muxer).construct();
        render.configure();

        VideoDecoder videoDecoder = create.videoDecoder().withDequeueOutputBufferIndex(0, -1, 0).construct();
        videoDecoder.setTrackId(0);
        videoDecoder.start();

        final CommandProcessorSpy commandProcessor = create.commandProcessor()
                .withPushFrameModel(mediaSource, mediaSource, videoDecoder)
                .withOutputFormatChangedModel(mediaSource, videoDecoder)
                .withPullFrameModel(videoDecoder, render)
                .construct();

        IOnStopListener onStopListener = new IOnStopListener() {
            @Override
            public void onStop() {
                commandProcessor.stop();
            }
        };
        render.addOnStopListener(onStopListener);

        commandProcessor.process();

        assertThat(commandProcessor)
                .processed(mediaSource, videoDecoder)
                .commands(Command.HasData, Command.NeedInputFormat) // frame from stream 1
                .commands(Command.HasData, Command.NeedData) // frame from stream 1
                .commands(Command.OutputFormatChanged, Command.NeedData) // frame from stream 1
                .commands(Command.HasData, Command.NeedInputFormat) // frame from stream 2
                .commands(Command.HasData, Command.NeedData) // frame from stream 2
                .commands(Command.EndOfFile, Command.NeedData) // frame from stream 2
                .processed(videoDecoder, render);
    }

    @Test
    public void canPauseAndResume() throws InterruptedException, OperationNotSupportedException {
        MediaSource mediaSource = create.mediaSource().with(1).videoFrames().construct();
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource().with(mediaSource).construct();
        multipleMediaSource.selectTrack(0);
        multipleMediaSource.start();
        multipleMediaSource.incrementConnectedPluginsCount();

        IMediaMuxer muxer = create.mediaMuxer().construct();
        final Render render = create.render().with(muxer).construct();
        render.configure();

        VideoDecoder decoder = create.videoDecoder().withDequeueInputBufferIndex(0).construct();
        decoder.setTrackId(0);
        decoder.start();

        final CommandProcessorSpy commandProcessor = create.commandProcessor()
                .withPushFrameModel(multipleMediaSource, multipleMediaSource, decoder)
                .withPullFrameModel(decoder, render)
                .onDone(new Runnable() {
                    @Override
                    public void run() {
                        onDone[0] = true;
                    }
                })
                .construct();

        IOnStopListener onStopListener = new IOnStopListener() {
            @Override
            public void onStop() {
                commandProcessor.stop();
            }
        };
        render.addOnStopListener(onStopListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                commandProcessor.process();
                onDone[0] = true;
            }
        }).start();

        commandProcessor.pause();
        commandProcessor.resume();
        waitUntilDone();

        assertThat(commandProcessor).processed(multipleMediaSource, decoder);
    }

    private void waitUntilDone() throws InterruptedException {
        while (!onDone[0]) {
            Thread.sleep(100);
        }
    }

//
//    @Test
//    @Ignore
//    public void shouldWaitForAllDrained() {
//        MultipleMediaSource mediaSource = create.multipleMediaSource()
//            .with(create.mediaSource().with(2).videoFrames())
//            .construct();
//        mediaSource.start();
//        Decoder decoder = create.videoDecoder().withDequeueOutputBufferIndex(-1, -1, -1, 0, -1, 0).construct();
//        Encoder encoder = create.videoEncoder().construct();
//        CommandProcessorSpy commandProcessor = create.commandProcessor()
//            .withPushFrameModel(mediaSource, decoder)
//            .withEofModel(mediaSource, decoder)
//            .withSurfaceModel(decoder, encoder)
//            .construct();
//        decoder.setTrackId(0);
//        decoder.start();
//        encoder.start();
//
//        commandProcessor.process();
//
//        assertThat(commandProcessor)
//            .processed(mediaSource, decoder)
//            .commands(Command.HasData, Command.NeedData) // frame 1
//            .commands(Command.HasData, Command.NeedData) // frame 2
//            .commands(Command.EndOfFile, Command.NeedData) // EOF
//            .processed(decoder, encoder)
//            .commands(Command.HasData, Command.NeedData) // frame 1
//            .commands(Command.HasData, Command.NeedData);// frame 2
//    }
}
