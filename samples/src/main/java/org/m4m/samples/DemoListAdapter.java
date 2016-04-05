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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DemoListAdapter extends ArrayAdapter<DemoListItem> {

    private List<DemoListItem> mList;
    private Activity mContext;

    public DemoListAdapter(Activity context, List<DemoListItem> list) {
        super(context, R.layout.sample_list_item, list);

        mContext = context;
        mList = list;
    }

    static class ViewHolder {
        protected TextView mTitle;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        DemoListItem item = mList.get(position);

        View view = null;

        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();

            view = inflater.inflate(R.layout.sample_list_item, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.mTitle = (TextView) view.findViewById(R.id.itemTitle);

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.mTitle.setText(item.getTitle());

        return view;
    }

    public void updateDisplay() {
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
