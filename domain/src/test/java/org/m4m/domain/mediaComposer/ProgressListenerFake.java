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

import java.util.ArrayList;
import java.util.List;

public class ProgressListenerFake implements IProgressListener {
    private ArrayList<Float> progresses = new ArrayList<Float>();
    private boolean started = false;
    private boolean done = false;
    private Exception exception;

    @Override
    public void onMediaStart() {
        started = true;
    }

    @Override
    public void onMediaProgress(float progress) {
        progresses.add(progress);
    }

    @Override
    public void onMediaDone() {
        done = true;
    }

    @Override
    public void onMediaPause() {

    }

    @Override
    public void onMediaStop() {

    }

    @Override
    public void onError(Exception exception) {
        done = true;
        this.exception = exception;
    }

    public List<Float> getProgress() {
        return progresses;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDone() {
        return done;
    }

    public Exception getException() { return exception; }
}
