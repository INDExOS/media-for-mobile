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

package org.m4m.domain.mediaComposer;

import org.m4m.domain.dsl.Father;
import org.m4m.domain.dsl.MediaSourceFather;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.pipeline.IOnStopListener;

import org.m4m.domain.AudioDecoder;
import org.m4m.domain.AudioEffector;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.CameraSource;
import org.m4m.domain.FileSegment;
import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.IAudioContentRecognition;
import org.m4m.domain.ICameraSource;
import org.m4m.domain.ICaptureSource;
import org.m4m.domain.IEffectorSurface;
import org.m4m.domain.IEglContext;
import org.m4m.domain.IFrameBuffer;
import org.m4m.domain.IMediaExtractor;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.IMicrophoneSource;
import org.m4m.domain.IPreview;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MuxRender;
import org.m4m.domain.ProgressTracker;
import org.m4m.domain.Render;
import org.m4m.domain.Resampler;
import org.m4m.domain.VideoDecoder;
import org.m4m.domain.VideoEffector;
import org.m4m.domain.VideoEncoder;
import org.m4m.domain.VideoTimeScaler;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.mockito.Mockito.mock;

public class AndroidMediaObjectFactoryFake implements IAndroidMediaObjectFactory {
    private final Father create;
    private Dictionary<String, IMediaExtractor> mediaExtractors = new Hashtable<String, IMediaExtractor>();
    private IMediaMuxer muxer;
    private Render sink;
    private VideoDecoder videoDecoder;
    private AudioDecoder audioDecoder;
    private VideoEncoder videoEncoder;
    private AudioEncoder audioEncoder;
    private Resampler resampler;
    private MediaSource mediaSource;
    private VideoEffector videoEffector;
    private ICameraSource cameraSource;
    private MediaFormat videoFormat;
    private IMicrophoneSource microphoneSource;
    private org.m4m.AudioFormat audioFormat;
    private IEglContext currentEglContext;
    private IFrameBuffer FB;
    private IPreview preview;
    private final IOnStopListener onStopListener;

    public AndroidMediaObjectFactoryFake(Father create) {
        this.create = create;
        onStopListener = new IOnStopListener() {
            @Override
            public void onStop() {
            }
        };
    }

    public AndroidMediaObjectFactoryFake withMediaSource(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
        return this;
    }

    public AndroidMediaObjectFactoryFake withVideoDecoder(VideoDecoder decoder) {
        this.videoDecoder = decoder;
        return this;
    }

    public AndroidMediaObjectFactoryFake withAudioDecoder(AudioDecoder decoder) {
        this.audioDecoder = decoder;
        return this;
    }

    public AndroidMediaObjectFactoryFake withVideoEncoder(VideoEncoder encoder) {
        this.videoEncoder = encoder;
        return this;
    }

    public void withVideoEffector(VideoEffector videoEffector) {
        this.videoEffector = videoEffector;
    }

    public void withCameraSource(CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Override
    public MediaSource createMediaSource(String fileName) throws IOException {
        if (null != mediaSource) {
            return mediaSource;
        }
        MediaSourceFather mediaSourceFather = create.mediaSource();
        if (mediaExtractors.get(fileName) != null) mediaSourceFather.with(mediaExtractors.get(fileName));
        return mediaSourceFather.withFilePath(fileName).construct();
    }

    @Override
    public MediaSource createMediaSource(FileDescriptor fileDescriptor) throws IOException {
        if (null != mediaSource) {
            return mediaSource;
        }
        MediaSourceFather mediaSourceFather = create.mediaSource();
        if (mediaExtractors.get(fileDescriptor) != null) mediaSourceFather.with(mediaExtractors.get(fileDescriptor));
        return mediaSourceFather.withFileDescriptor(fileDescriptor).construct();
    }

    @Override
    public MediaSource createMediaSource(org.m4m.Uri uri) throws IOException {
        if (null != mediaSource) {
            return mediaSource;
        }
        MediaSourceFather mediaSourceFather = create.mediaSource();
        if (mediaExtractors.get(uri) != null) mediaSourceFather.with(mediaExtractors.get(uri));
        return mediaSourceFather.withUri(uri).construct();
    }

    @Override
    public VideoDecoder createVideoDecoder(MediaFormat format) {
        if (videoDecoder == null) videoDecoder = create.videoDecoder().construct();
        return videoDecoder;
    }

    @Override
    public VideoEncoder createVideoEncoder() {
        if (videoEncoder == null) videoEncoder = create.videoEncoder().construct();
        return videoEncoder;
    }

    @Override
    public AudioDecoder createAudioDecoder() {
        if (audioDecoder == null) audioDecoder = create.audioDecoder().construct();
        return audioDecoder;
    }

    @Override
    public AudioEncoder createAudioEncoder(String mimeType) {
        if (audioEncoder == null) audioEncoder = create.audioEncoder().construct();
        return audioEncoder;
    }

    @Override
    public Resampler createAudioResampler(org.m4m.AudioFormat audioFormat) {
        if (resampler == null) resampler = create.resampler().construct();
        return resampler;
    }

    @Override
    public Render createSink(String fileName, int orientationHint, org.m4m.IProgressListener progressListener, ProgressTracker progressTracker) throws IOException {
        if (sink == null) {
            if (muxer == null)
                muxer = create.mediaMuxer().construct();
            return new MuxRender(muxer, progressListener, progressTracker);
        }
        return sink;
    }

    @Override
    public Render createSink(org.m4m.StreamingParameters StreamingParams, org.m4m.IProgressListener progressListener, ProgressTracker progressTracker) {
        if (muxer == null) {
            muxer = create.mediaStreamer().construct();
        }
        return new MuxRender(muxer, progressListener, progressTracker);
    }

    @Override
    public ICaptureSource createCaptureSource() {
        return null;
    }

    @Override
    public MediaFormat createVideoFormat(String mimeType, int width, int height) {
        if (videoFormat == null) {
            videoFormat = create.videoFormat().construct();
        }
        return videoFormat;
    }

    public void withVideoFormat(org.m4m.VideoFormat videoFormat) {
        this.videoFormat = videoFormat;
    }

    @Override
    public MediaFormat createAudioFormat(String mimeType, int channelCount, int sampleRate) {
        if (audioFormat == null) {
            audioFormat = create.audioFormat().construct();
        }
        return audioFormat;
    }

    public AndroidMediaObjectFactoryFake withAudioFormat(org.m4m.AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        return this;
    }

    @Override
    public VideoEffector createVideoEffector() {
        return videoEffector != null
                ? videoEffector
                : new VideoEffector(create.mediaCodec().construct(), this);
    }

    @Override
    public VideoTimeScaler createVideoTimeScaler(int timeScale, FileSegment segment) {
        return new VideoTimeScaler(create.mediaCodec().construct(), this, timeScale, segment);
    }

    @Override
    public IEffectorSurface createEffectorSurface() {
        return mock(IEffectorSurface.class);
    }

    @Override
    public IPreview createPreviewRender(Object glView, Object camera) {
        return mock(IPreview.class);
    }

    @Override
    public AudioEffector createAudioEffects() {
        return null;
    }

    @Override
    public ICameraSource createCameraSource() {
        if (cameraSource == null) {
            cameraSource = mock(ICameraSource.class);
        }
        return cameraSource;
    }

    @Override
    public IMicrophoneSource createMicrophoneSource() {
        if (microphoneSource == null) {
            microphoneSource = mock(IMicrophoneSource.class);
        }
        return microphoneSource;
    }

    public void withMicrophoneSource(IMicrophoneSource microphoneSource) {
        this.microphoneSource = microphoneSource;
    }

    @Override
    public IAudioContentRecognition createAudioContentRecognition() {
        return mock(IAudioContentRecognition.class);
    }

    @Override
    public IEglContext getCurrentEglContext() {
        return currentEglContext;
    }

    @Override
    public IEglUtil getEglUtil() {
        return mock(IEglUtil.class);
    }

    @Override
    public IFrameBuffer createFrameBuffer() {
        if (FB == null) {
            FB = mock(IFrameBuffer.class);
        }
        return FB;
    }

    public AndroidMediaObjectFactoryFake withAudioEncoder(AudioEncoder audioEncoder) {
        this.audioEncoder = audioEncoder;
        return this;
    }

    public void withCurrentEglContext(IEglContext eglContext) {
        this.currentEglContext = eglContext;
    }

    public void withFrameBuffer(IFrameBuffer fakeFB) {
        this.FB = fakeFB;
    }

    public AndroidMediaObjectFactoryFake withSink(Render render) {
        this.sink = render;
        return this;
    }
}
