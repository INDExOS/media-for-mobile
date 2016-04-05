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

import org.m4m.MediaComposer;
import org.m4m.domain.TestBase;
import org.junit.After;
import org.junit.Before;

public class MediaComposerTest extends TestBase {
    protected ProgressListenerFake progressListener;
    protected MediaComposer mediaComposer;
    protected final Object sync = new Object();

    @Before
    public void setUp() throws RuntimeException {
        progressListener = new ProgressListenerFake();
        mediaComposer = create.mediaComposer().with(progressListener).construct();
    }

    @After
    public void tearDown() throws Exception {
        if (progressListener.getException() != null) {
            throw progressListener.getException();
        }
    }

    protected void waitUntilStarted(ProgressListenerFake progress) throws InterruptedException {
        while (!progress.isStarted()) {
            Thread.sleep(100);
        }
    }

    protected void waitUntilDone(ProgressListenerFake progress) throws InterruptedException {
        while (!progress.isDone()) {
            Thread.sleep(100);
        }
    }
}
