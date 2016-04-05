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

import org.m4m.domain.Command;
import org.m4m.domain.CommandQueue;
import org.m4m.domain.Frame;
import org.m4m.domain.IAndroidMediaObjectFactory;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;
import org.m4m.domain.MediaFormatType;
import org.m4m.domain.MediaSource;
import org.m4m.domain.Pair;
import org.m4m.domain.VideoDecoder;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class provides information about a media file and returns uncompressed video frame at a position selected.
 */
public class MediaFileInfo {
    private IAndroidMediaObjectFactory factory = null;

    MediaFile file;
    MediaSource source;
    VideoDecoder videoDecoder;
    MediaFormat videoFormat = null;
    MediaFormat audioFormat = null;
    private ISurfaceWrapper outputSurface = null;
    private FileDescriptor fileDescriptor;

    /**
     * Instantiates an object with Android base-layer.
     *
     * @param factory
     * @see IAndroidMediaObjectFactory
     */
    public MediaFileInfo(IAndroidMediaObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * Sets input media file.
     *
     * @param fileName File name. String class, FileDescriptor class, or Uri class object.
     * @throws IOException when the file name is invalid or the file can not be opened.
     */
    public void setFileName(String fileName) throws IOException {
        this.source = factory.createMediaSource(fileName);
        prepareMediaFile();
    }

    /**
     * Sets input media file.
     *
     * @param fileDescriptor File descriptor.
     * @throws IOException when the file descriptor is invalid or the file can not be opened.
     */
    public void setFileDescriptor(FileDescriptor fileDescriptor) throws IOException {
        this.source = factory.createMediaSource(fileDescriptor);
        prepareMediaFile();
    }

    /**
     * Sets input media file.
     *
     * @param fileUri File URI.
     * @throws IOException when the file URI is invalid or the file can not be opened.
     */
    public void setUri(Uri fileUri) throws IOException {
        this.source = factory.createMediaSource(fileUri);
        prepareMediaFile();
    }

    /**
     * Gets the media file name.
     *
     * @return Media file name. String class object.
     */
    public String getFileName() {
        if (null == file) {
            return null;
        }
        return file.getFilePath();
    }

    /**
     * Gets the media file descriptor.
     *
     * @return Media file descriptor. FileDescriptor class object.
     */
    public FileDescriptor getFileDescriptor() {
        if (null == file) {
            return null;
        }
        return file.getFileDescriptor();
    }

    /**
     * Gets the media file URI.
     *
     * @return Input file URI. Uri class object.
     */
    public Uri getUri() {
        if (null == file) {
            return null;
        }
        return file.getUri();
    }


    private void prepareMediaFile() {
        this.file = new MediaFile(source);
        int index = 0;
        for (MediaFormat ignored : this.source.getMediaFormats()) {
            this.source.selectTrack(index++);
        }

        videoFormat = file.getVideoFormat(0);
        audioFormat = file.getAudioFormat(0);
    }

    /**
     * Sets output surface.
     */
    public void setOutputSurface(ISurfaceWrapper surface) {
        this.outputSurface = surface;
    }

    /**
     * Returns video track format if exists.
     *
     * @return MediaFormat
     */
    public MediaFormat getVideoFormat() {
        return videoFormat;
    }

    /**
     * Returns audio track format if exists.
     *
     * @return MediaFormat
     */
    public MediaFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Returns media file duration in microseconds.
     *
     * @return Duration in microseconds.
     */
    public long getDurationInMicroSec() {
        return file.getDurationInMicroSec();
    }

    /**
     * Returns an uncompressed video frame by selected time position.
     *
     * @param time   Time position to return frame for.
     * @param buffer ByteByffer allocated by the user to receive the uncompressed frame.
     */
    public void getFrameAtPosition(long time, ByteBuffer buffer) throws IOException {
        videoDecoder = factory.createVideoDecoder(videoFormat);
        videoDecoder.setMediaFormat(videoFormat);
        videoDecoder.setOutputSurface(outputSurface);
        videoDecoder.configure();
        videoDecoder.setTrackId(source.getTrackIdByMediaType(MediaFormatType.VIDEO));
        videoDecoder.start();

        if (null != audioFormat) {
            source.unselectTrack(source.getTrackIdByMediaType(MediaFormatType.AUDIO));
        }

        source.start();
        source.seek(time);

        Frame frame = null;
        Frame outputFrame = new Frame(buffer, 1920 * 1080 * 4, 0, 0, 0, 0);

        final CommandQueue sourceOutputQueue = source.getOutputCommandQueue();


        while (sourceOutputQueue.size() != 0) {

            Pair<Command, Integer> sourceOutputCommand = sourceOutputQueue.first();

            if (sourceOutputCommand == null || sourceOutputCommand.left == Command.EndOfFile) {
                break;
            }

            videoDecoder.fillCommandQueues();

            final CommandQueue videoDecoderInputQueue = videoDecoder.getInputCommandQueue();
            final Pair<Command, Integer> videoDecoderInputCommand = videoDecoderInputQueue.first();

            if (videoDecoderInputQueue.size() == 0 || videoDecoderInputCommand == null) {
                break;
            } else {
                if (videoDecoderInputCommand.left == Command.NeedData) {
                    frame = videoDecoder.findFreeFrame();
                } else if (videoDecoderInputCommand.left == Command.NeedInputFormat) {
                    videoDecoderInputQueue.dequeue();
                    videoDecoderInputQueue.queue(Command.NeedData, videoDecoder.getTrackId());
                    continue;
                }
            }

            if (frame != null) {
                source.pull(frame);
                videoDecoder.push(frame);

                sourceOutputQueue.dequeue();
                videoDecoderInputQueue.dequeue();
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final CommandQueue videoDecoderOutputQueue = videoDecoder.getOutputCommandQueue();
            final Pair<Command, Integer> videoDecoderOutputCommand = videoDecoderOutputQueue.first();

            if (videoDecoderOutputQueue.size() != 0 && videoDecoderOutputCommand != null) {
                if (videoDecoderOutputCommand.left == Command.HasData) {
                    if (outputSurface != null) {
                        Frame decoderFrame = videoDecoder.getFrame();
                        videoDecoder.releaseOutputBuffer(decoderFrame.getBufferIndex());
                    } else {
                        videoDecoder.pull(outputFrame);
                    }
                    break;
                } else if (videoDecoderOutputCommand.left == Command.OutputFormatChanged) {
                    videoDecoderOutputQueue.dequeue();
                }
            }
        }

        sourceOutputQueue.clear();
        videoDecoder.close();
    }

    /**
     * Returns the video rotation angle in degrees. Possible return values: 0, 90, 180, or 270 degrees.
     */
    public int getRotation() {
        return file.getRotation();
    }
}

