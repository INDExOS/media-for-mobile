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

package org.m4m.android;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import org.m4m.Uri;
import org.m4m.domain.IMediaExtractor;
import org.m4m.domain.MediaFormat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class implements a wrapper around MediaExtractor class to simplify its usage in the media pipeline.
 */
public class MediaExtractorPlugin implements IMediaExtractor {
    private MediaExtractor mediaExtractor = new MediaExtractor();
    private String path;
    private FileDescriptor fileDescriptor;
    private Context context;
    private Uri uri;

    public void setDataSource(String path) throws IOException {
        this.path = path;
        mediaExtractor.setDataSource(path);
    }

    public void setDataSource(FileDescriptor fileDescriptor) throws IOException {
        this.fileDescriptor = fileDescriptor;
        mediaExtractor.setDataSource(fileDescriptor);
    }

    public void setDataSource(Context context, Uri uri) throws IOException {
        this.context = context;
        this.uri = uri;
        mediaExtractor.setDataSource(context, android.net.Uri.parse(uri.getString()), null);
    }

    @Override
    public int getTrackCount() {
        return mediaExtractor.getTrackCount();
    }

    @Override
    public MediaFormat getTrackFormat(int index) {
        if (mediaExtractor.getTrackFormat(index).getString(android.media.MediaFormat.KEY_MIME).contains("video")) {
            return new VideoFormatAndroid(mediaExtractor.getTrackFormat(index));
        } else if (mediaExtractor.getTrackFormat(index).getString(android.media.MediaFormat.KEY_MIME).contains("audio")) {
            return new AudioFormatAndroid(mediaExtractor.getTrackFormat(index));
        }
        return null;
    }

    @Override
    public void selectTrack(int index) {
        mediaExtractor.selectTrack(index);
    }

    @Override
    public void unselectTrack(int index) {
        mediaExtractor.unselectTrack(index);
    }

    @Override
    public int getSampleTrackIndex() {
        return mediaExtractor.getSampleTrackIndex();
    }

    @Override
    public boolean advance() {
        return mediaExtractor.advance();
    }

    @Override
    public void release() {
        mediaExtractor.release();
    }

    @Override
    public int getSampleFlags() {
        return mediaExtractor.getSampleFlags();
    }

    @Override
    public void seekTo(long timeUs, int mode) {
        mediaExtractor.seekTo(timeUs, mode);
    }

    @Override
    public int getRotation() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (path != null) {
            retriever.setDataSource(path);
        } else if (fileDescriptor != null) {
            retriever.setDataSource(fileDescriptor);
        } else if (uri != null) {
            retriever.setDataSource(context, android.net.Uri.parse(uri.getString()));
        } else {
            throw new IllegalStateException("File not set");
        }
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        retriever.release();
        return Integer.parseInt(rotation);
    }

    @Override
    public int readSampleData(ByteBuffer inputBuffer) {
        return mediaExtractor.readSampleData(inputBuffer, 0);
    }

    @Override
    public long getSampleTime() {
        return mediaExtractor.getSampleTime();
    }

    @Override
    public String getFilePath() {
        return path;
    }

    @Override
    public FileDescriptor getFileDescriptor() { return fileDescriptor; }

    @Override
    public Uri getUri() {
        return uri;
    }
}
