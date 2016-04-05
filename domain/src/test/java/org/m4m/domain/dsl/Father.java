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
import org.m4m.IRecognitionPlugin;
import org.m4m.IVideoEffect;
import org.m4m.domain.CameraCaptureFather;
import org.m4m.domain.FileSegment;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Father {
    public FrameFather frame(int... bytes) {
        if (bytes.length == 0) bytes = new int[] {0};
        return new FrameFather(this).withBuffer(bytes);
    }

    public ByteBuffer byteBuffer(int... bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        for (int b : bytes) {
            buffer.put((byte) b);
        }
        buffer.rewind();
        return buffer;
    }

    public MediaExtractorFather mediaExtractor() {
        return new MediaExtractorFather(this);
    }

    public MediaCodecFather mediaCodec() {
        return new MediaCodecFather(this);
    }

    public VideoDecoderFather videoDecoder() {
        return new VideoDecoderFather(this);
    }

    public AudioDecoderFather audioDecoder() {
        return new AudioDecoderFather(this);
    }

    public RenderFather render() {
        return new RenderFather(this);
    }

    public RenderFather muxRender () {
        return new RenderFather(this);
    }

    public PipelineFather pipeline() {
        return new PipelineFather(this);
    }

    public MediaSourceFather mediaSource() {
        return new MediaSourceFather(this);
    }

    public MultipleMediaSourceFather multipleMediaSource() {
        return new MultipleMediaSourceFather(this);
    }

    public CommandProcessorFather commandProcessor() {
        return new CommandProcessorFather();
    }

    public MediaMuxerFather mediaMuxer() {
        return new MediaMuxerFather();
    }

    public VideoFormatFather videoFormat() {
        return new VideoFormatFather();
    }

    public SurfaceFather surface() {
        return new SurfaceFather();
    }

    public SurfaceContainerFather surfaceContainer() {
        return new SurfaceContainerFather();
    }

    public VideoEncoderFather videoEncoder() {
        return new VideoEncoderFather(this);
    }

    public AudioEncoderFather audioEncoder() {
        return new AudioEncoderFather(this);
    }

    public AudioFormatFather audioFormat() {
        return new AudioFormatFather();
    }

    public MediaFileInfoFather mediaFileInfo() {
        return new MediaFileInfoFather(this);
    }

    public VideoEffectorFather videoEffector() {
        return new VideoEffectorFather(this);
    }

    public AudioEffectorFather audioEffector() {
        return new AudioEffectorFather(this);
    }

    public CameraSourceFather cameraSource() {
        return new CameraSourceFather(this);
    }

    public MediaStreamerFather mediaStreamer() {
        return new MediaStreamerFather();
    }

    public ScreenCaptureFather screenCaptureSource() {
        return new ScreenCaptureFather(this);
    }

    public MediaComposerFather mediaComposer() {
        return new MediaComposerFather(this);
    }

    public MicrophoneSourceFather microphoneSource() {
        return new MicrophoneSourceFather(this);
    }

    public StreamingParametersFather streamingParameters() {
        return new StreamingParametersFather(this);
    }

    public RecognitionPipelineFather recognitionPipeline() {
        return new RecognitionPipelineFather(this);
    }

    public IRecognitionPlugin recognitionPlugin() {
        return mock(IRecognitionPlugin.class);
    }

    public CameraCaptureFather cameraCapture(IProgressListener progressListener) {
        return new CameraCaptureFather(this, progressListener);
    }

    public IVideoEffect videoEffect() {
        IVideoEffect videoEffect = mock(IVideoEffect.class);
        when(videoEffect.getSegment()).thenReturn(new FileSegment(0L, 0L));
        return videoEffect;
    }

    public ProgressListenerFather progressListener() {
        return new ProgressListenerFather(this);
    }

    public BrokenFather broken() {
        return new BrokenFather(this);
    }

    public SurfaceRenderFather surfaceRender() {
        return new SurfaceRenderFather(this);
    }

    public ResamplerFather resampler()  { return new ResamplerFather(this); }
}
