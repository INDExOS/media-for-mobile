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


import org.m4m.domain.CameraSource;
import org.m4m.domain.Command;
import org.m4m.domain.CommandQueue;
import org.m4m.domain.Frame;
import org.m4m.domain.ICameraSource;
import org.m4m.domain.ISurface;
import org.m4m.domain.Resolution;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public class CameraSourceFather extends FatherOf<ICameraSource> {
    private CameraSource camera;

    public CameraSourceFather(Father father) {
        super(father);
        camera = mock(CameraSource.class);
    }

    public CameraSourceFather with(CameraSource camera) {
        this.camera = camera;
        return this;
    }

    public CameraSourceFather with(final Frame frame) {
        when(camera.getFrame()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(10);
                return frame;
            }
        });
        return this;
    }


    @Override
    public CameraSource construct() {
        CommandQueue outputCommandQueue = new InfiniteCommandQueue(Command.HasData);
        outputCommandQueue.queue(Command.OutputFormatChanged, 0);
        when(camera.getOutputCommandQueue()).thenReturn(outputCommandQueue);
        when(camera.getOutputResolution()).thenReturn(new Resolution(0, 0));
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                camera.getOutputCommandQueue().queue(Command.EndOfFile, 0);
                return null;
            }
        }).when(camera).stop();

        return camera;
    }

    public CameraSource with(ISurface surface) {
        when(camera.getSurface()).thenReturn(surface);
        return camera;
    }
}
