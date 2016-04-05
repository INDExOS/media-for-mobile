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

package org.m4m.domain.mediaComposer;

import org.m4m.IProgressListener;
import org.m4m.MediaComposer;
import org.m4m.domain.Render;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WhenMediaDone extends MediaComposerTest {
    private IProgressListener onMediaDoneProgressListener;

    @Before
    public void setUp() {
        onMediaDoneProgressListener = create.progressListener().withSyncOnMediaDone(sync).construct();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void closeRender() throws InterruptedException, IOException {
        Render render = mock(Render.class);
        MediaComposer mediaComposer = create.mediaComposer()
                .with(onMediaDoneProgressListener)
                .withRender(render)
                .construct();

        mediaComposer.start();
        waitUntilDone();

        verify(render).close();
    }

    @Test
    public void notifyOnMediaStop() throws InterruptedException {
        MediaComposer mediaComposer = create.mediaComposer()
                .with(onMediaDoneProgressListener)
                .with(a.frame().construct())
                .construct();

        mediaComposer.start();
        waitUntilDone();

        verify(onMediaDoneProgressListener).onMediaStop();
    }

    private void waitUntilDone() throws InterruptedException {
        synchronized (sync) {
            sync.wait(1000);
        }
    }
}
