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

import org.m4m.AudioFormat;
import org.m4m.IProgressListener;
import org.m4m.StreamingParameters;
import org.m4m.VideoFormat;
import org.m4m.domain.graphics.TextureRenderer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Base class for different capturing pipelines exposed by library
 */
public abstract class CapturePipeline {
    VideoEncoder videoEncoder;
    AudioEncoder audioEncoder;
    protected VideoEffector videoEffector;
    protected TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;
    protected AudioEffector audioEffector;

    protected final IAndroidMediaObjectFactory androidMediaObjectFactory;
    protected Pipeline pipeline;
    Render sink;
    AudioFormat audioFormat;
    protected ExecutorService pools;
    private final IProgressListener progressListener;

    private boolean started;
    private final Object untillDone = new Object();
    private int orientaionDegrees = 0;
    private VideoFormat mediaFormat;

    /**
     * Constructor
     *
     * @param androidMediaObjectFactory Android backend object factory
     * @param progressListener          Progress listener
     */
    public CapturePipeline(IAndroidMediaObjectFactory androidMediaObjectFactory, IProgressListener progressListener) {
        this.androidMediaObjectFactory = androidMediaObjectFactory;
        this.progressListener = progressListener;
    }


    /**
     * Sets target file
     *
     * @param fileName absolute path to target file
     * @throws IOException if resource could not be created
     */
    public void setTargetFile(String fileName) throws IOException {
        this.sink = androidMediaObjectFactory.createSink(fileName, progressListener, new ProgressTracker());
    }

    /**
     * Sets target connection to streaming server
     *
     * @param parameters of streaming server we want to connect
     */
    public void setTargetConnection(StreamingParameters parameters) {
        this.sink = androidMediaObjectFactory.createSink(parameters, progressListener, new ProgressTracker());
    }


    /**
     * Sets VideoFormat for target file
     *
     * @param mediaFormat target file VideoFormat
     * @see org.m4m.VideoFormat
     */
    public void setTargetVideoFormat(VideoFormat mediaFormat) {
        if (videoEncoder == null) {
            videoEncoder = androidMediaObjectFactory.createVideoEncoder();
        }
        this.mediaFormat = mediaFormat;
        setVideoEncoderParameters(mediaFormat);

    }

    private void setVideoEncoderParameters(VideoFormat mediaFormat) {
        videoEncoder.setMediaFormat(androidMediaObjectFactory.createVideoFormat(mediaFormat.getVideoCodec(),
                mediaFormat.getVideoFrameSize().width(),
                mediaFormat.getVideoFrameSize().height()));
        videoEncoder.setBitRateInKBytes(mediaFormat.getVideoBitRateInKBytes());
        videoEncoder.setFrameRate(mediaFormat.getVideoFrameRate());
        videoEncoder.setIFrameInterval(mediaFormat.getVideoIFrameInterval());
    }

    /**
     * Sets AudioFormat for target file
     *
     * @param mediaFormat target file AudioFormat
     * @see AudioFormat
     */
    public void setTargetAudioFormat(AudioFormat mediaFormat) {
        if (audioEncoder == null) {
            audioEncoder = androidMediaObjectFactory.createAudioEncoder(null);
        }
        audioFormat = mediaFormat;

        int channelCount = mediaFormat.getAudioChannelCount();
        int sampleRate = mediaFormat.getAudioSampleRateInHz();

        AudioFormat audioFormat = (AudioFormat) androidMediaObjectFactory.createAudioFormat(mediaFormat.getAudioCodec(), channelCount, sampleRate);
        audioFormat.setAudioBitrateInBytes(22050);
        audioFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        audioEncoder.setMediaFormat(audioFormat);
    }

    /**
     * Sets orientation of target media file
     *
     * @param degrees of output media file orientation
     */
    public void setOrientation(int degrees) {
        orientaionDegrees = degrees;
    }

    /**
     * Start data processing
     */
    public void start() {
        CommandProcessor commandProcessor = new CommandProcessor(progressListener);
        pipeline = new Pipeline(commandProcessor);
        pools = Executors.newSingleThreadExecutor();

        buildPipeline();
        executeProcessor(commandProcessor);

        started = true;
    }

    protected void buildPipeline() {
        setMediaSource();

        if (videoEffector != null) {
            videoEffector.setFillMode(fillMode);
            pipeline.addVideoEffect(videoEffector);
        }
        if (audioEncoder != null) {

            if (audioEffector != null) {
                audioEffector.setMediaFormat(audioFormat);
                pipeline.addAudioEffect(audioEffector);
            }

            pipeline.addAudioEncoder(audioEncoder);
        }
        if (videoEncoder != null) {
            if (orientaionDegrees == 90 || orientaionDegrees == 270) {
                recreateVideoEncoder();
            }
            pipeline.addVideoEncoder(videoEncoder);
        }
        pipeline.setSink(sink);
    }

    private void recreateVideoEncoder() {
        mediaFormat.setVideoFrameSize(mediaFormat.getVideoFrameSize().height(),
                                       mediaFormat.getVideoFrameSize().width());
        setVideoEncoderParameters(mediaFormat);
    }

    protected void executeProcessor(final CommandProcessor commandProcessor) {
        pools.execute(new Runnable() {
            @Override
            public void run() {
                Exception reportedError = null;
                try {
                    pipeline.resolve();
                    notifyOnStart();
                    commandProcessor.process();
                    notifyOnDone();
                } catch (Exception e) {
                    reportedError = e;
                } finally {
                    try {
                        pipeline.release();
                        synchronized (untillDone){
                            untillDone.notify();
                        }
                        if (reportedError != null ){
                            notifyOnError(reportedError);
                        }
                    } catch (Exception e) {
                        notifyOnError(e);
                    }
                }
            }
        });
    }

    /**
     * Stop data processing
     */
    public void stop() {
        if (!started) {
            return;
        }
        try {
            //!!!!!DANGER!!!!
            //ask someone for help before removing that stop call, i have to revert it 3 times already
            pipeline.stop();
            //!!!!!DANGER!!!!

            notifyOnStop();

            synchronized (untillDone){
                untillDone.wait(10 * 1000);
            }

            //stopRequested = true;
            pools.shutdownNow();
            if (!pools.awaitTermination(10, TimeUnit.SECONDS)) {
                notifyOnError(new Exception("Cannot stop capture thread"));
            }

        } catch (Exception e) {
            notifyOnError(e);
        }
        //destroy everithing
        audioEncoder = null;
        videoEncoder = null;

        started = false;
    }

    public void setFillMode(TextureRenderer.FillMode fillMode) {
        this.fillMode = fillMode;
    }

    public TextureRenderer.FillMode getFillMode() {
        return fillMode;
    }

    private void notifyOnDone() {
        progressListener.onMediaDone();
    }

    protected void notifyOnError(Exception e) {
        progressListener.onError(e);
    }

    protected void notifyOnStart() {
        progressListener.onMediaStart();
    }

    protected void notifyOnStop() {
        progressListener.onMediaStop();
    }

    protected abstract void setMediaSource();
}
