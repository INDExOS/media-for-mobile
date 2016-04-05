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
import org.m4m.domain.Pair;
import org.m4m.domain.dsl.MediaSourceFather;
import org.junit.Test;

import javax.naming.OperationNotSupportedException;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class WhenReportProgress extends MediaComposerTest {
    @Test
    public void report_0_WhenStartProcessing() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer().with(progressListener).construct();

        mediaComposer.start();
        waitUntilStarted(progressListener);

        assertThat(progressListener).containsProgress(0);
    }

    @Test
    public void report_1_WhenDoneProcessing() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer().with(progressListener).with(Frame.EOF()).construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        assertThat(progressListener).containsProgress(0, 1);
    }

    @Test
    public void report_0_5_WhenProcessed1FrameInTheMiddle() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
                .withDuration(1000)
                .with(a.frame().withTimeStamp(500).construct())
                .with(progressListener).construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        assertThat(progressListener).containsProgress(0f, 0.5f, 1f);
    }

    @Test
    public void report_WhenProcessed2FramesInTheMiddle() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
                .withDuration(1000)
                .with(a.frame().withTimeStamp(250).construct())
                .with(a.frame().withTimeStamp(500).construct())
                .with(a.frame().withTimeStamp(800).construct())
                .with(progressListener).construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        assertThat(progressListener).containsProgress(0f, 0.25f, 0.5f, 0.8f, 1f);
    }

    @Test
    public void report_WhenProcessSegments() throws InterruptedException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
                .withDuration(1000)
                .with(a.frame().withTimeStamp(50).construct())
                .with(a.frame().withTimeStamp(100).construct())
                .with(a.frame().withTimeStamp(150).construct())
                .with(progressListener).construct();

        mediaComposer.getSourceFiles().get(0).addSegment(new Pair<Long, Long>(0L, 200L));
        mediaComposer.start();
        waitUntilDone(progressListener);

        assertThat(progressListener).containsProgress(0f, 0.25f, 0.5f, 0.75f, 1f);
    }

    @Test
    public void waitUntilDecoderInitialized_whenInitIsReallySlow() throws InterruptedException {
        MediaSourceFather mediaSource = create.mediaSource().with(a.frame().withTimeStamp(500).construct());
        IMediaCodec mediaCodec = create.mediaCodec().withDequeueInputBufferIndex(-1, -1, -1, -1, -1, 0).construct();
        mediaComposer = create.mediaComposer()
                .withDuration(1000)
                .with(mediaSource)
                .withDecoderMediaCodec(mediaCodec)
                .with(progressListener).construct();

        mediaComposer.start();
        waitUntilDone(progressListener);

        verify(mediaCodec).queueInputBuffer(anyInt(), anyInt(), anyInt(), eq(500L), anyInt());
    }
}
