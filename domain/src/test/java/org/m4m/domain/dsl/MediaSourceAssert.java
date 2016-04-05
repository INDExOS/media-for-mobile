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

package org.m4m.domain.dsl;

import org.m4m.domain.Frame;
import org.m4m.domain.IOutput;
import org.junit.Assert;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MediaSourceAssert {
    private final Father create;
    private final IOutput mediaSource;

    public MediaSourceAssert(Father create, IOutput mediaSource) {
        this.create = create;
        this.mediaSource = mediaSource;
    }

    public void willPull(Frame... expectedFrames) {
        for (Frame expectedFrame : expectedFrames) {
            Frame actualFrame;
            if (expectedFrame.equals(Frame.EOF())) {
                actualFrame = create.frame().construct();
            } else {
                actualFrame = create.frame().withLength(expectedFrame.getLength()).construct();
            }

            mediaSource.pull(actualFrame);

            if (expectedFrame.equals(Frame.EOF())) {
                Assert.assertEquals(Frame.EOF().getLength(), actualFrame.getLength());
            } else {
                Assert.assertThat(actualFrame.getByteBuffer().array(), is(equalTo(expectedFrame.getByteBuffer().array())));
            }
        }
    }
}
