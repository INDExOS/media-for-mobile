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

package org.m4m.samples;

import android.os.Bundle;
import android.widget.TextView;
import org.m4m.MediaComposer;
import org.m4m.MediaFileInfo;
import org.m4m.android.AndroidMediaObjectFactory;

import org.m4m.Uri;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ComposerJoinCoreActivity extends ComposerTranscodeCoreActivity {

    @Override
    protected void getActivityInputs() {
        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        srcMediaName2 = b.getString("srcMediaName2");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new Uri(b.getString("srcUri1"));
        mediaUri2 = new Uri(b.getString("srcUri2"));
    }

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        mediaComposer.addSourceFile(mediaUri2);
    }

    @Override
    protected void printDuration() {
        long duration1 = duration;
        long duration2 = 0;

        try {
            MediaFileInfo mediaFileInfo = new MediaFileInfo(new AndroidMediaObjectFactory(getApplicationContext()));
            mediaFileInfo.setUri(mediaUri2);
            duration2 = mediaFileInfo.getDurationInMicroSec();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView v = (TextView) findViewById(R.id.durationInfo);
        v.setText(String.format("durationSrc1 = %d sec\n", TimeUnit.MICROSECONDS.toSeconds(duration1)));
        v.append(String.format("durationSrc2 = %d sec\n", TimeUnit.MICROSECONDS.toSeconds(duration2)));
    }

    @Override
    protected void getDstDuration() {
        try {
            mediaFileInfo = new MediaFileInfo(new AndroidMediaObjectFactory(getApplicationContext()));
            mediaFileInfo.setUri(mediaUri2);

            duration += mediaFileInfo.getDurationInMicroSec();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void printPaths() {
        pathInfo.setText(String.format("srcMediaFileName1 = %s\nsrcMediaFileName2 = %s\ndstMediaPath = %s\n", srcMediaName1, srcMediaName2, dstMediaPath));
    }
}


