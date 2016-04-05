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
import android.view.LayoutInflater;
import android.widget.EditText;
import org.m4m.StreamingParameters;
import org.m4m.samples.R;

public class StreamingSettingsPopup extends Popup {

    private Context context;

    public interface CameraStreamingSettings {
        public void onStreamingParamsChanged(StreamingParameters parameters);
    }

    CameraStreamingSettings eventsListener;

    public StreamingSettingsPopup(Context context) {
        super(context);
        this.context = context;

        LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(R.layout.popup_streaming_settings, null));
    }

    public void setEventListener(CameraStreamingSettings listener) {
        eventsListener = listener;
    }

    @Override
    public void onDismiss() {
        StreamingParameters parameters = new StreamingParameters();

        parameters.Host = ((EditText)getContentView().findViewById(R.id.host)).getText().toString();
        parameters.Port = Integer.parseInt(((EditText)getContentView().findViewById(R.id.port)).getText().toString());
        parameters.ApplicationName = ((EditText)getContentView().findViewById(R.id.applicationName)).getText().toString();
        parameters.StreamName = ((EditText)getContentView().findViewById(R.id.streamName)).getText().toString();

        parameters.isToPublishAudio = false;
        parameters.isToPublishVideo = true;

        eventsListener.onStreamingParamsChanged(parameters);
    }

    public void setSettings(StreamingParameters params) {
        ((EditText)getContentView().findViewById(R.id.host)).setText(params.Host);
        ((EditText)getContentView().findViewById(R.id.port)).setText(String.valueOf(params.Port));
        ((EditText)getContentView().findViewById(R.id.applicationName)).setText(params.ApplicationName);
        ((EditText)getContentView().findViewById(R.id.streamName)).setText(params.StreamName);
    }
}
