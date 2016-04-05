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

package org.m4m;

/**
 * Listener that notifies when composer changes state
 */
public interface IProgressListener {
    /**
     * Called to notify that composing started
     */
    public void onMediaStart();

    /**
     * Called to notify about current composing progress
     *
     * @param progress current progress as number [0, 1]
     */
    public void onMediaProgress(float progress);

    /**
     * Called to notify that composing finished
     */
    public void onMediaDone();

    /**
     * Called to notify that composing paused
     */
    public void onMediaPause();

    /**
     * Called to notify that composing stopped
     */
    public void onMediaStop();

    /**
     * Called to notify that composing experienced some error
     *
     * @param exception
     */
    public void onError(Exception exception);
}
