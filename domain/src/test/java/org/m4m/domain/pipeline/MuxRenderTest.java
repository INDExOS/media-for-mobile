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

package org.m4m.domain.pipeline;

import org.m4m.IProgressListener;
import org.m4m.VideoFormat;
import org.m4m.domain.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.m4m.domain.Command;
import org.m4m.domain.Frame;
import org.m4m.domain.IMediaCodec;
import org.m4m.domain.IMediaMuxer;
import org.m4m.domain.MuxRender;
import org.m4m.domain.ProgressTracker;

import java.io.IOException;

import static org.mockito.Mockito.*;


public class MuxRenderTest extends TestBase {

    @Test
    public void setInputMediaFormat_configureFrameBuffer()
    {
        VideoFormat videoFormat = create
                .videoFormat()
                .withFrameSize(720, 480)
                .withBitRate(1000)
                .withFrameRate(30)
                .withIFrameInterval(4)
                .construct();

        IMediaMuxer muxer = create.mediaMuxer().construct();
        MuxRender muxRender = (MuxRender)create.muxRender().with(muxer).construct();
        MuxRender muxRenderSpy = spy(muxRender);

        muxRender.start();

        muxRenderSpy.setMediaFormat(videoFormat);

        verify(muxRenderSpy).setMediaFormat(videoFormat);
    }


    @Test
    public void writeSampleData_canTrack()
    {
        Frame frame = create.frame().withBuffer(1,2,3).construct();
        IMediaMuxer muxer = create.mediaMuxer().construct();
        ProgressTracker progressTracker = mock(ProgressTracker.class);

        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = 2;
        bufferInfo.presentationTimeUs = 100;
        bufferInfo.size = 5;

        muxer.writeSampleData(1, frame.getByteBuffer(), bufferInfo);
        progressTracker.track(10);

        verify(muxer).writeSampleData(1, frame.getByteBuffer(), bufferInfo);
        verify(progressTracker).track(10);
    }

    @Test
    public void configure_putsNeedInputFormatCommand() {
        MuxRender muxRender = (MuxRender)create.muxRender().construct();

        muxRender.configure();

        assertThat(muxRender.getInputCommandQueue()).equalsTo(Command.NeedInputFormat);
    }

    @Test
    public void push_putsNeedInputFormatCommand() {
        MuxRender muxRender = (MuxRender)create.muxRender().construct();
        Frame frame = mock(Frame.class);

        // frameBuffer.areAllTracksConfigured() = false
        muxRender.configure();
        muxRender.getInputCommandQueue().clear();

        muxRender.push(frame);

        assertThat(muxRender.getInputCommandQueue()).equalsTo(Command.NeedInputFormat);
    }

    @Test (expected = IllegalStateException.class)
    public void getTrackIdByMediaFormat_throwsIllegalStateException(){
        MuxRender muxRender = (MuxRender)create.muxRender().construct();
        VideoFormat videoFormat = mock(VideoFormat.class);

        muxRender.getTrackIdByMediaFormat(videoFormat);
    }

    @Test
    public void drain_feedIfNotDraining(){
        MuxRender muxRender = (MuxRender) create.muxRender().construct();

        muxRender.drain(0);

        assertThat(muxRender.getInputCommandQueue()).equalsTo(Command.NeedData);
    }

    @Test
     public void drain_putsNeedInputFormatCommand(){
        MuxRender muxRender = (MuxRender) create.muxRender().construct();
        VideoFormat videoFormat = mock(VideoFormat.class);


        muxRender.setMediaFormat(videoFormat);
        // frameBuffer.areAllTracksConfigured() = false
        muxRender.drain(0);

        assertThat(muxRender.getInputCommandQueue()).equalsTo(Command.NeedInputFormat);
    }

    @Test
    public void drain_notifyOnStop(){
        IProgressListener onStopListener = create.progressListener().construct();
        IMediaMuxer muxer = create.mediaMuxer().construct();
        MuxRender muxRender = (MuxRender)create.muxRender().withProgressListener(onStopListener).with(muxer).construct();

        //drainCount = connectedPluginsCount
        muxRender.start();
        muxRender.configure();
        muxRender.drain(0);

        verify(onStopListener).onMediaStop();
        verify(muxer).stop();
        verify(muxer).release();
    }

    @Test
    public void start_muxerStarts_CommandQueueIsEmpty(){
        IMediaMuxer muxer = create.mediaMuxer().construct();
        MuxRender muxRender = (MuxRender)create.muxRender().with(muxer).construct();

        muxRender.start();

        verify(muxer).start();
        Assert.assertEquals((muxRender.getInputCommandQueue().size()), 0);
    }

    @Test
    public void muxerClose()throws IOException{
        IMediaMuxer muxer = create.mediaMuxer().construct();
        MuxRender muxRender = (MuxRender)create.muxRender().with(muxer).construct();
        muxRender.start();

        muxRender.close();

        verify(muxer).stop();
        verify(muxer).release();
    }

    @Test
    public void muxerNull_notClose()throws IOException{
        IMediaMuxer muxer = mock(IMediaMuxer.class);
        MuxRender muxRender = (MuxRender)create.muxRender().with(muxer).construct();

        muxRender.close();
        verify(muxer, never()).stop();
        verify(muxer, never()).release();
    }



    @Test
    public void getTrackIdByMediaFormat_returnTrackId()throws IllegalStateException{
        IMediaMuxer muxer = mock(IMediaMuxer.class);
        MuxRender muxRender = (MuxRender)create.muxRender().with(muxer).construct();
        VideoFormat videoFormat = mock(VideoFormat.class);
        VideoFormat audioFormat = mock(VideoFormat.class);

        int expectedVideoFormat = 0;
        int expectedAudioFormat = 0;

        muxRender.setMediaFormat(videoFormat);
        muxRender.setMediaFormat(audioFormat);

        int actualVideoFormat = muxRender.getTrackIdByMediaFormat(videoFormat);
        int actualAudioFormat = muxRender.getTrackIdByMediaFormat(audioFormat);

        Assert.assertEquals(expectedVideoFormat, actualVideoFormat);
        Assert.assertEquals(expectedAudioFormat, actualAudioFormat);
    }
}
