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
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.opengl.EGLContext;

import org.m4m.domain.graphics.IEglUtil;

import org.m4m.domain.IEglContext;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.IWrapper;
import org.m4m.domain.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaCodecEncoderPlugin implements IMediaCodec {
    private MediaCodec mediaCodec;

    private ByteBuffer[] outputBuffers;
    private MediaCodec.BufferInfo outputBufferInfo;

    private ByteBuffer[] inputBuffers;
    private MediaCodec.BufferInfo inputBufferInfo;
    private IEglUtil eglUtil;

    private MediaCodecEncoderPlugin(IEglUtil eglUtil) {
        this.eglUtil = eglUtil;
        init();
    }
    public MediaCodecEncoderPlugin(String mime, IEglUtil eglUtil) {

        try {
            this.eglUtil = eglUtil;
            init();
            this.mediaCodec = MediaCodec.createEncoderByType(mime);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        outputBufferInfo = new MediaCodec.BufferInfo();
        inputBufferInfo = new MediaCodec.BufferInfo();
    }

    public static MediaCodecEncoderPlugin createByCodecName(String mime, IEglUtil eglUtil) {
        MediaCodecEncoderPlugin plugin = new MediaCodecEncoderPlugin(eglUtil);
        MediaCodecInfo audioCodecInfo = selectCodec(mime);

        String codecName = audioCodecInfo.getName();
        if (codecName != null) {

            try {
                plugin.mediaCodec = MediaCodec.createByCodecName(codecName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return plugin;
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();

            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    @Override
    public void configure(MediaFormat mediaFormat, ISurfaceWrapper surface, int flags) {
        if (mediaFormat.getMimeType().startsWith("video")) {
            mediaCodec.configure(MediaFormatTranslator.from(mediaFormat),
                                 surface == null ? null : ((SurfaceWrapper) surface).getNativeObject(),
                                 null,
                                 flags);
        } else if (mediaFormat.getMimeType().startsWith("audio")) {
            mediaCodec.configure(MediaFormatTranslator.from(mediaFormat), null, null, flags);
        }
    }

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
        return new Surface(mediaCodec, eglUtil);
    }

    @Override
    public ISurface createSimpleInputSurface(IEglContext eglSharedContext) {
        return new SimpleSurface(mediaCodec, ((IWrapper<EGLContext>) eglSharedContext).getNativeObject());
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
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.queueInputBuffer("
//                + index +", "+ offset+", " + size +", "+ presentationTimeUs +", "+ flags +")");
        mediaCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
    }

    @Override
    public int dequeueInputBuffer(long timeout) {
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.dequeueInputBuffer()+");
        int index = mediaCodec.dequeueInputBuffer(timeout);
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.dequeueInputBuffer()= "+ index);
        return index;
    }

    @Override
    public int dequeueOutputBuffer(BufferInfo bufferInfo, long timeout) {
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.dequeueInputBuffer()+");
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
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.signalEndOfInputStream();");
        mediaCodec.signalEndOfInputStream();
    }

    @Override
    public void stop() {
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.stop();");
        mediaCodec.stop();
    }

    @Override
    public void release() {
        //Log.i("MediaCodecEncoderPlugin", "mediaCodec.release();");
        mediaCodec.release();
    }

    @Override
    public void recreate() {

    }
}
