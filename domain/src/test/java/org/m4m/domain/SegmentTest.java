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

import org.junit.Test;

import java.util.ArrayList;

public class SegmentTest extends TestBase {
    @Test
    public void add_AlignsLeftOverlappingSegments() {
        Segments segments = new Segments(new ArrayList<Pair<Long, Long>>());

        segments.add(new Pair<Long, Long>(10L, 30L));
        segments.add(new Pair<Long, Long>(20L, 40L));

        assertThat(segments).equalsTo(
            new Pair<Long, Long>(10L, 30L),
            new Pair<Long, Long>(30L, 40L));
    }

    @Test
    public void add_AlignsRightOverlappingSegments() {
        Segments segments = new Segments(new ArrayList<Pair<Long, Long>>());

        segments.add(new Pair<Long, Long>(10L, 30L));
        segments.add(new Pair<Long, Long>(0L, 20L));

        assertThat(segments).equalsTo(
            new Pair<Long, Long>(10L, 30L),
            new Pair<Long, Long>(0L, 10L));
    }

    @Test
    public void add_SkipsSegmentInsideExistingSegment() {
        Segments segments = new Segments(new ArrayList<Pair<Long, Long>>());

        segments.add(new Pair<Long, Long>(10L, 50L));
        segments.add(new Pair<Long, Long>(20L, 30L));

        assertThat(segments).equalsTo(
            new Pair<Long, Long>(10L, 50L));
    }

    @Test
    public void add_ReplacesCoveringSegment() {
        Segments segments = new Segments(new ArrayList<Pair<Long, Long>>());

        segments.add(new Pair<Long, Long>(20L, 30L));
        segments.add(new Pair<Long, Long>(10L, 50L));

        assertThat(segments).equalsTo(
            new Pair<Long, Long>(10L, 50L));
    }
}
