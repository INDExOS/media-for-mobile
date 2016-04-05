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

package org.m4m.domain;

import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;
import org.m4m.domain.mediaComposer.ProgressListenerFake;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CameraCaptureTest extends TestBase {
    protected ProgressListenerFake progressListener;
    private final Object onStart = new Object();
    private final Object onDone = new Object();
    private final Object onError = new Object();

    @Test
    public void setTargetAudioFormat_configuresMicrophoneSource() {
        org.m4m.AudioFormat audioFormat = create
            .audioFormat()
            .withSampleRate(100)
            .withChannelCount(2)
            .construct();

        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        IMicrophoneSource microphoneSource = create.microphoneSource().construct();
        factory.withMicrophoneSource(microphoneSource);
        factory.withAudioFormat(audioFormat);

        new org.m4m.CameraCapture(factory, progressListener).setTargetAudioFormat(audioFormat);

        verify(microphoneSource).configure(eq(100), eq(2));
    }

    @Test
    public void setTargetVideoFormat_configuresVideoEncoder() {
        org.m4m.VideoFormat videoFormat = create
            .videoFormat()
            .withFrameSize(720, 480)
            .withBitRate(1000)
            .withFrameRate(30)
            .withIFrameInterval(4)
            .construct();

        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create);
        factory.withVideoFormat(videoFormat);
        VideoEncoder videoEncoder = mock(VideoEncoder.class);
        factory.withVideoEncoder(videoEncoder);

        new org.m4m.CameraCapture(factory, progressListener).setTargetVideoFormat(videoFormat);

        verify(videoEncoder).setMediaFormat(eq(videoFormat));
        verify(videoEncoder).setBitRateInKBytes(1000);
        verify(videoEncoder).setFrameRate(30);
        verify(videoEncoder).setIFrameInterval(4);
    }

    @Test
    public void setTargetConnection_configuresStreamingParameters() {
        org.m4m.StreamingParameters parameters = create
            .streamingParameters()
            .withHost("123")
            .withPort(911)
            .withApplicationName("123")
            .withStreamName("123")
            .withSecure(true)
            .withUsername("123")
            .withPassword("123")
            .withIsToPublishAudio(true)
            .withIsToPublishVideo(true)
            .construct();

        IAndroidMediaObjectFactory factory = mock(IAndroidMediaObjectFactory.class);

        new org.m4m.CameraCapture(factory, progressListener).setTargetConnection(parameters);

        verify(factory).createSink(eq(parameters), any(org.m4m.IProgressListener.class), any(ProgressTracker.class));
    }

    @Test
    public void setTargetFile_configuresFileName() throws IOException {
        IAndroidMediaObjectFactory factory = mock(IAndroidMediaObjectFactory.class);

        org.m4m.CameraCapture cameraCapture = new org.m4m.CameraCapture(factory, progressListener);
        cameraCapture.setTargetFile("123");

        verify(factory).createSink(eq("123"), any(org.m4m.IProgressListener.class), any(ProgressTracker.class));
    }

    @Test
    public void buildPipeline_addsVideoEncoderToPipeline() throws IOException {
        VideoEncoder videoEncoder = create.videoEncoder().construct();
        Pipeline pipeline = mock(Pipeline.class);
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(progressListener)
            .with(pipeline)
            .with(videoEncoder)
            .construct();

        cameraCapture.buildPipeline();

        verify(pipeline).addVideoEncoder(eq(videoEncoder));
    }

    @Test
    public void canAddVideoEffect() throws InterruptedException, IOException {
        org.m4m.IProgressListener onStartListener = create.progressListener().withSyncOnMediaStart(onStart).construct();
        Frame frame = create.frame().construct();
        org.m4m.IVideoEffect videoEffect = create.videoEffect();
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onStartListener)
            .withSourceFrame(frame)
            .with(videoEffect)
            .construct();

        cameraCapture.start();
        waitUntil(onStart);

        assertThat(videoEffect).receivedDuring(frame, 1000);

        cameraCapture.stop();
    }

    @Test
    public void addAudioEncoder_configuresAudioFormat() throws InterruptedException {
        org.m4m.AudioFormat audioFormat = create.audioFormat()
            .withSampleRate(44000)
            .withChannelCount(4)
            .withBitRate(500)
            .construct();
        MediaCodecInfo.CodecProfileLevel codecProfileLevel = new MediaCodecInfo.CodecProfileLevel();
        codecProfileLevel.profile = codecProfileLevel.AACObjectLC;
        audioFormat.setAudioProfile(codecProfileLevel.profile);

        AudioEncoder audioEncoder = create.audioEncoder().construct();
        AndroidMediaObjectFactoryFake factory = new AndroidMediaObjectFactoryFake(create)
            .withAudioFormat(audioFormat)
            .withAudioEncoder(audioEncoder);

        new org.m4m.CameraCapture(factory, progressListener).setTargetAudioFormat(audioFormat);

        assertEquals(44000, audioEncoder.getSampleRate());
        assertEquals(4, audioEncoder.getChannelCount());
        assertEquals(22050, audioEncoder.getBitRate());
        assertEquals(2, ((org.m4m.AudioFormat) audioEncoder.getMediaFormatByType(MediaFormatType.AUDIO)).getAudioProfile());
    }

    @Test
    public void start_notifiesOnStart() throws IOException, InterruptedException {
        org.m4m.IProgressListener onStartListener = create.progressListener().withSyncOnMediaStart(onStart).construct();
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onStartListener).construct();

        cameraCapture.start();
        waitUntil(onStart);

        verify(onStartListener).onMediaStart();
    }

    @Test
    public void start_brokenPipeline_notifiesOnError() throws IOException, InterruptedException {
        org.m4m.IProgressListener onErrorListener = create.progressListener().withSyncOnError(onError).construct();
        IMediaCodec brokenMediaCodec = create.broken().mediaCodec().construct();
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onErrorListener)
            .withEncoderMediaCodec(brokenMediaCodec)
            .construct();

        cameraCapture.start();
        waitUntil(onError);

        verify(onErrorListener).onError(any(Exception.class));
    }

    @Test
    public void stop_notFails() throws InterruptedException, IOException {
        org.m4m.IProgressListener onDone = create.progressListener()
            .withSyncOnMediaDone(this.onDone)
            .withSyncOnMediaStart(this.onStart)
            .construct();
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onDone)
            .withSourceFrame(create.frame(1, 2, 3).construct())
            .withSurface(create.surface().construct())
            .withRender(a.render().construct())
            .construct();

        cameraCapture.start();
        waitUntil(this.onStart);

        cameraCapture.stop();
        waitUntil(this.onDone);

        verify(onDone).onMediaDone();
    }

    @Test
    public void start_withError_closesRender() throws IOException, InterruptedException {
        org.m4m.IProgressListener onErrorListener = create.progressListener()
            .withSyncOnMediaStop(onDone)
            .construct();
        Render render = mock(Render.class);
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onErrorListener)
            .withEncoderMediaCodec(a.broken().mediaCodec().construct())
            .withRender(render)
            .construct();

        cameraCapture.start();
        waitUntil(onDone);

        verify(render).close();
    }

    @Test
    public void stop_withError_neverClosesRender() throws IOException, InterruptedException {
        org.m4m.IProgressListener onDoneListener = create.progressListener().withSyncOnMediaDone(onStart).withSyncOnError(onError).construct();
        Frame frame = create.frame().construct();
        org.m4m.IVideoEffect videoEffect = create.videoEffect();
        Render renderSpy = spy(create.render().construct());
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onDoneListener)
            .withSourceFrame(frame)
            .with(videoEffect)
            .withRender(renderSpy)
            .withBrokenStop()
            .construct();

        cameraCapture.start();
        waitUntil(onStart);
        cameraCapture.stop();
        waitUntil(onError);

        verify(renderSpy, never()).close();
    }

    @Test
    public void quickStartStopNoCrash() throws IOException, InterruptedException {
        org.m4m.IProgressListener onDone = create.progressListener()
                .withSyncOnMediaDone(this.onDone)
                .withSyncOnMediaStart(this.onStart)
                .construct();
        CameraSource camSource = mock(CameraSource.class);

        IMediaCodec mediaCodec = create.mediaCodec().construct();

        org.m4m.CameraCapture cameraCapture = create.cameraCapture(onDone)
                .withEncoderMediaCodec(mediaCodec)
                .withSourceFather(create.cameraSource().with(camSource))
                .withSourceFrame(create.frame(1, 2, 3).construct())
                .withSurface(create.surface().construct())
                .with(create.videoEffect())
                .construct();


        //this can happen if initially command queue is empty, then sleep, that end, then processCommands
        camSource.stop();

        cameraCapture.start();
        waitUntil(this.onStart);

        cameraCapture.stop();
        waitUntil(this.onDone);

        verify(onDone).onMediaDone();
    }

    private void waitUntil(Object sync) throws InterruptedException {
        synchronized (sync) {
            sync.wait(1000);
        }
    }

    @Test
    public void canSetCameraCourse() throws IOException{
        Object camera = mock(Object.class);
        CameraSource camSource = mock(CameraSource.class);

        org.m4m.CameraCapture cameraCapture = create.cameraCapture(progressListener)
                .withSourceFather(create.cameraSource().with(camSource))
                .withSourceFrame(create.frame(1, 2, 3).construct())
                .withSurface(create.surface().construct())
                .with(create.videoEffect())
                .construct();

        cameraCapture.setCamera(camera);

        verify(camSource).setCamera(camera);
    }

    @Test
    public void canRemoveVideoEffect() throws IOException, InterruptedException{
        Frame frame = create.frame().construct();
        org.m4m.IVideoEffect videoEffect = create.videoEffect();
        VideoEffector videoEffector = mock(VideoEffector.class);
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(progressListener).with(videoEffector)
                .withSourceFrame(frame)
                .with(videoEffect)
                .construct();

        cameraCapture.removeVideoEffect(videoEffect);

        assertTrue(videoEffector.getVideoEffects().isEmpty());
    }

    @Test
    public void setTargetFile_getNullFileName_returnsNull() throws IOException {
        IAndroidMediaObjectFactory factory = mock(IAndroidMediaObjectFactory.class);
        String filename = null;

        Render expected = null;
        Render actual = factory.createSink(filename, progressListener, new ProgressTracker());

        assertEquals(expected, actual);
    }


    @Test
    public void createPreview_previewRenderNull_canCreatePreview()throws IOException{
        AndroidMediaObjectFactoryFake factory = mock(AndroidMediaObjectFactoryFake.class);

        Object mGLView = new Object();
        Object camera = new Object();

        new org.m4m.CameraCapture(factory, progressListener).createPreview(mGLView, camera);

        verify(factory).createPreviewRender(mGLView, camera);
        verify(factory).createVideoEffector();
    }

    @Test
    public void stop_previewRenderNotNull_videoEffectorExecuteGetVideoEffects ()throws IOException {
        org.m4m.IProgressListener iProgressListener = create.progressListener().construct();
        VideoEffector videoEffector = mock(VideoEffector.class);
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(iProgressListener).with(videoEffector)
                .construct();

        Object mGLView = new Object();
        Object camera = new Object();

        cameraCapture.createPreview(mGLView, camera);

        cameraCapture.start();
        cameraCapture.stop();

        verify(videoEffector, times(2)).getVideoEffects();
    }

    @Test
    public void stop_previewRenderNotNull_videoEffectorExecuteEnablePreview ()throws IOException {
        org.m4m.IProgressListener iProgressListener = create.progressListener().construct();
        VideoEffector videoEffector = mock(VideoEffector.class);
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(iProgressListener).with(videoEffector)
                .construct();

        Object mGLView = new Object();
        Object camera = new Object();

        cameraCapture.createPreview(mGLView, camera);

        cameraCapture.start();
        cameraCapture.stop();

        verify(videoEffector, times(2)).enablePreview(any(IPreview.class));
    }

    @Test
    public void createPreview_previewRenderAndVideoEffectorNull_factoryCreateObjects()throws IOException {
        IAndroidMediaObjectFactory factory = mock(IAndroidMediaObjectFactory.class);

        org.m4m.CameraCapture cameraCapture = new org.m4m.CameraCapture(factory, progressListener);

        Object mGLView = new Object();
        Object camera = new Object();

        IPreview actual = cameraCapture.createPreview(mGLView, camera);

        verify(factory, times(1)).createPreviewRender(mGLView, camera);
        verify(factory).createVideoEffector();

        IPreview expected = factory.createPreviewRender(mGLView, camera);

        assertEquals(actual, expected);
    }

    @Test
    public void createPreview_enablePreview_canEnablePreview()throws IOException {
        Frame frame = create.frame().construct();
        org.m4m.IVideoEffect videoEffect = create.videoEffect();
        VideoEffector videoEffector = mock(VideoEffector.class);
        org.m4m.CameraCapture cameraCapture = create.cameraCapture(progressListener).with(videoEffector)
                .withSourceFrame(frame)
                .with(videoEffect)
                .construct();

        cameraCapture.createPreview(new Object(), new Object());

        verify(videoEffector).enablePreview(any(IPreview.class));
    }


}
