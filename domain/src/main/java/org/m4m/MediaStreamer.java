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

import org.m4m.domain.CommandProcessor;
import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.MultipleMediaSource;
import org.m4m.domain.PassThroughPlugin;
import org.m4m.domain.Pipeline;
import org.m4m.domain.ProgressTracker;
import org.m4m.domain.Render;

import java.io.IOException;
import java.io.Serializable;

/**
 * This class enables streaming a local media file to a server.
 */
public class MediaStreamer implements Serializable {

    private IAndroidMediaObjectFactory factory = null;
    private MultipleMediaSource multipleMediaSource = null;
    private Render sink;
    private PassThroughPlugin videoPipe;
    private PassThroughPlugin audioPipe;

    private ProgressTracker progressTracker = new ProgressTracker();

    private Pipeline pipeline;
    private CommandProcessor commandProcessor;
    private IProgressListener progressListener;

    /**
     * Instantiates an object with Android base-layer.
     *
     * @param factory          IAndroidMediaObjectFactory class object.
     * @param progressListener Progress listener.
     * @see IAndroidMediaObjectFactory
     * @see org.m4m.IProgressListener
     */
    public MediaStreamer(IAndroidMediaObjectFactory factory, IProgressListener progressListener) {
        this.factory = factory;
        this.progressListener = progressListener;
        multipleMediaSource = new MultipleMediaSource();
    }

    /**
     * Adds a file to a collection of input files. Call for every input file.
     *
     * @param fileName Input file name. String class or Uri class object.
     * @throws IOException when the file name is invalid or the file can not be opened.
     */
    public void addSourceFile(String fileName) throws IOException {
        MediaSource mediaSource = factory.createMediaSource(fileName);
        MediaFile mediaFile = new MediaFile(mediaSource);
        multipleMediaSource.add(mediaFile);
    }

    public void addSourceFile(Uri uri) throws IOException {
        MediaSource mediaSource = factory.createMediaSource(uri);
        MediaFile mediaFile = new MediaFile(mediaSource);
        multipleMediaSource.add(mediaFile);
    }

    /**
     * Sets target connection to a streaming server.
     *
     * @param parameters Streaming server connection parameters.
     * @see StreamingParameters
     */
    public void setTargetConnection(StreamingParameters parameters) {
        this.sink = factory.createSink(parameters, progressListener, progressTracker);
    }

    /**
     * Starts processing
     */
    public void start() {
        commandProcessor = new CommandProcessor(progressListener);
        pipeline = new Pipeline(commandProcessor);
        videoPipe = new PassThroughPlugin(1000 * 1024, MediaFormatType.VIDEO);
        audioPipe = new PassThroughPlugin(10 * 1024, MediaFormatType.AUDIO);

        if (videoPipe != null) pipeline.addVideoDecoder(videoPipe);
        if (audioPipe != null) pipeline.addAudioDecoder(audioPipe);

        pipeline.setMediaSource(multipleMediaSource);
        pipeline.setSink(sink);

        startCommandsProcessingAsync();
    }

    private void startCommandsProcessingAsync() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    pipeline.resolve();

                    progressListener.onMediaStart();
                    progressTracker.setFinish(multipleMediaSource.getSegmentsDurationInMicroSec());
                    progressListener.onMediaProgress(0);

                    commandProcessor.process();

                    progressListener.onMediaDone();
                } catch (Exception e) {
                    progressListener.onError(e);
                }

                try {
                    pipeline.release();
                } catch (Exception e) {
                    progressListener.onError(e);
                }
            }
        });

        thread.start();
    }

    /**
     * Pauses processing
     */
    public void pause() {
        commandProcessor.pause();
    }

    /**
     * Resumes processing after pause
     */
    public void resume() {
        commandProcessor.resume();
    }

    /**
     * Stops processing
     */
    public void stop() {
        if (commandProcessor != null) {
            commandProcessor.stop();
        }
        if (pipeline != null) {
            pipeline.stop();
        }
        progressListener.onMediaStop();
    }
}
