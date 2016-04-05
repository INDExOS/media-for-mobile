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

import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import org.m4m.domain.Resolution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ComposerMediaFileInfoCoreActivity extends ComposerTranscodeCoreActivity implements SurfaceHolder.Callback {

    private TextView sliderPositionTextView;
    private long sliderPosition = 0;
    private LinearLayout layout;
    final private String noInfo = "No info";

    private int viewPosition = 0;

    @Override
    protected void setupUI() {
        buttonStart.setVisibility(View.GONE);
        buttonStop.setVisibility(View.GONE);

        findViewById(R.id.transcodeParametersTable).setVisibility(View.GONE);
        findViewById(R.id.transcodeParametersHeader).setVisibility(View.GONE);

        sliderPositionTextView = (TextView) findViewById(R.id.sliderPosition);
        sliderPositionTextView.setVisibility(View.VISIBLE);

        SeekBar seekBar = new SeekBar(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8, 8, 8, 0);
        seekBar.setLayoutParams(lp);
        seekBar.setProgress(0);
        seekBar.setMax(100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                try {
                    // call preview method right here
                    sliderPosition = (long) ((double) progressChanged / (100.0) * (double) duration);
                    sliderPositionTextView.setText(String.format("slider position = %.1f sec", (double) sliderPosition / 10e5));

                    ByteBuffer buffer = ByteBuffer.allocate(1);
                    mediaFileInfo.getFrameAtPosition(sliderPosition, buffer);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        layout = (LinearLayout) findViewById(R.id.linearLayout);
        layout.addView(seekBar, 0);
    }

    protected void printFileInfo() {

        pathInfo = (TextView) findViewById(R.id.pathInfo);
        pathInfo.append(String.format("mediaFileName = %s\n", srcMediaName1));

        durationInfo = (TextView) findViewById(R.id.durationInfo);
        durationInfo.setText(String.format("duration = %d sec", TimeUnit.MICROSECONDS.toSeconds(duration)));

        sliderPositionTextView.setText(String.format("slider position = %.1f sec", (double) sliderPosition / 1e6));

        if(videoFormat != null) { printVideoInfo(); }
        if(audioFormat != null) { printAudioInfo(); }
    }

    protected void printVideoInfo() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        TableLayout table = (TableLayout) inflater.inflate(R.layout.media_file_video_info_table, null);
        ((LinearLayout) findViewById(R.id.transcodeParametersLayout)).addView(table, viewPosition++);

        String videoCodec = noInfo;
        String videoMimeType = noInfo;
        String videoFrameRate = noInfo;
        String videoIFrameInterval = noInfo;
        String videoBitRateInKBytes = noInfo;

        String videoWidth = noInfo;
        String videoHeight = noInfo;

        //video format
        try {
            videoCodec = videoFormat.getVideoCodec();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.video_info_codec)).setText(videoCodec);

        try {
            videoMimeType = videoFormat.getMimeType();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.video_info_mime_type)).setText(videoMimeType);

        try {
            Resolution resolution = videoFormat.getVideoFrameSize();
            videoWidth = String.format(Locale.getDefault(), "%d", resolution.width());
            videoHeight = String.format(Locale.getDefault(), "%d", resolution.height());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.video_info_width)).setText(videoWidth);
        ((TextView) findViewById(R.id.video_info_height)).setText(videoHeight);


        try {
            videoFrameRate = String.format(Locale.getDefault(), "%d", videoFormat.getVideoFrameRate());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.video_info_frame_rate)).setText(String.valueOf(videoFrameRate));

        try {
            videoIFrameInterval = String.format(Locale.getDefault(), "%d", videoFormat.getVideoIFrameInterval());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.video_info_frame_interval)).setText(String.valueOf(videoIFrameInterval));

        try {
            videoBitRateInKBytes = String.format(Locale.getDefault(), "%d", videoFormat.getVideoBitRateInKBytes());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.video_info_bit_rate)).setText(String.valueOf(videoBitRateInKBytes));
    }

    protected void printAudioInfo() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        TableLayout table = (TableLayout) inflater.inflate(R.layout.media_file_audio_info_table, null);
        ((LinearLayout) findViewById(R.id.transcodeParametersLayout)).addView(table, viewPosition++);

        String audioCodec = noInfo;
        String audioProfile = noInfo;
        String audioMimeType = noInfo;
        String audioChannelCount = noInfo;
        String audioBitrateInBytes = noInfo;
        String audioSampleRateHz = noInfo;

        //audio format
        try {
            audioCodec = String.format(Locale.getDefault(), "%s", audioFormat.getAudioCodec());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.audio_info_audio_codec)).setText(String.valueOf(audioCodec));

        try {
            audioProfile = String.format(Locale.getDefault(), "%d", audioFormat.getAudioProfile());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.audio_info_audio_profile)).setText(String.valueOf(audioProfile));

        try {
            audioMimeType = String.format(Locale.getDefault(), "%s", audioFormat.getMimeType());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.audio_info_mime_type)).setText(String.valueOf(audioMimeType));

        try {
            audioChannelCount = String.format(Locale.getDefault(), "%d", audioFormat.getAudioChannelCount());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.audio_info_channel_count)).setText(String.valueOf(audioChannelCount));

        try {
            audioSampleRateHz = String.format(Locale.getDefault(), "%d", audioFormat.getAudioSampleRateInHz());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.audio_info_sample_rate)).setText(String.valueOf(audioSampleRateHz));

        try {
            audioBitrateInBytes = String.format(Locale.getDefault(), "%d", audioFormat.getAudioBitrateInBytes());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.audio_info_bit_rate)).setText(String.valueOf(audioBitrateInBytes));
    }
}
