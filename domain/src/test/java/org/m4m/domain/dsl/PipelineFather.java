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

import org.m4m.domain.AudioEncoder;
import org.m4m.domain.Frame;
import org.m4m.domain.ICommandProcessor;
import org.m4m.domain.IOutput;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.Pipeline;
import org.m4m.domain.Plugin;
import org.m4m.domain.Render;
import org.m4m.domain.VideoEffector;
import org.m4m.domain.VideoEncoder;

public class PipelineFather {
    private final Father create;
    private Pipeline pipeline;
    private IOutput mediaSource;
    private VideoDecoderFather videoDecoderFather;
    private Plugin decoder;
    private Render sink;
    private ICommandProcessor commandProcessor;
    private VideoEncoderFather videoEncoderFather;
    private VideoEncoder videoEncoder;
    private Plugin audioDecoder;
    private AudioEncoder audioEncoder;
    private VideoEffectorFather effectorFather;
    private VideoEffector effector;

    public PipelineFather(Father create) {
        this.create = create;
        with(create.mediaSource().with(Frame.EOF()).construct());
        with(create.render());
        with(create.commandProcessor().construct());
    }

    public PipelineFather with(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
        return this;
    }

    public PipelineFather with(MediaSourceFather mediaSourceFather) {
        this.mediaSource = mediaSourceFather.construct();
        return this;
    }

    public PipelineFather with(MultipleMediaSource multipleMediaSource) {
        mediaSource = multipleMediaSource;
        return this;
    }

    public PipelineFather withVideoDecoder(Plugin decoder) {
        this.decoder = decoder;
        this.videoDecoderFather = null;
        return this;
    }

    public PipelineFather with(VideoDecoderFather decoder) {
        this.videoDecoderFather = decoder;
        this.decoder = null;
        return this;
    }

    public PipelineFather withVideoEffector(VideoEffector effector) {
        this.effectorFather = null;
        this.effector = effector;
        return this;
    }

    public PipelineFather withVideoEffector(VideoEffectorFather effector) {
        this.effectorFather = effector;
        this.effector = null;
        return this;
    }

    public PipelineFather with(VideoEncoder encoder) {
        this.videoEncoder = encoder;
        return this;
    }

    public PipelineFather with(VideoEncoderFather videoEncoderFather) {
        this.videoEncoderFather = videoEncoderFather;
        return this;
    }

    public PipelineFather with(Render sink) {
        this.sink = sink;
        return this;
    }

    public PipelineFather with(RenderFather sink) {
        this.sink = sink.construct();
        return this;
    }

    public PipelineFather with(ICommandProcessor processor) {
        this.commandProcessor = processor;
        return this;
    }

    public PipelineFather withAudioDecoder() {
        return withAudioDecoder(create.audioDecoder().construct());
    }

    public PipelineFather withAudioDecoder(Plugin audioDecoder) {
        this.audioDecoder = audioDecoder;
        return this;
    }

    public PipelineFather withAudioEncoder(AudioEncoder audioEncoder) {
        this.audioEncoder = audioEncoder;
        return this;
    }

    public Pipeline construct() {
        pipeline = new Pipeline(commandProcessor);
        pipeline.setMediaSource(mediaSource);
        if (videoDecoderFather != null) {
            pipeline.addVideoDecoder(videoDecoderFather.construct());
        }
        if (decoder != null) {
            pipeline.addTransform(decoder);
        }
        if (audioDecoder != null) {
            pipeline.addAudioDecoder(audioDecoder);
        }
        if (audioEncoder != null) {
            pipeline.addAudioEncoder(audioEncoder);
        }
        if (videoEncoder != null) {
            pipeline.addVideoEncoder(videoEncoder);
        }
        if (videoEncoderFather != null) {
            pipeline.addVideoEncoder(videoEncoderFather.construct());
        }
        pipeline.setSink(sink);
        return pipeline;
    }
}
