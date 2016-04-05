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

import org.m4m.VideoFormat;

public class VideoDecoder extends Decoder implements IVideoOutput {
    public VideoDecoder(IMediaCodec mediaCodec) {
        super(mediaCodec, MediaFormatType.VIDEO);
    }

    @Override
    public void drain(int bufferIndex) {
        if (state != PluginState.Normal) return;

        super.drain(bufferIndex);
        mediaCodec.signalEndOfInputStream();
    }

    @Override
    public void push(Frame frame) {
        if (state == PluginState.Draining || state == PluginState.Drained) {
            throw new RuntimeException("Out of order operation.");
        }
        super.push(frame);
    }

    @Override
    public void stop() {
        super.stop();
        recreate();
    }

    @Override
    protected void initInputCommandQueue() {
        getInputCommandQueue().queue(Command.NeedInputFormat, getTrackId());
    }

    public Resolution getOutputResolution() {
        return ((VideoFormat) getOutputMediaFormat()).getVideoFrameSize();
    }
}

