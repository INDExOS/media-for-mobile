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
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import org.m4m.IProgressListener;

import org.m4m.samples.controls.GameCaptureSettingsPopup;
import org.m4m.samples.controls.GameGLSurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GameCapturing extends Activity implements GameCaptureSettingsPopup.GameCaptureSettings {
    public static final int UPDATE_FPS = 1;
    public static final int ENABLE_BUTTON = 2;
    public static final int UPDATE_TIMER = 3;

    GameRenderer.RenderingMethod renderingMethod;

    private GameGLSurfaceView surfaceView;
    protected GameRenderer gameRenderer;

    protected ImageButton captureButton;

    private TextView fps;
    private TextView time;

    private String fpsText;
    private String timeText;

    private Timer timer;
    private long startTime;

    protected String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator;
    protected String lastFileName;

    final Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_FPS: {
                    updateFps(msg.arg1);
                }
                break;

                case UPDATE_TIMER: {
                    updateTimer();
                }
                break;

                case ENABLE_BUTTON: {
                    findViewById(msg.arg1).setEnabled(true);
                }
                break;
            }
        }
    };

    public IProgressListener progressListener = new IProgressListener() {
        @Override
        public void onMediaStart() {
        }

        @Override
        public void onMediaProgress(float progress) {
        }

        @Override
        public void onMediaDone() {
        }

        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        if (configurationInfo.reqGlEsVersion < 0x20000) {
            showToast("This sample requires OpenGL ES 2.0");

            return;
        }

        setContentView(R.layout.game_capturing);

        renderingMethod = GameRenderer.RenderingMethod.FrameBuffer;

        surfaceView = (GameGLSurfaceView) findViewById(R.id.surfaceView);

        gameRenderer = new GameRenderer(getApplicationContext(), uiHandler, progressListener);
        surfaceView.setRenderer(gameRenderer);

        captureButton = (ImageButton) findViewById(R.id.startCapturing);

        fps = (TextView) findViewById(R.id.fps);
        time = (TextView) findViewById(R.id.time);

        updateVideoPreview();
    }

    @Override
    public void onPause() {
        stopCapturing();

        super.onPause();

        surfaceView.onPause();

        updateUI();
        updateVideoPreview();
    }

    @Override
    public void onResume() {
        super.onResume();

        surfaceView.onResume();
    }

    public void clickToggleCapturing(View view) throws IOException {
        if (gameRenderer.isCapturingStarted()) {
            stop();
        } else {
            start();
        }

        updateUI();
    }

    public void updateFps(int fps) {
        fpsText = String.valueOf(fps) + " FPS";
        this.fps.setText(fpsText);
    }

    public void updateTimer() {
        timeText = Format.duration(System.currentTimeMillis() - startTime);
        time.setText(timeText);
    }

    protected void updateUI() {

        ImageButton settingsButton = (ImageButton)findViewById(R.id.settings);
        ImageButton previewButton = (ImageButton)findViewById(R.id.preview);

        if (gameRenderer.isCapturingStarted() == false) {
            captureButton.setImageResource(R.drawable.rec_inact);

            settingsButton.setVisibility(View.VISIBLE);
            time.setVisibility(View.INVISIBLE);
        } else {
            captureButton.setImageResource(R.drawable.rec_act);

            settingsButton.setVisibility(View.INVISIBLE);
            previewButton.setVisibility(View.INVISIBLE);
            time.setVisibility(View.VISIBLE);
        }
    }

    protected void startCapturing() throws IOException {
        lastFileName = "game_capturing.mp4";
        gameRenderer.startCapturing(videoPath + lastFileName);
    }

    protected void stopCapturing() {
        gameRenderer.stopCapturing();
    }

    public void start() throws IOException {
        captureButton.setEnabled(false);

        gameRenderer.setRenderingMethod(renderingMethod);

        startCapturing();

        startTime = System.currentTimeMillis();

        timer = new Timer();
        timer.schedule(new UpdateTimerTask(), 0, 1000);

        uiHandler.sendMessageDelayed(uiHandler.obtainMessage(ENABLE_BUTTON, R.id.startCapturing, 0), 500);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }

        captureButton.setEnabled(false);

        stopCapturing();

        uiHandler.sendMessageDelayed(uiHandler.obtainMessage(ENABLE_BUTTON, R.id.startCapturing, 0), 500);

        updateVideoPreview();
    }

    public void updateVideoPreview() {
        Bitmap thumb;

        thumb = ThumbnailUtils.createVideoThumbnail(videoPath + lastFileName, MediaStore.Video.Thumbnails.MINI_KIND);

        ImageButton preview = (ImageButton)findViewById(R.id.preview);

        if(thumb == null) {
            preview.setVisibility(View.INVISIBLE);
        } else {
            preview.setVisibility(View.VISIBLE);
            preview.setImageBitmap(thumb);
        }
    }

    protected void playVideo() {
        String videoUrl = "file:///" + videoPath + lastFileName;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.parse(videoUrl);
        intent.setDataAndType(data, "video/mp4");
        startActivity(intent);
    }

    public void playPreview(View v) {
        playVideo();
    }

    public void showSettings(View view) {
        GameCaptureSettingsPopup settingsPopup = new GameCaptureSettingsPopup(this);
        settingsPopup.setEventListener(this);
        settingsPopup.setSettings(renderingMethod);

        settingsPopup.show(view, false);
    }

    @Override
    public void onRenderMethodChanged(GameRenderer.RenderingMethod method) {
        renderingMethod = method;
    }

    protected void showToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }

    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            uiHandler.sendMessage(uiHandler.obtainMessage(UPDATE_TIMER));
        }
    }
}
