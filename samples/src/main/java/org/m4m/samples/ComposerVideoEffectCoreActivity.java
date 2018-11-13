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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import org.m4m.IVideoEffect;
import org.m4m.MediaComposer;
import org.m4m.Uri;
import org.m4m.domain.FileSegment;
import org.m4m.effects.GrayScaleEffect;
import org.m4m.effects.InverseEffect;
import org.m4m.effects.SepiaEffect;
import org.m4m.effects.TextOverlayEffect;

import java.io.IOException;


public class ComposerVideoEffectCoreActivity extends ComposerTranscodeCoreActivity {

    private int effectIndex;

    @Override
    protected void getActivityInputs() {

        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new Uri(b.getString("srcUri1"));

        effectIndex = b.getInt("effectIndex");
    }

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath, 0);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        configureVideoEffect(mediaComposer);
    }

    private void configureVideoEffect(MediaComposer mediaComposer) {
        IVideoEffect effect = null;

        switch (effectIndex) {
            case 0:
                effect = new SepiaEffect(0, factory.getEglUtil());
                break;
            case 1:
                effect = new GrayScaleEffect(0, factory.getEglUtil());
                break;
            case 2:
                effect = new InverseEffect(0, factory.getEglUtil());
                break;
            case 3:
                Bitmap b = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_dcard);
                effect = new TextOverlayEffect(0, factory.getEglUtil(), b);
                break;
            default:
                break;
        }

        if (effect != null) {
            effect.setSegment(new FileSegment(0l, 0l)); // Apply to the entire stream
            mediaComposer.addVideoEffect(effect);
        }
    }

    @Override
    protected void printEffectDetails() {
        effectDetails.append(String.format("Video effect = %s\n", getVideoEffectName(effectIndex)));
    }

    private String getVideoEffectName(int videoEffectIndex) {
        switch (videoEffectIndex) {
            case 0:
                return "Sepia";
            case 1:
                return "Grayscale";
            case 2:
                return "Inverse";
            case 3:
                return "Dcard Overlay";
            default:
                return "Unknown";
        }
    }
}


