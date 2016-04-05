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
package org.m4m.domain;

import java.io.IOException;

public class MicrophoneSource  implements  IMicrophoneSource{
    protected CommandQueue commandQueue = new CommandQueue();
    private boolean isStopped = true;

    @Override
    public void configure(int sampleRate, int channels) { }

    @Override
    public void pull(Frame frame) {
        if (!isStopped()) {

            // In case of AudioRecord reading ERRORS
			if(frame.getLength() < 0) {
                //frame.set(null, 0, 0, 0, 0, 0); // or Frame.EOF()?? EoF is better!
                frame.copyInfoFrom(Frame.EOF());
                frame.copyDataFrom(Frame.EOF());
                commandQueue.queue(Command.EndOfFile, 0);
                return;
            }

            // Workaround: When attempting to stream second time
            // MicrophoneSource sends frame with EOF flag
            if(frame.equals(Frame.EOF())) {
                frame.setFlags(0);
            }
            commandQueue.queue(Command.HasData, 0);
        }
        else {
            frame.copyInfoFrom(Frame.EOF());
            frame.copyDataFrom(Frame.EOF());
            commandQueue.queue(Command.EndOfFile, 0);
        }
    }

    @Override
    public MediaFormat getMediaFormatByType(MediaFormatType mediaFormatType) {
        return null;
    }

    @Override
    public boolean isLastFile() {
        return true;
    }

    @Override
    public void incrementConnectedPluginsCount() { }

    @Override
    public void close() throws IOException { }

    @Override
    public boolean canConnectFirst(IInputRaw connector) {
        return true;
    }

    @Override
    public CommandQueue getOutputCommandQueue() {
        return commandQueue;
    }

    @Override
    public void fillCommandQueues() { }

    @Override
    public void start() {
        commandQueue.queue(Command.HasData, 0);
        isStopped = false;
    }

    @Override
    public void stop() {
        // Sending HasData command in order to pull EoF frame next
        commandQueue.queue(Command.HasData, 0);
        isStopped = true;
    }

    public boolean isStopped() { return isStopped; }
}
