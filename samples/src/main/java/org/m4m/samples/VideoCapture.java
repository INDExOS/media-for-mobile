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

package org.m4m.samples;

import android.content.Context;

import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;

import java.io.IOException;

public class VideoCapture {
    private static final String TAG = "VideoCapture";

    private static final int width = 1280;
    private static final int height = 720;
    private static final int frameRate = 30;
    private static final int iFrameInterval = 1;
    private static final int bitRate = 3000;
    private static final String codec = "video/avc";

    private static final Object syncObject = new Object();

    private org.m4m.VideoFormat videoFormat;
    private org.m4m.GLCapture capture;

    private boolean isStarted;
    private boolean isConfigured;
    private Context context;
    private org.m4m.IProgressListener progressListener;

    public VideoCapture(Context context, org.m4m.IProgressListener progressListener) {
        this.context = context;
        this.progressListener = progressListener;
        initVideoFormat();
    }

    public void start(String videoPath) throws IOException {
        if (isStarted()) {
            throw new IllegalStateException(TAG + " already started!");
        }

        capture = new org.m4m.GLCapture(new AndroidMediaObjectFactory(context), progressListener);

        capture.setTargetFile(videoPath);
        capture.setTargetVideoFormat(videoFormat);

        org.m4m.AudioFormat audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 1);
        capture.setTargetAudioFormat(audioFormat);

        capture.start();

        isStarted = true;
        isConfigured = false;
    }

    public void start(org.m4m.StreamingParameters params) throws IOException {
        if (isStarted()) {
            throw new IllegalStateException(TAG + " already started!");
        }

        capture = new org.m4m.GLCapture(new AndroidMediaObjectFactory(context), progressListener);

        capture.setTargetConnection(params);
        capture.setTargetVideoFormat(videoFormat);

        capture.start();

        isStarted = true;
        isConfigured = false;
    }

    public void stop() {
        if (!isStarted()) {
            throw new IllegalStateException(TAG + " not started or already stopped!");
        }

        capture.stop();
        capture = null;
        isConfigured = false;
    }

    private boolean configure() {
        if (isConfigured()) {
            return true;
        }

        try {
            capture.setSurfaceSize(width, height);
            isConfigured = true;
        } catch (Exception ex) {

        }

        return isConfigured;
    }

    public boolean beginCaptureFrame() {
        if (!isStarted()) {
            return false;
        }

        if (!isConfigured()) {
            if (!configure()) {
                return false;
            }
        }

        capture.beginCaptureFrame();

        return true;
    }

    public void endCaptureFrame() {
        if (!isStarted()) {
            return;
        }

        if (!isConfigured()) {
            return;
        }

        capture.endCaptureFrame();
    }

    public boolean isStarted() {
        if (capture == null) {
            return false;
        }

        return isStarted;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public int getFrameWidth() {
        return width;
    }

    public int getFrameHeight() {
        return height;
    }

    private void initVideoFormat() {
        videoFormat = new VideoFormatAndroid(codec, width, height);

        videoFormat.setVideoBitRateInKBytes(bitRate);
        videoFormat.setVideoFrameRate(frameRate);
        videoFormat.setVideoIFrameInterval(iFrameInterval);
    }
}
