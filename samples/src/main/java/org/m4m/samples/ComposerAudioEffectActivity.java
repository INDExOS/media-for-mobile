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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.m4m.samples.controls.TimelineItem;

public class ComposerAudioEffectActivity extends ActivityWithTimeline implements View.OnClickListener {
    TimelineItem item1 = null;
    TimelineItem item2 = null;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.composer_join_activity);

        ((Button)findViewById(R.id.action)).setText("Apply Audio Effect");

        init();
    }

    private void init() {
        item1 = (TimelineItem) findViewById(R.id.timelineItem1);
        item1.setEventsListener(this);
        item1.enableSegmentPicker(false);

        item2 = (TimelineItem) findViewById(R.id.timelineItem2);
        item2.setEventsListener(this);
        item2.enableSegmentPicker(false);

        findViewById(R.id.action).setOnClickListener(this);
    }

    public void action() {
        String mediaFileName1 = item1.getMediaFileName();
        String mediaFileName2 = item2.getMediaFileName();

        if (mediaFileName1 == null || mediaFileName2 == null) {
            showToast("Please select valid video files first");

            return;
        }

        item1.stopVideoView();
        item2.stopVideoView();

        Intent intent = new Intent();
        intent.setClass(this, ComposerAudioEffectCoreActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("srcMediaName1", item1.getMediaFileName());
        intent.putExtras(bundle);
        bundle.putString("srcMediaName2", item2.getMediaFileName());
        intent.putExtras(bundle);
        bundle.putString("dstMediaPath", item1.genDstPath(item1.getMediaFileName(), "audio_effect"));
        intent.putExtras(bundle);
        bundle.putString("srcUri1", item1.getUri().getString());
        intent.putExtras(bundle);
        bundle.putString("srcUri2", item2.getUri().getString());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (item1 != null) {
            item1.updateView();
        }

        if (item2 != null) {
            item2.updateView();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.action: {
                action();
            }
            break;
        }
    }
}
