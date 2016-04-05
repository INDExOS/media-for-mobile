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
import org.m4m.domain.IPluginOutput;
import org.m4m.domain.Render;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public class SinkAssert {
    private final Render sink;

    public SinkAssert(Render sink) {
        this.sink = sink;
    }

    public void willReceive(Frame expectedFrame) {
        ArgumentCaptor<Frame> frame = ArgumentCaptor.forClass(Frame.class);
        verify(sink).pushWithReleaser(frame.capture(), any(IPluginOutput.class));
        Assert.assertThat(frame.getValue().getByteBuffer().array(), is(equalTo(expectedFrame.getByteBuffer().array())));
    }
}
