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

import static org.junit.Assert.*;

public class EofFrameTest extends TestBase {
    @Test
    public void eofEqualsToEof() {
        assertEquals(Frame.EOF(), Frame.EOF());
        assertTrue(Frame.EOF().equals(Frame.EOF()));
    }

    @Test
    public void eofNotEqualsToRegularFrame() {
        Frame frame = create.frame().construct();
        Frame eof = Frame.EOF();

        assertFalse(eof.equals(frame));
        assertFalse(frame.equals(eof));
    }

    @Test
    public void eofEqualsToFrameWithEofFLag() {
        Frame frame = create.frame().construct();
        frame.setFlags(IMediaCodec.BUFFER_FLAG_END_OF_STREAM);
        Frame eof = Frame.EOF();

        assertTrue(eof.equals(frame));
        assertTrue(frame.equals(eof));
    }
}
