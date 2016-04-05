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

package org.m4m;

import org.m4m.domain.AudioEffector;
import org.m4m.domain.AudioEncoder;
import org.m4m.domain.CommandProcessor;
import org.m4m.domain.FileSegment;
import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.Pipeline;
import org.m4m.domain.Plugin;
import org.m4m.domain.ProgressTracker;
import org.m4m.domain.Render;
import org.m4m.domain.Resampler;
import org.m4m.domain.VideoEffector;
import org.m4m.domain.VideoEncoder;
import org.m4m.domain.VideoTimeScaler;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * This class implements functionality for simple video editing and transcoding, e.g. joining files, cutting segments from files, applying effects.
 */
public class MediaComposer implements Serializable {
    private IAndroidMediaObjectFactory factory;
    private MultipleMediaSource multipleMediaSource;
    private Plugin videoDecoder;
    private VideoEncoder videoEncoder;
    private Plugin audioDecoder;
    private AudioEncoder audioEncoder;
    private Render sink;
    private VideoEffector videoEffector;
    private VideoTimeScaler videoTimeScaler;
    private AudioEffector audioEffector;
    private Pipeline pipeline;
    private CommandProcessor commandProcessor;
    private IProgressListener progressListener;
    private ProgressTracker progressTracker = new ProgressTracker();
    private AudioFormat audioFormat;
    private VideoFormat videoFormat;
    private Resampler resampler;

    private int timeScale = 1;
    private FileSegment segment = new FileSegment(0l, 0l); // Whole stream by default

    /**
     * Instantiates an object with Android base-layer.
     *
     * @param factory          IAndroidMediaObjectFactory class object.
     * @param progressListener Progress listener.
     * @see IAndroidMediaObjectFactory
     * @see org.m4m.IProgressListener
     */
    public MediaComposer(IAndroidMediaObjectFactory factory, IProgressListener progressListener) {
        this.progressListener = progressListener;
        this.factory = factory;
        multipleMediaSource = new MultipleMediaSource();
    }

    /**
     * Adds a file to a collection of input files. Call for every input file.
     *
     * @param fileName Input file name. String class, FileDescriptor class, or Uri class object.
     * @throws IOException      when the file name is invalid or the file can not be opened.
     * @throws RuntimeException when the file validation fails, i.e. audio sample rate not supported.
     */
    public void addSourceFile(String fileName) throws IOException, RuntimeException {
        MediaSource mediaSource = factory.createMediaSource(fileName);
        MediaFile mediaFile = new MediaFile(mediaSource);
        multipleMediaSource.add(mediaFile);
    }

    public void addSourceFile(FileDescriptor fileDescriptor) throws IOException, RuntimeException {
        MediaSource mediaSource = factory.createMediaSource(fileDescriptor);
        MediaFile mediaFile = new MediaFile(mediaSource);
        multipleMediaSource.add(mediaFile);
    }

    public void addSourceFile(Uri uri) throws IOException, RuntimeException {
        MediaSource mediaSource = factory.createMediaSource(uri);
        MediaFile mediaFile = new MediaFile(mediaSource);
        multipleMediaSource.add(mediaFile);
    }

    /**
     * Removes a single file from the collection of input files.
     *
     * @param mediaFile Media file to be removed from the collection.
     */
    public void removeSourceFile(MediaFile mediaFile) {
        multipleMediaSource.remove(mediaFile);
    }

    /**
     * Inserts a single file to a specified position in the collection of input files.
     *
     * @param index    Position in the collection.
     * @param fileName String class object to be inserted.
     * @throws IOException when the file name is invalid or the file can not be opened.
     */
    public void insertSourceFile(int index, String fileName) throws IOException {
        MediaSource mediaSource = factory.createMediaSource(fileName);
        MediaFile mediaFile = new MediaFile(mediaSource);
        multipleMediaSource.insertAt(index, mediaFile);
    }

    /**
     * Get the collection of input files.
     *
     * @return Collection of MediaFile class objects.
     */
    public List<MediaFile> getSourceFiles() {
        return multipleMediaSource.files();
    }

    /**
     * Sets the target file.
     *
     * @param fileName Absolute path to the target file. String class object.
     * @throws IOException when the file name is invalid or the file can not be opened.*
     */
    public void setTargetFile(String fileName) throws IOException {
        this.sink = factory.createSink(fileName, progressListener, progressTracker);
    }

    /**
     * Returns the total duration of the collection of input files.
     *
     * @return Duration value in microseconds.
     */
    public long getDurationInMicroSec() {
        return multipleMediaSource.getSegmentsDurationInMicroSec();
    }

    /**
     * Sets VideoFormat for the target file.
     *
     * @param mediaFormat Target file VideoFormat.
     * @see VideoFormat
     */
    public void setTargetVideoFormat(VideoFormat mediaFormat) {
        videoFormat = mediaFormat;
    }

    /**
     * Returns the target file VideoFormat.
     *
     * @return Target file VideoFormat.
     */
    public VideoFormat getTargetVideoFormat() {
        return videoFormat;
    }

    /**
     * Sets AudioFormat for the target file.
     *
     * @param mediaFormat Target file AudioFormat.
     * @see org.m4m.AudioFormat
     */
    public void setTargetAudioFormat(AudioFormat mediaFormat) {
        this.audioFormat = mediaFormat;
    }

    /**
     * Returns the target file AudioFormat.
     *
     * @return Target file AudioFormat.
     */
    public AudioFormat getTargetAudioFormat() {
        return audioFormat;
    }

    /**
     * Adds a user's video effect to a collection of video effects.
     *
     * @param effect Video effect to be added.
     * @see org.m4m.IVideoEffect
     */
    public void addVideoEffect(IVideoEffect effect) {
        if (videoEffector == null) {
            videoEffector = factory.createVideoEffector();
        }
        videoEffector.getVideoEffects().add(effect);
    }

    /**
     * Removes a video effect from the collection of video effects.
     *
     * @param effect Video effect to be removed.
     * @see org.m4m.IVideoEffect
     */
    public void removeVideoEffect(IVideoEffect effect) {
        videoEffector.getVideoEffects().remove(effect);
    }

    /**
     * Returns the collection of video effects.
     *
     * @return Read-only collection of video effects used.
     * @see org.m4m.IVideoEffect
     */
    public Collection<IVideoEffect> getVideoEffects() {
        return (Collection<IVideoEffect>) videoEffector.getVideoEffects().clone();
    }

    /**
     * Adds a user's audio effect to a collection of audio effects.
     *
     * @param effect Audio effect to be added.
     * @see org.m4m.IAudioEffect
     */
    public void addAudioEffect(IAudioEffect effect) {
        if (audioEffector == null) {
            audioEffector = factory.createAudioEffects();
        }
        audioEffector.getAudioEffects().add(effect);
    }

    /**
     * Removes an audio effect from the collection of audio effects.
     *
     * @param effect Audio effect to be removed.
     * @see org.m4m.IAudioEffect
     */
    public void removeAudioEffect(IAudioEffect effect) {
        audioEffector.getAudioEffects().remove(effect);
    }

    /**
     * Returns the collection of audio effects.
     *
     * @return Read-only collection of audio effects used by MediaComposer.
     * @see org.m4m.IAudioEffect
     */
    public Collection<IAudioEffect> getAudioEffects() {
        return (Collection<IAudioEffect>) audioEffector.getAudioEffects().clone();
    }

    /**
     * Starts processing.
     */
    public void start() {

        multipleMediaSource.verify();

        commandProcessor = new CommandProcessor(progressListener);
        pipeline = new Pipeline(commandProcessor);
        pipeline.setMediaSource(multipleMediaSource);

        // Note: if the 1st (current) stream doesn't have video, there will be audio pipeline only.
        if (videoFormat != null && multipleMediaSource.hasTrack(MediaFormatType.VIDEO)) {
            videoDecoder = factory.createVideoDecoder(videoFormat);
            videoEncoder = factory.createVideoEncoder();
            videoEncoder.setMediaFormat(videoFormat);
        }
        if (videoDecoder != null) pipeline.addVideoDecoder(videoDecoder);
        if (videoEncoder != null) pipeline.addVideoEncoder(videoEncoder);

        if (videoEffector != null) {
            videoEffector.setTimeScale(timeScale);
            videoEffector.setTimeScalerSegment(segment);
            pipeline.addVideoEffect(videoEffector);
        }

        if (videoTimeScaler != null && videoEffector == null) {
            pipeline.addVideoTimeScaler(videoTimeScaler);
        }

        // Note: if the 1st (current) stream doesn't have audio, there will be video pipeline only.
        if (audioFormat != null && multipleMediaSource.hasTrack(MediaFormatType.AUDIO)) {
            audioDecoder = factory.createAudioDecoder();
            audioEncoder = factory.createAudioEncoder(audioFormat.getAudioCodec());
            audioEncoder.setMediaFormat(audioFormat);

            // This m4m release doesn't support audio resampler
            //createResampler(audioFormat);
            //audioEncoder.addResampler(resampler);
        }
        if (audioDecoder != null) pipeline.addAudioDecoder(audioDecoder);
        if (audioEncoder != null) pipeline.addAudioEncoder(audioEncoder);
        if (audioEffector != null) {
            audioEffector.setMediaFormat(audioFormat);
            pipeline.addAudioEffect(audioEffector);
        }

        pipeline.setSink(sink);

        startCommandsProcessingAsync();
    }

    public void setVideoTimeScale(int timeScale, FileSegment segment) {
        this.timeScale = timeScale;
        this.segment = segment;
        videoTimeScaler = factory.createVideoTimeScaler(timeScale, segment);
    }

    /**
     * Pauses processing.
     */
    public void pause() {
        commandProcessor.pause();
    }

    /**
     * Resumes processing after a pause.
     */
    public void resume() {
        commandProcessor.resume();
    }

    /**
     * Stops processing.
     */
    public void stop() {
        if (pipeline != null) {
            pipeline.stop();
        }
        notifyOnMediaStop();
    }

    private void createResampler(AudioFormat audioFormat) {
        this.resampler = factory.createAudioResampler(audioFormat);
    }

    private void notifyOnMediaStart() {
        progressListener.onMediaStart();
    }

    private void notifyOnMediaDone() {
        progressListener.onMediaDone();
    }

    private void notifyOnMediaStop() {
        progressListener.onMediaStop();
    }

    private void notifyOnMediaProgress(float progress) {
        progressListener.onMediaProgress(progress);
    }

    private void notifyOnError(Exception exception) {
        progressListener.onError(exception);
    }

    private void startCommandsProcessingAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pipeline.resolve();
                    notifyOnMediaStart();
                    notifyOnMediaProgress(0);
                    progressTracker.setFinish(multipleMediaSource.getSegmentsDurationInMicroSec());
                    commandProcessor.process();
                } catch (Exception e) {
                    try {
                        pipeline.release();
                        notifyOnError(e);
                    } catch (IOException e1) {
                        notifyOnError(e);
                        notifyOnError(e1);
                    }
                    return;
                }

                try {
                    pipeline.release();
                } catch (IOException e) {
                    notifyOnError(e);
                    return;
                }

                notifyOnMediaProgress(1);
                notifyOnMediaDone();
            }
        }).start();
    }
}
