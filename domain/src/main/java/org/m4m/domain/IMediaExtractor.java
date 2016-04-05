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

import org.m4m.Uri;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

/**
 * IMediaExtractor abstract interface for MediaExtractor class
 */
public interface IMediaExtractor {
    public static final int SEEK_TO_PREVIOUS_SYNC = 0;
    public static final int SEEK_TO_NEXT_SYNC = 1;
    public static final int SEEK_TO_CLOSEST_SYNC = 2;

    int readSampleData(ByteBuffer inputBuffer);

    MediaFormat getTrackFormat(int i);

    long getSampleTime();

    boolean advance();

    int getTrackCount();

    void selectTrack(int index);

    void unselectTrack(int index);

    int getSampleTrackIndex();

    void release();

    int getSampleFlags();

    void seekTo(long timeUs, int mode);

    int getRotation();

    String getFilePath();

    FileDescriptor getFileDescriptor();

    Uri getUri();
}
