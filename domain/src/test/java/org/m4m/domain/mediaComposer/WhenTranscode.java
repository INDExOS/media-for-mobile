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

package org.m4m.domain.mediaComposer;

import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.dsl.MediaSourceFather;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class WhenTranscode extends MediaComposerTest {
    @Test
    public void pushDataFromMediaSourceToDecoder() throws InterruptedException {
        Frame frame = create.frame().withTimeStamp(123).construct();
        MediaSourceFather mediaSource = create.mediaSource().with(frame).with(Frame.EOF());
        IMediaCodec mediaCodec = create.mediaCodec().construct();

        mediaComposer = create.mediaComposer().withDecoderMediaCodec(mediaCodec).with(mediaSource).with(progressListener).construct();
        mediaComposer.start();
        waitUntilDone(progressListener);

        verify(mediaCodec).queueInputBuffer(anyInt(), anyInt(), anyInt(), eq(123L), anyInt());
    }

    @Ignore
    @Test
    public void frameFromExtractorReachesMuxer() throws InterruptedException {
        Frame frame = create.frame(1, 2, 3).withFlag(8).withTimeStamp(555).withTrackId(0).construct();
        MediaSourceFather mediaSource = create.mediaSource().with(frame).with(Frame.EOF());

        IMediaMuxer muxer = create.mediaMuxer().construct();

        mediaComposer = create.mediaComposer().with(mediaSource).with(muxer).with(progressListener).construct();
        mediaComposer.start();
        waitUntilDone(progressListener);

        IMediaCodec.BufferInfo expectedBufferInfo = new IMediaCodec.BufferInfo();
        expectedBufferInfo.size = 3;
        expectedBufferInfo.flags = 8;
        expectedBufferInfo.presentationTimeUs = 555;
        verify(muxer).writeSampleData(eq(0), eq(create.byteBuffer(1, 2, 3)), eq(expectedBufferInfo));
    }
}
