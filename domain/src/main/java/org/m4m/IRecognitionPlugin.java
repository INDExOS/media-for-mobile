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

import org.m4m.domain.Frame;
import org.m4m.domain.MediaFormat;

/**
 * This interface is defined for recognition plug-ins, which could be embedded into
 * pipelines to extract metadata obtained by analyzing received content.
 */
public interface IRecognitionPlugin {
    /**
     * Base class for output data received from recognition plug-in.
     */
    public class RecognitionOutput {
    }

    /**
     * Base class for input data supplied to recognition plug-in.
     */
    public class RecognitionInput {
        private MediaFormat mediaFormat;
        private Frame frame;

        /**
         * Sets media format of the content.
         *
         * @param mediaFormat
         */
        public void setMediaFormat(MediaFormat mediaFormat) {
            this.mediaFormat = mediaFormat;
        }

        /**
         * Gets media format of the content.
         *
         * @return Media format.
         */
        public MediaFormat getMediaFormat() {
            return mediaFormat;
        }

        /**
         * Sets a frame.
         *
         * @param frame
         */
        public void setFrame(Frame frame) {
            this.frame = frame;
        }

        /**
         * Gets a frame.
         *
         * @return Frame.
         */
        public Frame getFrame() {
            return frame;
        }
    }

    /**
     * Interface for signaling upon content recognition status.
     */
    public interface RecognitionEvent {
        /**
         * Called to notify that content recognition is done.
         */
        public void onContentRecognized(IRecognitionPlugin plugin, RecognitionOutput output);
    }

    /**
     * Starts recognition plug-in.
     */
    public void start();

    /**
     * Stops recognition plug-in.
     */
    public void stop();

    /**
     * Performs content recognition.
     *
     * @param input Data for content recognition.
     * @return Content recognition output.
     */
    public RecognitionOutput recognize(RecognitionInput input);
}
