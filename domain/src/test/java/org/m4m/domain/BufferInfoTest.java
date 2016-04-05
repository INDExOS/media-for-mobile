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

import org.junit.Test;
import org.m4m.domain.IMediaCodec;

import static org.junit.Assert.*;

public class BufferInfoTest {
    @Test
    public void isEof_WhenEofFlagSet() {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = IMediaCodec.BUFFER_FLAG_END_OF_STREAM;

        assertTrue(bufferInfo.isEof());
    }

    @Test
    public void isNotEof_WhenEofFlagNotSet() {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = 0;

        assertFalse(bufferInfo.isEof());
    }

    @Test
    public void returnRightHashCode() {
        IMediaCodec.BufferInfo bufferInfo = new IMediaCodec.BufferInfo();
        bufferInfo.flags = 1;
        bufferInfo.offset = 1;
        bufferInfo.presentationTimeUs = 3;
        bufferInfo.size = 2;
        assertEquals(bufferInfo.hashCode(), 30847);
    }

}
