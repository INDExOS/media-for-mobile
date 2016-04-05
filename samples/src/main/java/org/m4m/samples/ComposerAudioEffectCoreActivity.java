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

import android.media.MediaCodecInfo;

import org.m4m.MediaComposer;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.effects.SubstituteAudioEffect;

import java.io.IOException;

public class ComposerAudioEffectCoreActivity extends ComposerJoinCoreActivity {

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);

        AudioFormatAndroid audioFormat = new AudioFormatAndroid(audioMimeType, audioSampleRate, audioChannelCount);
        audioFormat.setKeyMaxInputSize(48 * 1024);
        audioFormat.setAudioBitrateInBytes(audioBitRate);
        audioFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaComposer.setTargetAudioFormat(audioFormat);


        SubstituteAudioEffect effect = new SubstituteAudioEffect();
        effect.setFileUri(this, mediaUri2, this.audioFormat);
        mediaComposer.addAudioEffect(effect);
    }
}
