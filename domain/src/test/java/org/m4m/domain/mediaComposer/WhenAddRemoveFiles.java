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

import org.m4m.MediaFile;
import org.junit.Test;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WhenAddRemoveFiles extends MediaComposerTest {
    @Test
    public void canAddSingleFile() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("")
            .construct();

        assertEquals(1, mediaComposer.getSourceFiles().size());
    }

    @Test
    public void canAddTwoFiles() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("")
            .withSourceFile("")
            .construct();

        assertEquals(2, mediaComposer.getSourceFiles().size());
    }

    @Test
    public void canRemoveFirstFile() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("1.mp4")
            .withSourceFile("2.mp4")
            .construct();

        mediaComposer.removeSourceFile(mediaComposer.getSourceFiles().get(0));

        assertEquals(1, mediaComposer.getSourceFiles().size());
        assertEquals("2.mp4", mediaComposer.getSourceFiles().get(0).getFilePath());
    }

    @Test
    public void canRemoveLastFile() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("1.mp4")
            .withSourceFile("2.mp4")
            .construct();

        mediaComposer.removeSourceFile(mediaComposer.getSourceFiles().get(1));

        assertEquals(1, mediaComposer.getSourceFiles().size());
        assertEquals("1.mp4", mediaComposer.getSourceFiles().get(0).getFilePath());
    }

    @Test
    public void canRemoveFileInTheMiddle() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("1.mp4")
            .withSourceFile("2.mp4")
            .withSourceFile("3.mp4")
            .construct();

        MediaFile fileInTheMiddle = mediaComposer.getSourceFiles().get(1);
        mediaComposer.removeSourceFile(fileInTheMiddle);

        assertEquals(2, mediaComposer.getSourceFiles().size());
        assertEquals("1.mp4", mediaComposer.getSourceFiles().get(0).getFilePath());
        assertEquals("3.mp4", mediaComposer.getSourceFiles().get(1).getFilePath());
    }

    @Test
    public void canInsertFileInTheEnd() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("1.mp4")
            .construct();

        mediaComposer.insertSourceFile(1, "2.mp4");

        assertEquals(2, mediaComposer.getSourceFiles().size());
        assertEquals("2.mp4", mediaComposer.getSourceFiles().get(1).getFilePath());
    }

    @Test
    public void canInsertFileInTheBeginning() throws IOException, OperationNotSupportedException {
        mediaComposer = create.mediaComposer()
            .withSourceFile("last.mp4")
            .construct();

        mediaComposer.insertSourceFile(0, "first.mp4");

        assertEquals(2, mediaComposer.getSourceFiles().size());
        assertEquals("first.mp4", mediaComposer.getSourceFiles().get(0).getFilePath());
    }
}


