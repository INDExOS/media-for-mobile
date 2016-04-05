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
import android.media.MediaMuxer;

import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaMuxerPlugin implements IMediaMuxer {
    private final MediaMuxer mediaMuxer;

    private long[] lastPresentationTime = new long[2];

    public MediaMuxerPlugin(String filename, int outputFormat) throws IOException {
        mediaMuxer = new MediaMuxer(filename, outputFormat);
    }

    @Override
    public int addTrack(MediaFormat mediaFormat) {
        return mediaMuxer.addTrack(MediaFormatTranslator.from(mediaFormat));
    }

    @Override
    public void release() {
        mediaMuxer.release();
    }

    @Override
    public void setOrientationHint(int degrees) {
        mediaMuxer.setOrientationHint(degrees);
    }

    @Override
    public void start() {
        mediaMuxer.start();
    }

    @Override
    public void stop() {
        mediaMuxer.stop();
    }

    @Override
    public void writeSampleData(int trackIndex, ByteBuffer buffer, IMediaCodec.BufferInfo bufferInfo) {
        //  //Log.i("MMP", "writeSampleData: trackId = " + trackIndex);
        //  //Log.i("MMP", "writeSampleData: pts = " + bufferInfo.presentationTimeUs);
        //  //Log.i("MMP", "writeSampleData: size = " + bufferInfo.size);
        //  //Log.i("MMP", "writeSampleData: flags = " + bufferInfo.flags);


        //Log.e("MMP", "writeSampleData: pts = " + bufferInfo.presentationTimeUs);

        if(bufferInfo.size == 0)
        {
            return;
        }

        if(lastPresentationTime[trackIndex] > bufferInfo.presentationTimeUs)
        {
            return;
        }

        //for some reason mediamuxer donot need that
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
            //ignore codec config
            return;
        }

        lastPresentationTime[trackIndex] =  bufferInfo.presentationTimeUs;

        mediaMuxer.writeSampleData(trackIndex, buffer, ByteBufferTranslator.from(bufferInfo));
    }
}
