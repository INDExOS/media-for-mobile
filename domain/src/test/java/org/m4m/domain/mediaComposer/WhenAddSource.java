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

import org.m4m.MediaComposer;
import org.m4m.MediaFile;
import org.m4m.Uri;
import org.junit.Test;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WhenAddSource extends MediaComposerTest  {
    @Test
    public void canAddSingleSourceByFileName() throws IOException, InterruptedException {
        AndroidMediaObjectFactoryFake androidMediaObjectFactoryFake = new AndroidMediaObjectFactoryFake(create);
        mediaComposer = new MediaComposer(androidMediaObjectFactoryFake, progressListener);

        mediaComposer.addSourceFile("");

        List<MediaFile> sourceFiles = mediaComposer.getSourceFiles();
        assertEquals(1, sourceFiles.size());

        assertEquals("", sourceFiles.get(0).getFilePath());
        assertEquals(null, sourceFiles.get(0).getFileDescriptor());
        assertEquals(null, sourceFiles.get(0).getUri());
    }

    @Test
    public void canAddSingleSourceByFileDescriptor() throws IOException, InterruptedException {
        AndroidMediaObjectFactoryFake androidMediaObjectFactoryFake = new AndroidMediaObjectFactoryFake(create);
        mediaComposer = new MediaComposer(androidMediaObjectFactoryFake, progressListener);

        FileDescriptor fileDescriptor = new FileDescriptor();

        mediaComposer.addSourceFile(fileDescriptor);

        List<MediaFile> sourceFiles = mediaComposer.getSourceFiles();
        assertEquals(1, sourceFiles.size());

        assertEquals(fileDescriptor, sourceFiles.get(0).getFileDescriptor());
        assertEquals(null, sourceFiles.get(0).getFilePath());
        assertEquals(null, sourceFiles.get(0).getUri());
    }

    @Test
    public void canAddSingleSourceByUri() throws IOException, InterruptedException {
        AndroidMediaObjectFactoryFake androidMediaObjectFactoryFake = new AndroidMediaObjectFactoryFake(create);
        mediaComposer = new MediaComposer(androidMediaObjectFactoryFake, progressListener);

        File file = new File("1.mp4");
        Uri uri = new Uri(file.getPath());

        mediaComposer.addSourceFile(uri);

        List<MediaFile> sourceFiles = mediaComposer.getSourceFiles();
        assertEquals(1, sourceFiles.size());

        assertEquals("1.mp4", sourceFiles.get(0).getUri().getString());
        assertEquals(null, sourceFiles.get(0).getFilePath());
        assertEquals(null, sourceFiles.get(0).getFileDescriptor());
    }
}
