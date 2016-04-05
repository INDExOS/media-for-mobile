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

import org.m4m.domain.IMediaMuxer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MediaMuxerFather {
    private final IMediaMuxer mediaMuxer;

    public MediaMuxerFather() {
        mediaMuxer = mock(IMediaMuxer.class);
    }

    public MediaMuxerFather withSleepOnStart(final int sleep) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(sleep);
                return null;
            }
        }).when(mediaMuxer).start();
        return this;
    }

    public IMediaMuxer construct() {
        return mediaMuxer;
    }
}
