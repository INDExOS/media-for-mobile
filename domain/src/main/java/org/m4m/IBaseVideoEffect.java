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

import org.m4m.domain.FileSegment;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.TextureRenderer;

public interface IBaseVideoEffect  {
    /**
     * Sets the segment of the target stream for which the effect is to be applied.
     *
     * @param segment Pair of time stamps in nanoseconds that designate the beginning and the end of the segment.
     */
    void setSegment(FileSegment segment);

    /**
     * Gets the segment of the target stream for which the effect is to be applied.
     *
     * @return Segment for which the effect is to be applied. Value (NULL, NULL) is valid and means the entire stream.
     */
    FileSegment getSegment();

    /**
     * Performs internal initialization. Creates required internal components and allocates buffers if necessary.
     * Called from GL thread.
     */
    void start();

    /**
     * Sets input resolution. Notifies the effect about input resolution change.
     * Called by the pipeline manager
     *
     * @param resolution Resolution of surfaces for which the effect is to be applied.
     */
    void setInputResolution(Resolution resolution);

    void setFillMode(TextureRenderer.FillMode fillMode);

    public TextureRenderer.FillMode getFillMode();
}
