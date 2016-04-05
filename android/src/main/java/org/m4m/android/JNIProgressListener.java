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
package org.m4m.android;

import org.m4m.IProgressListener;

/*should be instantiated by c++ JNI only*/
public class JNIProgressListener implements IProgressListener {
    long TAG;
    public JNIProgressListener(long TAG) {
        this.TAG = TAG;
    }

    @Override
    public void onMediaStart() {
        onMediaStartJNI(TAG);
    }

    @Override
    public void onMediaProgress(float progress) {
        onMediaProgressJNI(TAG, progress);
    }

    @Override
    public void onMediaDone() {
       onMediaDoneJNI(TAG);
    }

    @Override
    public void onMediaPause() {
        onMediaPauseJNI(TAG);
    }

    @Override
    public void onMediaStop() {
        onMediaStopJNI(TAG);
    }

    @Override
    public void onError(Exception exception) {
        onErrorJNI(TAG, exception.toString());
    }

    private native void onMediaStartJNI(long thisListener);
    private native void onMediaProgressJNI(long thisListener, float progress);
    private native void onMediaDoneJNI(long thisListener);
    private native void onMediaPauseJNI(long thisListener);
    private native void onMediaStopJNI(long thisListener);
    private native void onErrorJNI(long thisListener, String exception);
}
