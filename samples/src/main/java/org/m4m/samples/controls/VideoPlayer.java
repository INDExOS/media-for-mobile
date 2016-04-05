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

package org.m4m.samples.controls;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoPlayer {
    private final static int BufferTimeoutUs = 1000;

    private final static int DURATION_CHANGED = 1;
    private final static int POSITION_CHANGED = 2;
    private final static int RESUMED = 3;
    private final static int PAUSED = 4;

    public interface VideoPlayerEvents {
        void onVideoPlayerDurationChanged(long duration);

        void onVideoPlayerPositionChanged(boolean outside, long position);

        void onVideoPlayerPlaybackResumed();

        void onVideoPlayerPlaybackPaused();
    }

    VideoPlayerEvents events;
    Surface surface;

    MediaExtractor extractor;
    MediaCodec codec;

    int trackNumber;

    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;
    MediaCodec.BufferInfo bufferInfo;

    Object pauseLock;
    boolean pause;
    boolean stop;

    Object seekLock;
    long seekRequestedToMs;
    long currentPresentationTimeMs;

    Thread videoThread;

    public VideoPlayer(Surface surface) {
        this.surface = surface;

        pauseLock = new Object();
        pause = false;

        seekLock = new Object();
        seekRequestedToMs = -1;

        stop = false;
    }

    public void setEventListener(VideoPlayerEvents listener) {
        events = listener;
    }

    public void open(String path) throws IOException {
        close();

        pause = false;
        stop = false;
        seekRequestedToMs = -1;
        currentPresentationTimeMs = 0;

        extractor = new MediaExtractor();

        extractor.setDataSource(path);

        if (initWithTrackOfInterest("video/") == false) {
            throw new IOException("Can't open video file. Unsupported video format.");
        }

        if (codec == null) {
            throw new IOException("Can't open video file. Unsupported video format.");
        }

        pause();

        videoThread = new VideoThread();
        videoThread.start();
    }

    public void close() {
        stop = true;

        if (videoThread != null) {
            if (videoThread.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                videoThread.interrupt();
            }

            videoThread = null;
        }
    }

    public void resume() {
        if (pause == false || videoThread == null) {
            return;
        }

        synchronized (pauseLock) {
            pause = false;
            pauseLock.notifyAll();
        }

        Message msg = new Message();
        msg.what = RESUMED;
        uiHandler.sendMessage(msg);
    }

    public void pause() {
        if (pause) {
            return;
        }

        synchronized (pauseLock) {
            pause = true;
        }

        Message msg = new Message();
        msg.what = PAUSED;
        uiHandler.sendMessage(msg);
    }

    public void seekTo(long positionMs) {
        synchronized (seekLock) {
            seekRequestedToMs = positionMs;
        }
    }

    public boolean playing() {
        return (videoThread != null && pause == false);
    }

    private boolean initWithTrackOfInterest(String startsWith) {
        int numTracks = extractor.getTrackCount();

        for (int i = 0; i < numTracks; i++) {
            MediaFormat mediaFormat = extractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);

            if (mime.startsWith(startsWith)) {
                trackNumber = i;

                extractor.selectTrack(trackNumber);

                try {
                    codec = MediaCodec.createDecoderByType(mime);
                    codec.configure(mediaFormat, surface, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long duration = (int) mediaFormat.getLong(MediaFormat.KEY_DURATION);

                Message msg = uiHandler.obtainMessage(DURATION_CHANGED, duration / 1000);
                uiHandler.sendMessage(msg);

                return true;
            }
        }

        return false;
    }

    protected boolean checkPause() {
        boolean paused = false;

        synchronized (pauseLock) {
            if (pause) {
                paused = true;

                while (pause && stop == false) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        return paused;
    }

    final Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DURATION_CHANGED: {
                    final String longString = msg.obj.toString();
                    final Long theLongObj = new Long(longString);

                    long duration = theLongObj.longValue();

                    events.onVideoPlayerDurationChanged(duration);
                }
                break;

                case POSITION_CHANGED: {
                    final String longString = msg.obj.toString();
                    final Long theLongObj = new Long(longString);

                    long position = theLongObj.longValue();

                    events.onVideoPlayerPositionChanged(false, position);
                }
                break;

                case RESUMED: {
                    events.onVideoPlayerPlaybackResumed();
                }
                break;

                case PAUSED: {
                    events.onVideoPlayerPlaybackPaused();
                }
                break;
            }
        }
    };

    private class VideoThread extends Thread {
        public VideoThread() {
        }

        @Override
        public void run() {
            codec.start();

            inputBuffers = codec.getInputBuffers();
            outputBuffers = codec.getOutputBuffers();
            bufferInfo = new MediaCodec.BufferInfo();

            boolean isEOS = false;

            long startTimeMs = System.currentTimeMillis();

            currentPresentationTimeMs = 0;

            long lastNotificationTime = 0;

            while (stop == false) {
                if (checkPause()) {
                    startTimeMs = System.currentTimeMillis() - currentPresentationTimeMs;
                }

                if (stop) {
                    break;
                }

                synchronized (seekLock) {
                    if (seekRequestedToMs != -1) {
                        extractor.seekTo(seekRequestedToMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        codec.flush();

                        currentPresentationTimeMs = seekRequestedToMs;

                        startTimeMs = System.currentTimeMillis() - currentPresentationTimeMs;

                        seekRequestedToMs = -1;
                    }
                }

                if (!isEOS) {
                    int inIndex = codec.dequeueInputBuffer(BufferTimeoutUs);

                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);

                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = codec.dequeueOutputBuffer(bufferInfo, BufferTimeoutUs);

                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED: {
                        outputBuffers = codec.getOutputBuffers();
                    }
                    break;

                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED: {
                    }
                    break;

                    case MediaCodec.INFO_TRY_AGAIN_LATER: {

                    }
                    break;

                    default: {
                        currentPresentationTimeMs = bufferInfo.presentationTimeUs / 1000;

                        if ((SystemClock.currentThreadTimeMillis() - lastNotificationTime) > 10) {
                            Message msg = uiHandler.obtainMessage(POSITION_CHANGED, currentPresentationTimeMs);
                            uiHandler.sendMessage(msg);

                            lastNotificationTime = SystemClock.currentThreadTimeMillis();
                        }

                        while (currentPresentationTimeMs > (System.currentTimeMillis() - startTimeMs)) {
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                break;
                            }

                            if (pause) {
                                break;
                            }

                            synchronized (seekLock) {
                                if (seekRequestedToMs != -1) {
                                    break;
                                }
                            }
                        }

                        codec.releaseOutputBuffer(outIndex, true);
                    }

                    break;
                }

                if (isEOS) {
                    pause();

                    seekTo(0);
                }
            }

            codec.stop();
            codec.release();

            extractor.release();
        }
    }
}
