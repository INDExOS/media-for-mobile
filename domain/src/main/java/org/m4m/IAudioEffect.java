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

import org.m4m.domain.Pair;

import org.m4m.domain.MediaFormat;

import java.nio.ByteBuffer;


/**
 * Use this simple interface for implementing audio effects for MediaComposer pipeline.
 */
public interface IAudioEffect {

    /**
     * Sets the segment of the target stream for which the effect is to be applied.
     *
     * @param segment Pair of time stamps in nanoseconds that designate the beginning and the end of the segment.
     */
    void setSegment(Pair<Long, Long> segment);

    /**
     * Gets the segment of the target stream for which the effect is to be applied.
     *
     * @return Segment for which the effect is to be applied. Value (NULL, NULL) is valid and means the entire stream.
     */
    Pair<Long, Long> getSegment();

    /**
     * Applies effect. Main function of the effect object.
     * Gets called for each frame for which the affect is to be applied.
     *
     * @param input        Input data.
     * @param timeProgress Time in nanoseconds of applying the effect in the target stream.
     */
    void applyEffect(ByteBuffer input, long timeProgress);

    /**
     * Gets media format.
     *
     * @return Media format.
     */
    MediaFormat getMediaFormat();
}
