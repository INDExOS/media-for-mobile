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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableSamplesListAdapter  extends BaseExpandableListAdapter {

    private Context context;

    private SampleGroup[] sampleGroups = {
            SampleGroup.VIDEO,
            SampleGroup.CAPTURING,
    };

    public ExpandableSamplesListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public SampleItem getChild(int group, int child) {
        return sampleGroups[group].samples[child];
    }

    @Override
    public long getChildId(int group, int child) {
        return group;
    }

    @Override
    public int getChildrenCount(int group) {
        return sampleGroups[group].samples.length;
    }

    @Override
    public View getChildView(int group, int child, boolean isLastChild, View convertView, ViewGroup parent) {
        final View childView;

        if (convertView != null) {
            childView = convertView;
        }
        else {
            childView = LayoutInflater.from(context).inflate(R.layout.sample_list_item, null);
        }

        ((TextView)childView.findViewById(R.id.itemTitle)).setText(context.getResources().getString(this.getChild(group, child).titleId));

        return childView;
    }

    @Override
    public View getGroupView(int group, boolean isExpanded, View convertView, ViewGroup parent) {
        final View groupView;

        if (convertView != null) {
            groupView = convertView;
        }
        else {
            groupView = LayoutInflater.from(context).inflate(R.layout.sample_list_group, null);
        }

        ((TextView)groupView.findViewById(R.id.itemTitle)).setText(context.getResources().getString(getGroup(group).titleId));

        return groupView;
    }

    @Override
    public SampleGroup getGroup(int group) {
        return sampleGroups[group];
    }

    @Override
    public int getGroupCount() {
        return sampleGroups.length;
    }

    @Override
    public long getGroupId(int group) {
        return group;
    }

    @Override
    public boolean isChildSelectable(final int pGroupPosition, final int pChildPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public enum SampleItem
    {
        TRANSCODE_VIDEO(R.string.transcode_video, ComposerTranscodeActivity.class.getName()),
        JOIN_VIDEO(R.string.join_video, ComposerJoinActivity.class.getName()),
        CUT_VIDEO(R.string.cut_video, ComposerCutActivity.class.getName()),
        VIDEO_EFFECT(R.string.video_effect, ComposerVideoEffectActivity.class.getName()),
        AUDIO_EFFECT(R.string.audio_effect, ComposerAudioEffectActivity.class.getName()),
        MEDIA_INFO(R.string.media_file_info, ComposerMediaFileInfoActivity.class.getName()),
        TIME_SCALING(R.string.time_scaling, ComposerTimeScalingActivity.class.getName()),

        CAMERA_CAPTURING(R.string.camera_capturing, CameraCapturerActivity.class.getName()),
        GAME_CAPTURING(R.string.game_capturing, GameCapturing.class.getName());

        public String className;
        public int titleId;

        private SampleItem(int titleId, String className) {
            this.className = className;
            this.titleId = titleId;
        }
    }

    public enum SampleGroup
    {
        VIDEO(R.string.video,
                SampleItem.TRANSCODE_VIDEO,
                SampleItem.JOIN_VIDEO,
                SampleItem.CUT_VIDEO,
                SampleItem.VIDEO_EFFECT,
                SampleItem.AUDIO_EFFECT,
                SampleItem.MEDIA_INFO,
                SampleItem.TIME_SCALING),

        CAPTURING(R.string.capturing,
                SampleItem.CAMERA_CAPTURING,
                SampleItem.GAME_CAPTURING);

        public int titleId;
        public SampleItem[] samples;

        private SampleGroup(int titleId, SampleItem ... samples) {
            this.titleId = titleId;
            this.samples = samples;
        }
    }
}
