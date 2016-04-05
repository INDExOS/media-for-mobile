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

import java.nio.ByteBuffer;

public interface IMediaCodec {
    public static final int CONFIGURE_FLAG_ENCODE = 1;
    public static final int BUFFER_FLAG_END_OF_STREAM = 4;
    public static final int BUFFER_FLAG_CODEC_CONFIG = 2;
    public static final int INFO_OUTPUT_BUFFERS_CHANGED = -3;
    public static final int INFO_OUTPUT_FORMAT_CHANGED = -2;
    public static final int INFO_TRY_AGAIN_LATER = -1;

    void configure(MediaFormat mediaFormat, ISurfaceWrapper surface, int flags);

    void start();

    void releaseOutputBuffer(int bufferIndex, boolean render);

    ISurface createInputSurface();

    ISurface createSimpleInputSurface(IEglContext eglSharedContext);

    ByteBuffer[] getInputBuffers();

    ByteBuffer[] getOutputBuffers();

    void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags);

    int dequeueInputBuffer(long timeout);

    int dequeueOutputBuffer(BufferInfo info, long timeout);

    MediaFormat getOutputFormat();

    void signalEndOfInputStream();

    void stop();

    void release();

    void recreate();

    public class BufferInfo {
        public int flags;
        public int offset;
        public long presentationTimeUs;
        public int size;

        public boolean isEof() {
            return (flags & IMediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BufferInfo that = (BufferInfo) o;

            if (flags != that.flags) return false;
            if (offset != that.offset) return false;
            if (presentationTimeUs != that.presentationTimeUs) return false;
            if (size != that.size) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = flags;
            result = 31 * result + offset;
            result = 31 * result + (int) (presentationTimeUs ^ (presentationTimeUs >>> 32));
            result = 31 * result + size;
            return result;
        }
    }
}
