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

import android.media.MediaCodec;

import org.m4m.domain.IEglContext;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class MediaCodecDecoderPlugin implements IMediaCodec {
    protected MediaCodec mediaCodec;

    private ByteBuffer[] outputBuffers;
    private MediaCodec.BufferInfo outputBufferInfo;

    private ByteBuffer[] inputBuffers;
    private MediaCodec.BufferInfo inputBufferInfo;

    public MediaCodecDecoderPlugin(String mime) {

        try {
            this.mediaCodec = MediaCodec.createDecoderByType(mime);
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        outputBufferInfo = new MediaCodec.BufferInfo();
        inputBufferInfo = new MediaCodec.BufferInfo();
    }

    @Override
    public abstract void configure(MediaFormat mediaFormat, ISurfaceWrapper surface, int flags);

    @Override
    public void start() {
        mediaCodec.start();
        inputBuffers = null;
        outputBuffers = null;
    }

    @Override
    public void releaseOutputBuffer(int bufferIndex, boolean render) {
        mediaCodec.releaseOutputBuffer(bufferIndex, render);
    }

    @Override
    public ISurface createInputSurface() {
        return null;
    }

    @Override
    public ISurface createSimpleInputSurface(IEglContext eglSharedContext) {
        return null;
    }

    @Override
    public ByteBuffer[] getInputBuffers() {
        if (inputBuffers == null) {
            inputBuffers = mediaCodec.getInputBuffers();
        }

        return inputBuffers;
    }

    @Override
    public ByteBuffer[] getOutputBuffers() {
        if (outputBuffers == null) {
            outputBuffers = mediaCodec.getOutputBuffers();
        }

        return outputBuffers;
    }

    @Override
    public void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) {
        mediaCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
    }

    @Override
    public int dequeueInputBuffer(long timeout) {
        return mediaCodec.dequeueInputBuffer(timeout);
    }

    public int dequeueOutputBuffer(BufferInfo bufferInfo, long timeout) {
        int result = mediaCodec.dequeueOutputBuffer(outputBufferInfo, timeout);

        if (result == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            outputBuffers = null;
            getOutputBuffers();
        }

        BufferInfoTranslator.convertFromAndroid(outputBufferInfo, bufferInfo);

        return result;
    }

    @Override
    public MediaFormat getOutputFormat() {
        return MediaFormatTranslator.toDomain(mediaCodec.getOutputFormat());
    }

    @Override
    public void signalEndOfInputStream() {
    }

    @Override
    public void stop() {
        mediaCodec.stop();
    }
}
