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

import org.m4m.IProgressListener;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class ProgressListenerFather {
    private final IProgressListener listener = mock(IProgressListener.class);;
    private Father father;
    private Object errorSync ;
    private Object startSync;
    private Object stopSync;
    private Object progressSync;
    private Object mediaDoneSync;
    private Object mediaPauseSync;

    class NotifyAnswer implements Answer {
        private Object sync;
        public NotifyAnswer(Object sync){
            this.sync = sync;
        }
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            synchronized (sync) {
                sync.notifyAll();
            }
            return null;
        }
    }

    public ProgressListenerFather(Father father) {
        this.father = father;
        withSync(new Object());
    }

    public ProgressListenerFather withSyncOnError(final Object sync) {
        errorSync = sync;
        return this;
    }
    public ProgressListenerFather withSyncOnMediaStart(final Object sync) {
        startSync = sync;
        return this;
    }
    public ProgressListenerFather withSyncOnMediaStop(final Object sync) {
        stopSync  = sync;
        return this;
    }
    public ProgressListenerFather withSyncOnMediaProgress(final Object sync) {
        progressSync = sync;
        return this;
    }
    public ProgressListenerFather withSyncOnMediaDone(final Object sync) {
        mediaDoneSync = sync;
        return this;
    }

    public ProgressListenerFather withSyncOnMediaPause(final Object sync) {
        mediaPauseSync = sync;
        return this;
    }

    public ProgressListenerFather withSync(final Object sync) {
        errorSync = sync;
        startSync = sync;
        stopSync  = sync;
        progressSync = sync;
        mediaDoneSync = sync;
        mediaPauseSync = sync;
        return this;
    }

    public IProgressListener construct() {
        doAnswer(new NotifyAnswer(errorSync)).when(listener).onError(any(Exception.class));
        doAnswer(new NotifyAnswer(startSync)).when(listener).onMediaStart();
        doAnswer(new NotifyAnswer(stopSync)).when(listener).onMediaStop();
        doAnswer(new NotifyAnswer(progressSync)).when(listener).onMediaProgress(anyFloat());
        doAnswer(new NotifyAnswer(mediaDoneSync)).when(listener).onMediaDone();
        doAnswer(new NotifyAnswer(mediaPauseSync)).when(listener).onMediaPause();

        return listener;
    }
}
