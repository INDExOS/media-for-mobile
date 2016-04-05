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

package org.m4m.samples.controls;

import android.content.Context;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import org.m4m.samples.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraCaptureSettingsPopup extends Popup {

    private Context context;
    List<Camera.Size> supportedResolutions;

    public interface CameraCaptureSettings {
        public void displayResolutionChanged(int width, int height);

        public void videoResolutionChanged(int width, int height);

        public void audioRecordChanged(boolean bState);
    }

    CameraCaptureSettings eventsListener;

    public CameraCaptureSettingsPopup(Context context, List<Camera.Size> resolutions, CameraCaptureSettings listener) {
        super(context);

        supportedResolutions = resolutions;
        this.context = context;
        eventsListener = listener;

        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(R.layout.popup_camera_capture_settings, null));

        setupCameraResolutionsSpinner();
        setupOutputResolutionsSpinner();

        final CheckBox audioRecord = (CheckBox) getContentView().findViewById(R.id.recordAudio);
        audioRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventsListener.audioRecordChanged(audioRecord.isChecked());
            }
        });
    }

    private void setupCameraResolutionsSpinner() {
        Spinner spinner = (Spinner) getContentView().findViewById(R.id.cam_resolutions);
        List<String> spinner_data = new ArrayList();

        for (Camera.Size sz : supportedResolutions) {
            spinner_data.add(sz.width + " x " + sz.height);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, spinner_data);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

                eventsListener.displayResolutionChanged(supportedResolutions.get((int) id).width, supportedResolutions.get((int) id).height);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupOutputResolutionsSpinner() {
        Spinner spinner = (Spinner) getContentView().findViewById(R.id.encoded_resolutions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.frame_size_values, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            final Pattern pattern = Pattern.compile("(\\d*)\\s*[xX]\\s*(\\d*)");

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                String resolution = adapterView.getAdapter().getItem((int) id).toString();
                Matcher matcher = pattern.matcher(resolution);
                if (!matcher.find()) {
                    throw new RuntimeException("Invalid resolution string: " + resolution);
                }
                eventsListener.videoResolutionChanged(new Integer(matcher.group(1)), new Integer(matcher.group(2)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinner.setSelection(0);
    }
}
