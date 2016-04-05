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

package org.m4m.domain.formatSetup;

import org.m4m.domain.dsl.VideoDecoderFather;
import org.m4m.domain.dsl.VideoFormatFake;
import org.junit.Before;
import org.junit.Test;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Command;
import org.m4m.domain.ICommandProcessor;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaExtractor;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.OutputInputPair;
import org.m4m.domain.Pair;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Pipeline;
import org.m4m.domain.Plugin;
import org.m4m.domain.Render;
import org.m4m.domain.TestBase;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhenResolvePipeline extends TestBase {
    private IMediaExtractor extractor;
    private VideoFormatFake videoFormat;

    @Before
    public void setup() {
        extractor = create.mediaExtractor().construct();

        videoFormat = create.videoFormat().construct();

        when(extractor.getTrackFormat(anyInt())).thenReturn(videoFormat);
        when(extractor.getTrackCount()).thenReturn(1);
    }

    @Test
    public void canGetOutputFormatFromSource() {
        MediaSource mediaSource = create.mediaSource().with(extractor).construct();
        assertEquals(videoFormat, mediaSource.getMediaFormatByType(MediaFormatType.VIDEO));
    }

    @Test
    public void canNotGetWrongMediaFormatFromSource() {
        MediaSource mediaSource = create.mediaSource().with(extractor).construct();
        MediaFormat mediaFormat = mediaSource.getMediaFormatByType(MediaFormatType.AUDIO);
        assertNull(mediaFormat);
    }

    @Test
    public void canGetOutputFormatFromMultipleMediaSource() throws RuntimeException {
        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(extractor).construct()).construct();

        assertEquals(videoFormat, mediaSource.getMediaFormatByType(MediaFormatType.VIDEO));
    }

    @Test
    public void throwExceptionWhenGetOutputMediaFormatWithInvalidType() throws RuntimeException {
        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().with(extractor).construct()).construct();

        MediaFormat mediaFormat = mediaSource.getMediaFormatByType(MediaFormatType.AUDIO);

        assertNull(mediaFormat);
    }

    @Test
    public void setInputFormatToDecoder() {
        Plugin plugin = new PassThroughPlugin(10, MediaFormatType.VIDEO);

        plugin.setMediaFormat(videoFormat);

        assertEquals(videoFormat, plugin.getMediaFormatByType(MediaFormatType.VIDEO));
    }

    @Test
    public void failWhenGetWrongInputFormatFromDecoder() {
        Plugin plugin = new PassThroughPlugin(10, MediaFormatType.VIDEO);

        plugin.setMediaFormat(videoFormat);

        assertNull(plugin.getMediaFormatByType(MediaFormatType.AUDIO));
    }

    @Test
    public void setInputFormatToAudioDecoder() {
        MediaSource mediaSource = create.mediaSource()
            .withVideoTrack(0)
            .with(a.audioFormat().withMimeType("audio/mp4a-latm").construct())
            .construct();
        Plugin audioDecoder = create.audioDecoder().construct();
        Pipeline pipeline = create.pipeline()
            .with(mediaSource)
            .withAudioDecoder(audioDecoder)
            .withAudioEncoder(create.audioEncoder().construct())
            .construct();

        pipeline.resolve();

        assertEquals("audio/mp4a-latm", audioDecoder.getMediaFormatByType(MediaFormatType.AUDIO).getMimeType());
    }

    @Test
    public void setInputFormatsToBothVideoAndAudioDecoders() throws RuntimeException {
        MediaSource mediaSource = create.mediaSource()
            .with(a.audioFormat().withMimeType("audio/mp4a-latm").construct())
            .with(a.videoFormat().withMimeType("video/vp8").construct())
            .with(1).videoFrames()
            .with(1).audioFrames()
            .construct();
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource().with(mediaSource).construct();
        Plugin videoDecoder = create.videoDecoder().construct();
        Plugin audioDecoder = create.audioDecoder().construct();

        Pipeline pipeline = create.pipeline()
            .with(multipleMediaSource)
            .withVideoDecoder(videoDecoder)
            .withAudioDecoder(audioDecoder)
            .withAudioEncoder(create.audioEncoder().construct())
            .with(create.videoEncoder().construct())
            .construct();

        pipeline.resolve();

        assertEquals("video/vp8", videoDecoder.getMediaFormatByType(MediaFormatType.VIDEO).getMimeType());
        assertEquals("audio/mp4a-latm", audioDecoder.getMediaFormatByType(MediaFormatType.AUDIO).getMimeType());
    }

    @Test
    public void audioEncoderIsConfiguredAndStarted() {
        MediaSource mediaSource = create.mediaSource().withVideoTrack(0).withAudioTrack(1).construct();
        IMediaCodec encoderMediaCodec = create.mediaCodec().construct();
        AudioEncoder audioEncoder = create.audioEncoder().with(encoderMediaCodec).construct();
        audioEncoder.setMediaFormat(create.audioFormat().construct());
        Pipeline pipeline = create.pipeline()
            .with(mediaSource)
            .withAudioDecoder()
            .withAudioEncoder(audioEncoder)
            .construct();

        pipeline.resolve();

        verify(encoderMediaCodec).configure(any(MediaFormat.class), eq((ISurfaceWrapper) null), eq(IMediaCodec.CONFIGURE_FLAG_ENCODE));
        verify(encoderMediaCodec).start();
        verify(encoderMediaCodec, never()).createInputSurface();
    }

    @Test
    public void audioEncoderShouldNotCreateSurface() throws RuntimeException {
        MediaSource mediaSource = create.mediaSource().withVideoTrack(0).withAudioTrack(1).construct();
        MultipleMediaSource multipleMediaSource = create.multipleMediaSource().with(mediaSource).construct();
        IMediaCodec encoderMediaCodec = create.mediaCodec().construct();
        AudioEncoder audioEncoder = create.audioEncoder().with(encoderMediaCodec).construct();
        audioEncoder.setMediaFormat(create.audioFormat().construct());
        Pipeline pipeline = create.pipeline()
            .with(multipleMediaSource)
            .withAudioDecoder()
            .withAudioEncoder(audioEncoder)
            .construct();

        pipeline.resolve();

        verify(encoderMediaCodec, never()).createInputSurface();
    }

    @Test(expected = IllegalStateException.class)
    public void failResolveWhenMediaSourceIsNotInitialized() {
        create.pipeline().with((MediaSource) null).construct().resolve();
    }

    @Test(expected = IllegalStateException.class)
    public void failResolveWhenSinkIsNotInitialized() {
        create.pipeline().with((Render) null).construct().resolve();
    }

    @Test
    public void pullEofAfterEofReached() throws RuntimeException {
        MultipleMediaSource mediaSource = create.multipleMediaSource()
            .with(create.mediaSource().withVideoTrack(0).withAudioTrack(1).construct())
            .construct();

        Pipeline pipeline = create.pipeline()
            .with(mediaSource)
            .withAudioDecoder()
            .withVideoDecoder(create.videoDecoder().construct())
            .with(create.videoEncoder().construct())
            .withAudioEncoder(create.audioEncoder().construct())
            .construct();
        pipeline.resolve();

        assertThat(mediaSource.getOutputCommandQueue()).equalsTo(new Pair<Command, Integer>(Command.EndOfFile, 0), new Pair<Command, Integer>(Command.EndOfFile, 1));
    }

    @Test
    public void configureAudeoEncoderSampleRateWithSetMediaFormat() {
        MediaSource mediaSource = create.mediaSource()
            .withVideoTrack(0)
            .with(a.audioFormat().withSampleRate(123).construct())
            .construct();
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.setMediaFormat(create.audioFormat().withSampleRate(456).withBitRate(10000).construct());

        Pipeline pipeline = create.pipeline()
            .with(mediaSource)
            .withAudioDecoder(create.audioDecoder().construct())
            .withAudioEncoder(encoder)
            .construct();
        pipeline.resolve();

        assertEquals(456, encoder.getSampleRate());
        assertEquals(10000, encoder.getBitRate());
    }

    @Test
    public void configureAudeoEncoderChannelCountWithSetMediaFormat() {
        MediaSource mediaSource = create.mediaSource()
            .withVideoTrack(0)
            .with(a.audioFormat().withChannelCount(5).construct())
            .construct();
        AudioEncoder encoder = create.audioEncoder().construct();
        encoder.setMediaFormat(create.audioFormat().withChannelCount(10).withBitRate(10000).construct());

        Pipeline pipeline = create.pipeline()
            .with(mediaSource)
            .withAudioEncoder(encoder)
            .withAudioDecoder(create.audioDecoder().construct())
            .construct();
        pipeline.resolve();

        assertEquals(10, encoder.getChannelCount());
        assertEquals(10000, encoder.getBitRate());
    }

    @Test
    public void commandProcessorAlsoConfigured() {
        ICommandProcessor commandProcessor = spy(create.commandProcessor().construct());
        VideoDecoder decoder = create.videoDecoder().construct();
        VideoEncoder encoder = create.videoEncoder().construct();
        create.pipeline().with(commandProcessor).withVideoDecoder(decoder).with(encoder).construct().resolve();

        verify(commandProcessor, times(3)).add(any(OutputInputPair.class));
    }

    @Test
    public void mediaCodecIsStarted() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoderFather decoder = create.videoDecoder().with(mediaCodec);
        VideoEncoder encoder = create.videoEncoder().construct();
        encoder.setMediaFormat(create.videoFormat().construct());

        create.pipeline().with(decoder).with(encoder).construct().resolve();

        verify(mediaCodec).start();
    }

    @Test
    public void decoderIsConfiguredWithSurface() {
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoderFather decoder = create.videoDecoder().with(mediaCodec);
        ISurfaceWrapper container = create.surfaceContainer().construct();
        ISurface surface = create.surface().with(container).construct();
        VideoEncoder encoder = create.videoEncoder().with(surface).construct();
        encoder.setMediaFormat(create.videoFormat().construct());

        create.pipeline().with(decoder).with(encoder).construct()
            .resolve();

        verify(mediaCodec).configure(any(MediaFormat.class), eq(container), eq(0));
    }

    @Test
    public void encoderIsConfigured() {
        VideoDecoderFather decoder = create.videoDecoder();
        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoEncoder encoder = create.videoEncoder().with(mediaCodec).construct();
        encoder.setMediaFormat(create.videoFormat().construct());

        create
            .pipeline().with(decoder).with(encoder).construct()
            .resolve();

        verify(mediaCodec).configure(any(MediaFormat.class), eq((ISurfaceWrapper) null), eq(IMediaCodec.CONFIGURE_FLAG_ENCODE));
    }

    @Test
    public void decoderMediaFormatIsConfigured() {
        MediaFormat mediaFormat = create.videoFormat().construct();
        MediaSource mediaSource = create.mediaSource().with(mediaFormat).construct();

        IMediaCodec mediaCodec = create.mediaCodec().construct();
        VideoDecoderFather decoder = create.videoDecoder().with(mediaCodec);
        VideoEncoder encoder = create.videoEncoder().construct();
        Render render = create.render().construct();
        encoder.setMediaFormat(create.videoFormat().construct());

        create.pipeline().with(mediaSource).with(decoder).with(encoder).with(render).construct()
            .resolve();

        verify(mediaCodec).configure(eq(mediaFormat), any(ISurfaceWrapper.class), anyInt());
    }
}
