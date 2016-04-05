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

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class SamplesMainActivity extends ExpandableListActivity {
    private ExpandableSamplesListAdapter samplesListAdapter;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main);

        samplesListAdapter = new ExpandableSamplesListAdapter(this);

        setListAdapter(samplesListAdapter);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int group, int child, long id) {
        ExpandableSamplesListAdapter.SampleItem sample = samplesListAdapter.getChild(group, child);

        Intent intent = null;

        try {
            intent = new Intent(SamplesMainActivity.this, Class.forName(sample.className));
        } catch (ClassNotFoundException e) {
            showToast("Something went wrong...");
        }

        startActivity(intent);

        return super.onChildClick(parent, view, group, child, id);
    }

    private void showToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }
}
