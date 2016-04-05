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

import org.m4m.domain.ISurface;
import org.m4m.domain.ISurfaceWrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurfaceFather {
    ISurfaceWrapper container = null;

    public SurfaceFather with(ISurfaceWrapper container) {
        this.container = container;
        return this;
    }

    public ISurface construct() {
        ISurface surface = mock(ISurface.class);
        if (container != null) {
            when(surface.getCleanObject()).thenReturn(container);
        }
        return surface;
    }
}
