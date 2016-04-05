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

public final class MediaCodecInfo {
    public static final class CodecProfileLevel {
        public static final int AVCProfileBaseline = 1;
        public static final int AVCProfileMain = 2;
        public static final int AVCProfileExtended = 4;
        public static final int AVCProfileHigh = 8;
        public static final int AVCProfileHigh10 = 16;
        public static final int AVCProfileHigh422 = 32;
        public static final int AVCProfileHigh444 = 64;
        public static final int AVCLevel1 = 1;
        public static final int AVCLevel1b = 2;
        public static final int AVCLevel11 = 4;
        public static final int AVCLevel12 = 8;
        public static final int AVCLevel13 = 16;
        public static final int AVCLevel2 = 32;
        public static final int AVCLevel21 = 64;
        public static final int AVCLevel22 = 128;
        public static final int AVCLevel3 = 256;
        public static final int AVCLevel31 = 512;
        public static final int AVCLevel32 = 1024;
        public static final int AVCLevel4 = 2048;
        public static final int AVCLevel41 = 4096;
        public static final int AVCLevel42 = 8192;
        public static final int AVCLevel5 = 16384;
        public static final int AVCLevel51 = 32768;
        public static final int H263ProfileBaseline = 1;
        public static final int H263ProfileH320Coding = 2;
        public static final int H263ProfileBackwardCompatible = 4;
        public static final int H263ProfileISWV2 = 8;
        public static final int H263ProfileISWV3 = 16;
        public static final int H263ProfileHighCompression = 32;
        public static final int H263ProfileInternet = 64;
        public static final int H263ProfileInterlace = 128;
        public static final int H263ProfileHighLatency = 256;
        public static final int H263Level10 = 1;
        public static final int H263Level20 = 2;
        public static final int H263Level30 = 4;
        public static final int H263Level40 = 8;
        public static final int H263Level45 = 16;
        public static final int H263Level50 = 32;
        public static final int H263Level60 = 64;
        public static final int H263Level70 = 128;
        public static final int MPEG4ProfileSimple = 1;
        public static final int MPEG4ProfileSimpleScalable = 2;
        public static final int MPEG4ProfileCore = 4;
        public static final int MPEG4ProfileMain = 8;
        public static final int MPEG4ProfileNbit = 16;
        public static final int MPEG4ProfileScalableTexture = 32;
        public static final int MPEG4ProfileSimpleFace = 64;
        public static final int MPEG4ProfileSimpleFBA = 128;
        public static final int MPEG4ProfileBasicAnimated = 256;
        public static final int MPEG4ProfileHybrid = 512;
        public static final int MPEG4ProfileAdvancedRealTime = 1024;
        public static final int MPEG4ProfileCoreScalable = 2048;
        public static final int MPEG4ProfileAdvancedCoding = 4096;
        public static final int MPEG4ProfileAdvancedCore = 8192;
        public static final int MPEG4ProfileAdvancedScalable = 16384;
        public static final int MPEG4ProfileAdvancedSimple = 32768;
        public static final int MPEG4Level0 = 1;
        public static final int MPEG4Level0b = 2;
        public static final int MPEG4Level1 = 4;
        public static final int MPEG4Level2 = 8;
        public static final int MPEG4Level3 = 16;
        public static final int MPEG4Level4 = 32;
        public static final int MPEG4Level4a = 64;
        public static final int MPEG4Level5 = 128;
        public static final int AACObjectMain = 1;
        public static final int AACObjectLC = 2;
        public static final int AACObjectSSR = 3;
        public static final int AACObjectLTP = 4;
        public static final int AACObjectHE = 5;
        public static final int AACObjectScalable = 6;
        public static final int AACObjectERLC = 17;
        public static final int AACObjectLD = 23;
        public static final int AACObjectHE_PS = 29;
        public static final int AACObjectELD = 39;
        public static final int VP8Level_Version0 = 1;
        public static final int VP8Level_Version1 = 2;
        public static final int VP8Level_Version2 = 4;
        public static final int VP8Level_Version3 = 8;
        public static final int VP8ProfileMain = 1;
        public int profile;
        public int level;
    }

    public static final class CodecCapabilities {
        public static final int COLOR_FormatMonochrome = 1;
        public static final int COLOR_Format8bitRGB332 = 2;
        public static final int COLOR_Format12bitRGB444 = 3;
        public static final int COLOR_Format16bitARGB4444 = 4;
        public static final int COLOR_Format16bitARGB1555 = 5;
        public static final int COLOR_Format16bitRGB565 = 6;
        public static final int COLOR_Format16bitBGR565 = 7;
        public static final int COLOR_Format18bitRGB666 = 8;
        public static final int COLOR_Format18bitARGB1665 = 9;
        public static final int COLOR_Format19bitARGB1666 = 10;
        public static final int COLOR_Format24bitRGB888 = 11;
        public static final int COLOR_Format24bitBGR888 = 12;
        public static final int COLOR_Format24bitARGB1887 = 13;
        public static final int COLOR_Format25bitARGB1888 = 14;
        public static final int COLOR_Format32bitBGRA8888 = 15;
        public static final int COLOR_Format32bitARGB8888 = 16;
        public static final int COLOR_FormatYUV411Planar = 17;
        public static final int COLOR_FormatYUV411PackedPlanar = 18;
        public static final int COLOR_FormatYUV420Planar = 19;
        public static final int COLOR_FormatYUV420PackedPlanar = 20;
        public static final int COLOR_FormatYUV420SemiPlanar = 21;
        public static final int COLOR_FormatYUV422Planar = 22;
        public static final int COLOR_FormatYUV422PackedPlanar = 23;
        public static final int COLOR_FormatYUV422SemiPlanar = 24;
        public static final int COLOR_FormatYCbYCr = 25;
        public static final int COLOR_FormatYCrYCb = 26;
        public static final int COLOR_FormatCbYCrY = 27;
        public static final int COLOR_FormatCrYCbY = 28;
        public static final int COLOR_FormatYUV444Interleaved = 29;
        public static final int COLOR_FormatRawBayer8bit = 30;
        public static final int COLOR_FormatRawBayer10bit = 31;
        public static final int COLOR_FormatRawBayer8bitcompressed = 32;
        public static final int COLOR_FormatL2 = 33;
        public static final int COLOR_FormatL4 = 34;
        public static final int COLOR_FormatL8 = 35;
        public static final int COLOR_FormatL16 = 36;
        public static final int COLOR_FormatL24 = 37;
        public static final int COLOR_FormatL32 = 38;
        public static final int COLOR_FormatYUV420PackedSemiPlanar = 39;
        public static final int COLOR_FormatYUV422PackedSemiPlanar = 40;
        public static final int COLOR_Format18BitBGR666 = 41;
        public static final int COLOR_Format24BitARGB6666 = 42;
        public static final int COLOR_Format24BitABGR6666 = 43;
        public static final int COLOR_TI_FormatYUV420PackedSemiPlanar = 2130706688;
        public static final int COLOR_FormatSurface = 2130708361;
        public static final int COLOR_QCOM_FormatYUV420SemiPlanar = 2141391872;
        public int[] colorFormats;
    }
}
