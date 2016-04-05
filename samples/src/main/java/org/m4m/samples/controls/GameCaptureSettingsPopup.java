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
import android.widget.Spinner;

import org.m4m.samples.GameRenderer;
import org.m4m.samples.R;

import java.util.ArrayList;
import java.util.List;

public class GameCaptureSettingsPopup extends Popup {

    private Context context;
    private Spinner renderingMethodList;

    public interface GameCaptureSettings {
        public void onRenderMethodChanged(GameRenderer.RenderingMethod method);
    }

    GameCaptureSettings eventsListener;

    public GameCaptureSettingsPopup(Context context) {
        super(context);
        this.context = context;

        LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(R.layout.popup_game_capture_settings, null));

        fillRenderMethodsList();
    }

    public void setEventListener(GameCaptureSettings listener) {
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

    public void setSettings(GameRenderer.RenderingMethod method) {

        if (method == GameRenderer.RenderingMethod.RenderTwice) {
            renderingMethodList.setSelection(0);
        } else {
            renderingMethodList.setSelection(1);
        }
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
    }
}
