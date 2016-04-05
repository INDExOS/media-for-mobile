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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import org.m4m.StreamingParameters;
import org.m4m.samples.GameRenderer;
import org.m4m.samples.R;

import java.util.ArrayList;
import java.util.List;

public class GameStreamingSettingsPopup extends Popup {

    private Context context;
    private Spinner renderingMethodList;

    public interface GameStreamingSettings {
        public void onRenderMethodChanged(GameRenderer.RenderingMethod method);
        public void onStreamingParamsChanged(StreamingParameters parameters);
    }

    GameStreamingSettings eventsListener;

    public GameStreamingSettingsPopup(Context context) {
        super(context);
        this.context = context;

        LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(R.layout.popup_game_streaming_settings, null));

        fillRenderMethodsList();
    }

    public void setEventListener(GameStreamingSettings listener) {
        eventsListener = listener;
    }

    private void fillRenderMethodsList() {
        renderingMethodList = (Spinner)getContentView().findViewById(R.id.renderMethod);

        List<String> list = new ArrayList<String>();

        list.add("Render Twice");
        list.add("Using Frame Buffer");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        renderingMethodList.setAdapter(dataAdapter);
    }

    @Override
    public void onDismiss() {
        int method = renderingMethodList.getSelectedItemPosition();

        GameRenderer.RenderingMethod renderingMethod;

        if (method == 0) {
            renderingMethod = GameRenderer.RenderingMethod.RenderTwice;
        } else {
            renderingMethod = GameRenderer.RenderingMethod.FrameBuffer;
        }

        eventsListener.onRenderMethodChanged(renderingMethod);

        StreamingParameters parameters = new StreamingParameters();

        parameters.Host = ((EditText)getContentView().findViewById(R.id.host)).getText().toString();
        parameters.Port = Integer.parseInt(((EditText)getContentView().findViewById(R.id.port)).getText().toString());
        parameters.ApplicationName = ((EditText)getContentView().findViewById(R.id.applicationName)).getText().toString();
        parameters.StreamName = ((EditText)getContentView().findViewById(R.id.streamName)).getText().toString();

        parameters.isToPublishAudio = false;
        parameters.isToPublishVideo = true;

        eventsListener.onStreamingParamsChanged(parameters);
    }

    public void setSettings(GameRenderer.RenderingMethod method, StreamingParameters params) {

        if (method == GameRenderer.RenderingMethod.RenderTwice) {
            renderingMethodList.setSelection(0);
        } else {
            renderingMethodList.setSelection(1);
        }

        ((EditText)getContentView().findViewById(R.id.host)).setText(params.Host);
        ((EditText)getContentView().findViewById(R.id.port)).setText(String.valueOf(params.Port));
        ((EditText)getContentView().findViewById(R.id.applicationName)).setText(params.ApplicationName);
        ((EditText)getContentView().findViewById(R.id.streamName)).setText(params.StreamName);
    }
}
