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
import org.m4m.IVideoEffect;
import org.m4m.MediaComposer;
import org.m4m.VideoFormat;
import org.m4m.domain.FileSegment;
import org.m4m.domain.Frame;
import org.m4m.domain.IFrameBuffer;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.Render;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEffector;

import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

public class MediaComposerFather {
    private final Father create;
    private Render render;
    private MediaSourceFather mediaSourceFather;
    private IProgressListener progressListener;
    private ArrayList<String> fileNames = new ArrayList<String>();
    private AndroidMediaObjectFactoryFake androidMediaObjectFactory;
    private MediaCodecFather decoderMediaCodecFather;
    private MediaCodecFather encoderMediaCodecFather;
    private int frameCount = 0;
    private IVideoEffect videoEffect;
    private VideoDecoder videoDecoder;
    private IMediaMuxer muxer;

    public MediaComposerFather(Father create) {
        this.create = create;
        mediaSourceFather = create.mediaSource();
        decoderMediaCodecFather = create.mediaCodec();
        encoderMediaCodecFather = create.mediaCodec()
                .withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED);
        androidMediaObjectFactory = new AndroidMediaObjectFactoryFake(create);
    }

    public MediaComposerFather with(IProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public MediaComposerFather withSourceFile(String fileName) {
        fileNames.add(fileName);
        return this;
    }

    public MediaComposerFather withDuration(int durationInMicroseconds) {
        mediaSourceFather.withDuration(durationInMicroseconds);
        return this;
    }

    public MediaComposerFather with(Frame frame) {
        mediaSourceFather.with(frame);

        // The following logic to be removed when we have "smart" MediaCodec mock
        // (the one that can output input frames).
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = frame.getFlags();
        bufferInfo.presentationTimeUs = frame.getSampleTime();
        bufferInfo.size = frame.getLength();

        decoderMediaCodecFather.withOutputBuffer();
        decoderMediaCodecFather.withOutputBufferInfo(bufferInfo);
        decoderMediaCodecFather.withDequeueOutputBufferIndex(IMediaCodec.INFO_OUTPUT_FORMAT_CHANGED, frameCount);

        encoderMediaCodecFather.withOutputBuffer();
        encoderMediaCodecFather.withOutputBufferInfo(bufferInfo);
        encoderMediaCodecFather.withDequeueOutputBufferIndex(frameCount);
        //

        frameCount++;
        return this;
    }

    public MediaComposerFather with(MediaSourceFather mediaSourceFather) {
        this.mediaSourceFather = mediaSourceFather;
        return this;
    }

    public MediaComposerFather withVideoEffect(IVideoEffect videoEffect) {
        this.videoEffect = videoEffect;
        when(videoEffect.getSegment()).thenReturn(new FileSegment(0L, 0L));
        return this;
    }

    public MediaComposerFather withVideoEffectAndSegment(IVideoEffect videoEffect) {
        this.videoEffect = videoEffect;
        return this;
    }

    public MediaComposerFather withDecoderMediaCodec(IMediaCodec mediaCodec) {
        this.videoDecoder = create.videoDecoder().with(mediaCodec).construct();
        return this;
    }

    public MediaComposerFather withVideoEffector(VideoEffector videoEffector) {
        androidMediaObjectFactory.withVideoEffector(videoEffector);
        return this;
    }

    public MediaComposerFather withFrameBuffer(IFrameBuffer fakeFB) {
        androidMediaObjectFactory.withFrameBuffer(fakeFB);
        return this;
    }

    public MediaComposer construct() throws RuntimeException {
        buildMediaObjects();
        MediaComposer mediaComposer = new MediaComposer(androidMediaObjectFactory, progressListener);
        setVideoFormat(mediaComposer);
        addSourceFiles(mediaComposer);
        addTargetFile(mediaComposer);
        if (videoEffect != null) {
            mediaComposer.addVideoEffect(videoEffect);
        }
        return mediaComposer;
    }

    private void buildMediaObjects() {
        if (fileNames.isEmpty()) {
            androidMediaObjectFactory.withMediaSource(mediaSourceFather.construct());
        }
        if (videoDecoder == null)
            videoDecoder = create.videoDecoder().with(decoderMediaCodecFather.construct()).construct();
        androidMediaObjectFactory.withVideoDecoder(videoDecoder);
        androidMediaObjectFactory.withVideoEncoder(create.videoEncoder().with(encoderMediaCodecFather.construct()).construct());
    }

    private void setVideoFormat(MediaComposer mediaComposer) {
        VideoFormatFake videoFormat = create.videoFormat().construct();
        mediaComposer.setTargetVideoFormat(videoFormat);
    }

    private void addSourceFiles(MediaComposer mediaComposer) throws RuntimeException {
        try {
            if (fileNames.isEmpty()) fileNames.add("");
            for (String fileName : fileNames) {
                mediaComposer.addSourceFile(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTargetFile(MediaComposer mediaComposer) {
        try {
            mediaComposer.setTargetFile("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaComposerFather withFactory(AndroidMediaObjectFactoryFake factory) {
        androidMediaObjectFactory = factory;
        return this;
    }

    public MediaComposerFather withInputResolution(int width, int height) {
        VideoFormat videoFormat = create.videoFormat().withFrameSize(width, height).construct();
        decoderMediaCodecFather.withOutputFormat(videoFormat);
        videoDecoder = create.videoDecoder().with(decoderMediaCodecFather.construct()).construct();
        return this;
    }

    public MediaComposerFather withRender(Render render) {
        androidMediaObjectFactory.withSink(render);
        return this;
    }

    public MediaComposerFather withDecoderMediaCodecInputBuffers(int... inputBufferIndexes) {
        decoderMediaCodecFather.withDequeueInputBufferIndex(inputBufferIndexes);
        return this;
    }

    public MediaComposerFather with(IMediaMuxer muxer) {
        androidMediaObjectFactory.withSink(create.render().with(muxer).construct());
        return this;
    }
}
