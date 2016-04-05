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

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioRender extends Render {
    private IAudioTrack audioPlayBack = null;
    private MediaFormat mediaFormat = null;

    private boolean isPaused = false;

    private long audioRealTimeOffset = 0l;
    private long currentRealTime = 0l;
    private long shiftedRealTime = 0l;
    private long globalRealTimeOffset = 0l;
    private static final int SKIP_FRAME_DELTA = 100000;


    private long neededPosition = 0;
    private boolean inSkipState = false;

    private ScheduledExecutorService playBackService = null;
    private Object audioPlayBackSyncObject = new Object();

    private LinkedList<AudioTask> queue = new LinkedList<AudioTask>();

    public AudioRender() {
        playBackService = Executors.newScheduledThreadPool(1);
    }

    private IAudioTrack createAudioTrack() {
        int sampleRate = getSampleRate();
        int audioChannelConfig = getAudioChannelsConfig(getAudioFormat());
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        /// TODO: To build implement an apropriate factory
        //int bufferSize = IAudioTrack.getMinBufferSize(sampleRate, audioChannelConfig, audioFormat);
        //IAudioTrack audioTrack = new IAudioTrack(AudioManager.STREAM_MUSIC, sampleRate, audioChannelConfig, audioFormat, bufferSize, IAudioTrack.MODE_STREAM);
        IAudioTrack audioTrack = null;

        return audioTrack;
    }

    private int getAudioChannelsConfig(AudioFormat audioFormat) {
        if (audioFormat.getAudioChannelCount() == 1) {
            return AudioFormat.CHANNEL_OUT_MONO;
        } else {
            return AudioFormat.CHANNEL_OUT_STEREO;
        }
    }

    @Override
    public int getTrackIdByMediaFormat(MediaFormat mediaFormat) {
        return 0;
    }

    @Override
    public void start() {
        initInputCommandQueue();

        if(audioPlayBack != null) audioPlayBack.play();
        else throw new NullPointerException("AudioPlayBack is not initialized");
    }


    // this function is needed to be called to init audioTrack upon start and when outputFormat is changing
    @Override
    public void configure() {
        if(mediaFormat != null) audioPlayBack = createAudioTrack();
        else throw new IllegalStateException("AudioRender mediaFormat is not initialized");
    }


    @Override
    public void push(Frame frame) {
        if (!isPaused) feedMeIfNotDraining();
    }

    @Override
    public void setMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    @Override
    public void close() throws IOException {
        if (audioPlayBack != null) {
            audioPlayBack.stop();
            audioPlayBack.release();
        }
    }

    @Override
    public boolean canConnectFirst(IOutputRaw connector) {
        return true;
    }

    @Override
    public void fillCommandQueues() {
    }

    @Override
    protected void initInputCommandQueue() {
        getInputCommandQueue().queue(Command.NeedData, getTrackId());
    }

    @Override
    public void drain(int bufferIndex) {
        setState(PluginState.Drained);
        getInputCommandQueue().clear();
    }

    private boolean inputQueueIsEmpty() {
        if(getInputCommandQueue().size() == 0) return true;
        return false;
    }


    @Override
    public void pushWithReleaser(Frame frame, IPluginOutput releaser) {
        if (isSkipPlayToWaitSamples(frame.getSampleTime())) {
            feedMeIfNotDraining();
            releaser.releaseOutputBuffer(frame.getBufferIndex());
            return;
        }

        int frameLength = frame.getLength();
        long sampleTime = frame.getSampleTime();
        byte[] inputBytes = new byte[frameLength];

        frame.getByteBuffer().position(0);
        frame.getByteBuffer().get(inputBytes, 0, frameLength);

        if (globalRealTimeOffset == 0) {
            queue.add(new AudioTask(inputBytes, sampleTime));

            //Log.d("Render", "globalRealTimeOffset = " + globalRealTimeOffset);
        } else {
            //Log.e("Render", "queue.size() = " + queue.size());

            currentRealTime = System.nanoTime() / 1000;
            shiftedRealTime = currentRealTime - globalRealTimeOffset;

            if (shiftedRealTime >= sampleTime) {
                writeDataToAudioTrack(inputBytes, frameLength);
            } else {
                long audioPlayingDelay = sampleTime - shiftedRealTime;
                playBackService.schedule(new AudioPlayingThread(inputBytes, frameLength, sampleTime, shiftedRealTime, globalRealTimeOffset),
                        audioPlayingDelay,
                        TimeUnit.MICROSECONDS);
            }
        }

        if (!isPaused) feedMeIfNotDraining();
        releaser.releaseOutputBuffer(frame.getBufferIndex());
    }

    private void writeDataToAudioTrack(byte[] audioData, int length) {
        synchronized (audioPlayBackSyncObject) {
            if (!isPaused) {
                audioPlayBack.write(audioData, 0, length);
            } else {
                queue.add(new AudioTask(audioData, length));
            }
        }
    }


    // TODO may be should pause the audioTrack
    public void pause() {
        isPaused = true;
    }

    public void resume() {
        resetGlobalRealTimeOffset();
        feedMeIfNotDraining();
        isPaused = false;
    }

    private void resetGlobalRealTimeOffset() {
        globalRealTimeOffset = 0l;
    }

    private AudioFormat getAudioFormat() {
        return (AudioFormat) mediaFormat;
    }

    public int getSampleRate() {
        return getAudioFormat().getAudioSampleRateInHz();
    }

    public int getChannelCount() {
        return getAudioFormat().getAudioChannelCount();
    }

    @Override
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    private boolean isSkipPlayToWaitSamples(long sampleTime) {
        if (inSkipState) {
            if (((sampleTime - neededPosition) < SKIP_FRAME_DELTA) && ((sampleTime - neededPosition) > -SKIP_FRAME_DELTA)) {
                inSkipState = false;
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    public void seek(long position) {
        queue.clear();
        resetGlobalRealTimeOffset();
        feedMeIfNotDraining();

        inSkipState = true;
        this.neededPosition = position;
    }

    public class AudioPlayingThread implements Runnable {

        private byte[] audioData = null;
        private int audioDataLength = 0;

        private long sampleTime = 0l;
        private long proposedTime = 0l;
        private long gap = 0l;

        public AudioPlayingThread(byte[] audioData, int audioDataLength, long sampleTime, long proposedTime, long gap) {
            this.audioData = audioData;
            this.audioDataLength = audioDataLength;
            this.sampleTime = sampleTime;
            this.proposedTime = proposedTime;
            this.gap = gap;
        }

        @Override
        public void run() {
            /*synchronized (audioPlayBackSyncObject) {
                if (!isPaused) {
                    //Log.d("Render", "Time of real audio playing = " + String.valueOf(System.nanoTime() / 1000 - gap));
                    audioPlayBack.write(audioData, 0, audioDataLength);
                } else {
                    queue.add(new AudioTask(audioData, audioDataLength));
                }

              *//*  Log.d("Render", "shiftedRealTime = " + String.valueOf(proposedTime));
                Log.e("Render", "sampleTime = " + String.valueOf(sampleTime));*//*
            }*/

            writeDataToAudioTrack(audioData, audioDataLength);
        }
    }


    public void setRealTimeOffset(long offset){
        globalRealTimeOffset = offset;
    }

    public long getRealTimeOffset(){
        return globalRealTimeOffset;
    }

    @Override
    public void syncSampleTimes(long videoRealTimeOffset) {
        if (videoRealTimeOffset > audioRealTimeOffset) {
            globalRealTimeOffset = videoRealTimeOffset;
        } else {
            globalRealTimeOffset = audioRealTimeOffset;
        }

        submitBufferedData();
    }


    public class AudioTask {
        private byte[] audioData = null;
        private long pts = 0l;

        public AudioTask(byte[] audioData, long pts) {
            this.audioData = audioData;
            this.pts = pts;
        }

        byte[] getAudioByteArray() {
            return audioData;
        }

        long getAudioSampleTime() {
            return pts;
        }

        int getAudioArrayLength() {
            return audioData.length;
        }
    }

    private void submitBufferedData() {
        if (queue.size() != 0) {
            AudioTask headTask = queue.poll();
            long headOffset = headTask.getAudioSampleTime();

            playBackService.submit(new AudioPlayingThread(
                    headTask.getAudioByteArray(),
                    headTask.getAudioArrayLength(), 0, 0, 0));

            for (AudioTask task : queue) {
                long audioPlayingDelay = task.getAudioSampleTime() - headOffset;

                playBackService.schedule(new AudioPlayingThread(
                                task.getAudioByteArray(),
                                task.getAudioArrayLength(),
                    task.getAudioSampleTime(),
                    shiftedRealTime, globalRealTimeOffset), audioPlayingDelay, TimeUnit.MICROSECONDS);
            }

            queue.clear();
        }
    }
}
