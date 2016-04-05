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

package org.m4m;

import org.m4m.domain.MediaFormat;
import org.m4m.domain.Resolution;

/**
 * This class is used to describe and/or setup parameters of video data.
 */
public abstract class VideoFormat extends MediaFormat {
    public static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding

    private static final java.lang.String KEY_BIT_RATE = "bitrate";
    private static final java.lang.String KEY_COLOR_FORMAT = "color-format";
    private static final java.lang.String KEY_FRAME_RATE = "frame-rate";
    private static final java.lang.String KEY_I_FRAME_INTERVAL = "i-frame-interval";
    public static final java.lang.String KEY_HEIGHT = "height";
    public static final java.lang.String KEY_WIDTH = "width";

    private static final String NO_INFO_AVAILABLE = "No info available.";

    private String mimeType;
    private int width;
    private int height;

    protected void setVideoCodec(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns video codec MIME type.
     *
     * @return MIME type.
     */
    public String getVideoCodec() {
        return mimeType;
    }

    /**
     * Sets video frame size.
     *
     * @param width  Frame width.
     * @param height Frame height.
     */
    public void setVideoFrameSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns video frame size.
     *
     * @return {@link Resolution} Structure with frame width and height.
     */
    public Resolution getVideoFrameSize() {
        return new Resolution(width, height);
    }

    /**
     * Returns video bit rate in KBytes.
     *
     * @return Video bit rate in KBytes.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getVideoBitRateInKBytes() {
        try {
            return getInteger(KEY_BIT_RATE) / 1024;
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }

    /**
     * Sets video bit rate in KBytes.
     *
     * @param bitRate Video bit rate in KBytes.
     */
    public void setVideoBitRateInKBytes(int bitRate) {
        if (width * height * 30 * 2 * 0.00007 < bitRate) {
            bitRate = (int) (width * height * 30 * 2 * 0.00007);
        }
        setInteger(KEY_BIT_RATE, bitRate * 1024);
    }

    /**
     * Returns video frame rate in frames/sec.
     *
     * @return Video frame rate in frames/sec.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getVideoFrameRate() {
        try {
            return getInteger(KEY_FRAME_RATE);
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }

    /**
     * Sets video frame rate in frames/sec.
     *
     * @param bitRate Video frame rate in frames/sec.
     */
    public void setVideoFrameRate(int bitRate) {
        setInteger(KEY_FRAME_RATE, bitRate);
    }


    /**
     * Sets frequency of I frames expressed in secs between I frames.
     *
     * @param iFrameIntervalInSecs Frequency of I frames expressed in secs between I frames.
     */
    public void setVideoIFrameInterval(int iFrameIntervalInSecs) {
        setInteger(KEY_I_FRAME_INTERVAL, iFrameIntervalInSecs);
    }

    /**
     * Returns frequency of I frames expressed in secs between I frames.
     *
     * @return Frequency of I frames expressed in secs between I frames.
     * @throws RuntimeException when there is no information on the parameter.
     */
    public int getVideoIFrameInterval() {
        try {
            return getInteger(KEY_I_FRAME_INTERVAL);
        } catch (NullPointerException e) {
            throw new RuntimeException(NO_INFO_AVAILABLE);
        }
    }

    /**
     * Sets the color format of the content.
     * See http://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities.html for values.
     *
     * @param colorFormat Color format.
     */
    public void setColorFormat(int colorFormat) {
        setInteger(KEY_COLOR_FORMAT, colorFormat);
    }
}
