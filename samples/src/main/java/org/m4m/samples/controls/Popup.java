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
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.PopupWindow;
import org.m4m.samples.R;

public class Popup extends PopupWindow implements OnTouchListener, PopupWindow.OnDismissListener {
    protected Context context;
    protected View contentView;

    public Popup(Context context) {
        super(context);

        this.context = context;

        init();
    }

    public void show(View anchor, boolean center) {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int contentWidth = contentView.getMeasuredWidth();
        int contentHeight = contentView.getMeasuredHeight();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int xPos = 0;
        int yPos = 0;

        boolean onTop = true;
        boolean onLeft = true;

        int gravity = Gravity.NO_GRAVITY;

        if (center) {
            xPos = 0;
            yPos = 0;

            gravity = Gravity.CENTER;
        } else {
            int[] location = new int[2];

            anchor.getLocationOnScreen(location);

            Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

            if ((anchorRect.left + contentWidth) > screenWidth) {
                if (anchorRect.right - contentWidth < 0) {
                    xPos = screenWidth - contentWidth - 4;
                } else {
                    xPos = anchorRect.right - contentWidth;
                }

                onLeft = false;
            } else {
                xPos = anchorRect.left;
                onLeft = true;
            }

            if (anchorRect.top < screenHeight / 2) {
                yPos = anchorRect.bottom + 4;
                onTop = true;
            } else {
                yPos = anchorRect.top - contentHeight - 4;
                onTop = false;
            }

            if (onTop) {
                if (onLeft) {
                    setAnimationStyle(R.style.Popups_Down_Left);
                } else {
                    setAnimationStyle(R.style.Popups_Down_Right);
                }
            } else {
                if (onLeft) {
                    setAnimationStyle(R.style.Popups_Up_Left);
                } else {
                    setAnimationStyle(R.style.Popups_Up_Right);
                }
            }
        }

        setOnDismissListener(this);

        onShow();

        showAtLocation(anchor, gravity, xPos, yPos);
    }

    public void hide() {
        dismiss();
    }

    private void init() {
        setTouchInterceptor(this);

        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_popup));
    }

    public void setContentView(View view) {
        super.setContentView(view);

        contentView = view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            dismiss();

            return true;
        }

        return false;
    }

    public void onShow() {
    }

    @Override
    public void onDismiss() {

    }
}
