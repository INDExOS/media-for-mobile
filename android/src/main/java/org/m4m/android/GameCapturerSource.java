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

import org.m4m.domain.CaptureSource;
import org.m4m.domain.Command;
import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceListener;

public class GameCapturerSource extends CaptureSource {

    ISurface renderingSurface = null;
    private boolean swapBuffers = true;
    private EglContextSwitcher contextSwitcher;
    private ISurfaceListener listener;

    public GameCapturerSource() {
        contextSwitcher = new EglContextSwitcher();
    }

    @Override
    public void addSetSurfaceListener(ISurfaceListener listenMe) {
        listener = listenMe;
    }

    //trying to avoid swapbuffers happen in different thread
    @Override
    public ISurface getSurface() {
        return renderingSurface;
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        contextSwitcher.init(width, height);
        contextSwitcher.saveEglState();

        if(listener != null) {
            listener.onSurfaceAvailable(null);
        }

        renderingSurface.makeCurrent();
        contextSwitcher.restoreEglState();
    }

    @Override
    public void beginCaptureFrame() {
        if (renderingSurface == null) {
            return;
        }

        contextSwitcher.saveEglState();

        renderingSurface.makeCurrent();
        renderingSurface.setProjectionMatrix(contextSwitcher.getProjectionMatrix());
        renderingSurface.setViewport();
    }

    @Override
    public void endCaptureFrame() {
        super.endCaptureFrame();

        final long presentationTimeUs = (System.nanoTime() - startTime);
        renderingSurface.setPresentationTime(presentationTimeUs);

        if (swapBuffers) {
            renderingSurface.swapBuffers();
        }

        contextSwitcher.restoreEglState();
        commandQueue.queue(Command.HasData, 0);
    }

    @Override
    public void setOutputSurface(ISurface surface) {
        renderingSurface = surface;
    }
}
