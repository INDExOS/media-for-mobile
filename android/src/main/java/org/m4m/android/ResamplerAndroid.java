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

import org.m4m.AudioFormat;
import org.m4m.domain.Frame;
import org.m4m.domain.Resampler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ResamplerAndroid extends Resampler {

    //private final String TAG = "AMP";
    public int frameSize = 1024; // frame window

    private ByteBuffer internalBuffer;
    private IntBuffer numOutSamples = IntBuffer.allocate(1); // This is to emulate int* for the native code only.

    final private String resamplerLibName = "ippresample"; // libippresample.so

    public ResamplerAndroid(AudioFormat audioFormat) {
        super(audioFormat);
    }

    private void loadLibrary() {
        try {
            System.loadLibrary(resamplerLibName);
        } catch (LinkageError e) {
            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();
            //Log.e(TAG, message);
            throw new IllegalArgumentException("Could not load library: " + resamplerLibName);
        }
    }

    @Override
    protected void setup() {
        loadLibrary();
    }

    @Override
    protected void allocateInitInternalBuffers() {
        if (inputChannelCount == 2) {
            frameSize = 2048;
        } else {
            frameSize = 1024;
        }

        int size = ResampleGetSizeJNI(inputChannelCount, inputSampleRate, frameSize, targetChannelCount, targetSampleRate);
        internalBuffer = ByteBuffer.allocateDirect(size);
        ResampleInitJNI(inputChannelCount, inputSampleRate, frameSize, targetChannelCount, targetSampleRate, internalBuffer.array());

        super.allocateInitInternalBuffers();
    }

    // Refactored version of ResampleFrame function
    @Override
    public void resampleFrame(Frame frame) {
        super.resampleFrame(frame);

        if (resamplingRequired()) {
            int srcOffset, dstOffset, frameLen;
            int inSamplesDone = 0;

            srcOffset = dstOffset = 0;


            // for calculating outputLen need ((inLenShort * targetChannelCount/inputChannelCount)*((float)targetSampleRate/(float)inputSampleRate))
            // multiply by 2 as ipp function works with (short = 2 bytes) buffers, but we have (byte) buffers
            // input lenght in bytes
            int inLenByte = frame.getLength();
            // input lenght in samples ("short" - Ipp16s)
            int inLenShort = inLenByte / 2;


            int outputLenByte = (int) ((inLenByte * targetChannelCount / inputChannelCount) * ((float) targetSampleRate / (float) inputSampleRate)) * 2 + 2;
            int outputLenShort = outputLenByte / 2;


            short[] inputShorts = new short[inLenShort];
            short[] outputShorts = new short[outputLenShort];


            // copy input data from framebuffer to massive of shorts (simultaneously make a transfer from byte to short)
            (((ByteBuffer) frame.getByteBuffer().flip()).asShortBuffer()).get(inputShorts);

            while (true) {

                if (inLenShort - srcOffset > frameSize) {
                    // do resample of the full frame
                    frameLen = frameSize;
                } else {
                    frameLen = inLenShort - srcOffset;
                }


                inSamplesDone = ResampleFrameJNI(inputShorts, srcOffset, frameLen, outputShorts, dstOffset, numOutSamples.array(), internalBuffer.array());

                srcOffset += frameLen;
                dstOffset += numOutSamples.get(0);

                if ((srcOffset >= inLenShort) || (inSamplesDone == 0)) {
                    break;
                }
            }

            frame.setLength(2 * dstOffset);
            frame.getByteBuffer().limit(frame.getByteBuffer().capacity());

            frame.getByteBuffer().position(0);
            frame.getByteBuffer().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outputShorts, 0, dstOffset);
        }
    }



    // Refactores version of ResampleBuffer
    @Override
    public void resampleBuffer(ByteBuffer frameBuffer, int bufferLength) {
        super.resampleBuffer(frameBuffer, bufferLength);

        if (resamplingRequired()) {
            int srcOffset = 0;
            int dstOffset = 0;

            int inLenByte = bufferLength; // input length in octets ("byte" or Ipp8u)
            int inLenShort = inLenByte / 2; // input length in samples ("short" or Ipp16s)

            int outputLenByte = (int) ((inLenByte * targetChannelCount / inputChannelCount) * ((float) targetSampleRate / (float) inputSampleRate)) * 2 + 2;
            int outputLenShort = outputLenByte / 2;

            short[] inputShorts = new short[inLenShort];
            short[] outputShorts = new short[outputLenShort];

            // copy input data from framebuffer to massive of shorts (simultaneously make a transfer from byte to short)
            (((ByteBuffer) frameBuffer.flip()).asShortBuffer()).get(inputShorts);


            while (true) {
                int frameLen;

                if (inLenShort - srcOffset > frameSize) {
                    // do resample of the full frame
                    frameLen = frameSize;
                } else {
                    frameLen = inLenShort - srcOffset;
                }


                int inSamplesDone = ResampleFrameJNI(inputShorts, srcOffset, frameLen, outputShorts, dstOffset, numOutSamples.array(), internalBuffer.array());


                srcOffset += frameLen;
                dstOffset += numOutSamples.get(0);

                if ((srcOffset >= inLenShort) || (inSamplesDone == 0)) {
                    break;
                }
            }


            frameBuffer.limit(2 * dstOffset);
            frameBuffer.position(0);
            frameBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outputShorts, 0, dstOffset);
        }
    }

    // Declaration of native methods
    private native int ResampleGetSizeJNI(int inChannels, int inRate, int frameSize, int outChannels, int outRate);

    private native void ResampleInitJNI(int inChannels, int inRate, int frameSize, int outChannels, int outRate, byte[] pBuffer);

    private native int ResampleFrameJNI(short[] pSrc, int srcOffset, int inLen, short[] pDst, int dstOffset, int[] pOutLen, byte[] pBuffer);
}
