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


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;
import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.FileSegment;
import org.m4m.domain.IPreview;
import org.m4m.domain.Resolution;
import org.m4m.domain.graphics.TextureRenderer;
import org.m4m.effects.GrayScaleEffect;
import org.m4m.effects.InverseEffect;
import org.m4m.effects.MuteAudioEffect;
import org.m4m.effects.SepiaEffect;
import org.m4m.effects.TextOverlayEffect;
import org.m4m.samples.controls.CameraCaptureSettingsPopup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CameraCapturerActivity extends ActivityWithTimeline implements CameraCaptureSettingsPopup.CameraCaptureSettings {

    public org.m4m.IProgressListener progressListener = new org.m4m.IProgressListener() {
        @Override
        public void onMediaStart() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isRecordingInProgress = true;
                        captureButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
        public void onMediaProgress(float progress) {
        }

        @Override
        public void onMediaDone() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isRecordingInProgress = false;
                        showToast("Video saved to " + getVideoFilePath());
                        updateVideoFilePreview();
                        captureButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {
            try {
                final Exception e = exception;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String message = (e.getMessage() != null) ? e.getMessage() : e.toString();
                        showMessageBox("Capturing failed" + "\n" + message, null);

                        isRecordingInProgress = false;
                        captureButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
            }
        }
    };
    private AndroidMediaObjectFactory factory;
    private boolean recordAudio = true;
    private Handler handler;

    private boolean autoFocusSupported = false;
    private boolean autoFlashSupported = false;
    private ImageButton videoFilePreview;
    private ImageButton captureButton;
    private TextView fpsText;
    private GLSurfaceView surfaceView;
    private CheckBox muteCheckBox;

    class AllEffects implements org.m4m.IVideoEffect {
        private FileSegment segment = new FileSegment(0l, 0l);
        private ArrayList<org.m4m.IVideoEffect> videoEffects = new ArrayList<org.m4m.IVideoEffect>();
        private int activeEffectId;
        private long l;
        private long msPerFrame = 1;
        ArrayList<Long> lst = new ArrayList<Long>();
        static final int window = 10;

        public synchronized double getFps() {
            long sum = 0;

            for (Long aLong : lst) {
                sum += aLong;
            }
            return 1e9 * lst.size() / sum;
        }

        @Override
        public FileSegment getSegment() {
            return segment;
        }

        @Override
        public void setSegment(FileSegment segment) {
        }

        @Override
        public void start() {
            for (org.m4m.IVideoEffect effect : videoEffects) {
                effect.start();
            }
        }

        @Override
        public void applyEffect(int inTextureId, long timeProgress, float[] transformMatrix) {
            long currentTime = System.nanoTime();
            msPerFrame = currentTime - l;
            l = currentTime;
            synchronized (this) {
                lst.add(msPerFrame);
                if (lst.size() > window) {
                    lst.remove(0);
                }
            }
            handler.sendMessage(handler.obtainMessage());

            videoEffects.get(activeEffectId).applyEffect(inTextureId, timeProgress, transformMatrix);
        }

        @Override
        public void setInputResolution(Resolution resolution) {
            for (org.m4m.IVideoEffect videoEffect : videoEffects) {
                videoEffect.setInputResolution(resolution);
            }
        }

        @Override
        public void setFillMode(TextureRenderer.FillMode fillMode) {
            org.m4m.IVideoEffect activeEffect = videoEffects.get(activeEffectId);
            if (activeEffect != null)
                activeEffect.setFillMode(fillMode);
        }

        @Override
        public TextureRenderer.FillMode getFillMode() {
            org.m4m.IVideoEffect activeEffect = videoEffects.get(activeEffectId);
            return activeEffect != null ? activeEffect.getFillMode() : null;
        }

        @Override
        public void setAngle(int degrees) {
            for (org.m4m.IVideoEffect videoEffect : videoEffects) {
                videoEffect.setAngle(degrees);
            }
        }

        @Override
        public int getAngle() {
            return videoEffects.get(activeEffectId).getAngle();
        }

        public void setActiveEffectId(int activeEffectId) {
            this.activeEffectId = activeEffectId;
        }

        public int getActiveEffectId() {
            return activeEffectId;
        }

        public ArrayList<org.m4m.IVideoEffect> getVideoEffects() {
            return videoEffects;
        }
    }

    boolean isRecordingInProgress = false;

    CameraCaptureSettingsPopup settingsPopup;

    Camera camera = null;
    private int camera_type = 0;
    org.m4m.CameraCapture capture;
    List<Camera.Size> supportedResolutions;
    Resolution encodedResolution = new Resolution(640, 480);
    private IPreview preview;
    AllEffects allEffects = new AllEffects();
    private MuteAudioEffect muteAudioEffect = new MuteAudioEffect();

    private int activeEffectId = 0;
    ScheduledExecutorService service;
    ScheduledFuture<?> scheduledFuture;

    private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;

    private void setViewIDs() {

        captureButton = (ImageButton) findViewById(R.id.streaming);

        videoFilePreview = (ImageButton) findViewById(R.id.preview);
        videoFilePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });

        captureButton = (ImageButton) findViewById(R.id.streaming);
        fpsText = (TextView) findViewById(R.id.fpsText);

        // Setup focus on click
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.camera_layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (camera == null) {
                            return true;
                        }

                        if (autoFocusSupported) {
                            camera.cancelAutoFocus();

                            Camera.Parameters param = camera.getParameters();
                            param.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                            param.setFocusAreas(getFocusAreas(v, event));
                            camera.setParameters(param);

                            startAutoFocus(null);

                            // if the scheduledFuture is run (in other words it is not the first tap) and not cancelled - call cancel
                            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                                scheduledFuture.cancel(true);
                            }

                            scheduledFuture = service.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    startAutoFocus(new Camera.AutoFocusCallback() {
                                        @Override
                                        public void onAutoFocus(boolean success, Camera camera) {
                                            camera.cancelAutoFocus();
                                            Camera.Parameters params = camera.getParameters();

                                            if (params.getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) {
                                                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                                                camera.setParameters(params);
                                            }
                                        }
                                    });
                                }
                            }, 10, TimeUnit.SECONDS);

                            ImageView focusRect = (ImageView) findViewById(R.id.focus_rect);
                            focusRect.setX(event.getX() - focusRect.getWidth() / 2);
                            focusRect.setY(event.getY() - focusRect.getHeight() / 2);
                        }
                        break;
                }
                return true;
            }

            private ArrayList<Camera.Area> getFocusAreas(View v, MotionEvent event) {
                float camera_x = event.getX() * 2000 / v.getWidth() - 1000;
                float camera_y = event.getY() * 2000 / v.getHeight() - 1000;
                Rect focusArea = new Rect((int) camera_x - 20, (int) camera_y - 20, (int) camera_x + 20, (int) camera_y + 20);

                if (focusArea.left < -1000) focusArea.left = -1000;
                if (focusArea.top < -1000) focusArea.left = -1000;
                if (focusArea.right > 1000) focusArea.right = 1000;
                if (focusArea.bottom < 1000) focusArea.bottom = 1000;

                ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(focusArea, 1000));
                return focusAreas;
            }
        });
    }

    public void onCreate(Bundle icicle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onCreate(icicle);
        service = Executors.newSingleThreadScheduledExecutor();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_capturer_activity);
        setViewIDs();

        Intent intent = getIntent();
        camera_type = intent.getIntExtra("CAMERA_TYPE", 0);

        createCamera();
        factory = new AndroidMediaObjectFactory(getApplicationContext());
        configureEffects(factory);
        createCapturePipeline();
        createPreview();

        settingsPopup = new CameraCaptureSettingsPopup(this, supportedResolutions, this);

        updateVideoFilePreview();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                fpsText.setText(String.valueOf(String.format("%.0f", allEffects.getFps())));
            }
        };

        muteCheckBox = (CheckBox) findViewById(R.id.muteCheckBox);
        muteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                        if (isChecked) {
                                                            muteAudioEffect.setMute(true);
                                                        } else {
                                                            muteAudioEffect.setMute(false);
                                                        }
                                                    }
                                                }
        );
    }

    private void configureEffects(final AndroidMediaObjectFactory factory) {

        ArrayList<org.m4m.IVideoEffect> videoEffects = allEffects.getVideoEffects();

        videoEffects.clear();

        videoEffects.add(new VideoEffect(0, factory.getEglUtil()) {
        });
        videoEffects.add(new GrayScaleEffect(0, factory.getEglUtil()));
        videoEffects.add(new SepiaEffect(0, factory.getEglUtil()));
        videoEffects.add(new InverseEffect(0, factory.getEglUtil()));
        videoEffects.add(new TextOverlayEffect(0, factory.getEglUtil()));

        if (camera_type == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            checkWorkingEffects();
        }
    }


    // removeTextOverlay
    private void checkWorkingEffects() {
        int removeIndex = 4;

        ImageButton effectText = (ImageButton) findViewById(R.id.effect_text);
        if (effectText != null) {
            ((ViewManager) findViewById(R.id.effect_text).getParent()).removeView(findViewById(R.id.effect_text));
        }

        if (allEffects.getVideoEffects().size() >= (removeIndex + 1)) {
            allEffects.getVideoEffects().remove(removeIndex); // remove TextOverLay
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null) {
            stopRecording();

            saveSettings();

            destroyPreview();
            destroyCapturePipeline();
            destroyCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (camera == null) {
            createCamera();
            factory = new AndroidMediaObjectFactory(getApplicationContext());
            createCapturePipeline();
            configureEffects(factory);
            createPreview();
        }
        restoreSettings();
    }

    private void saveSettings() {
        //Save camera effect settings
        activeEffectId = allEffects.getActiveEffectId();
    }

    private void restoreSettings() {
        //Restore saved effect settings
        allEffects.setActiveEffectId(activeEffectId);
        preview.setActiveEffect(allEffects);
    }


    private void createCapturePipeline() {
        capture = new org.m4m.CameraCapture(factory, progressListener);
        capture.setFillMode(fillMode);
        if (allEffects != null) {
            capture.addVideoEffect(allEffects);
        }
    }

    private void destroyCapturePipeline() {
        capture = null;
    }

    private void createPreview() {
        surfaceView = new GLSurfaceView(getApplicationContext());

        surfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);

        ((RelativeLayout) findViewById(R.id.camera_layout)).addView(surfaceView, 0);

        preview = capture.createPreview(surfaceView, camera);
        preview.setFillMode(fillMode);

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            capture.setOrientation(90);
        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            capture.setOrientation(0);
        }

        preview.start();
    }

    private void destroyPreview() {
        preview.stop();
        preview = null;
        ((RelativeLayout) findViewById(R.id.camera_layout)).removeView(surfaceView);
        surfaceView = null;
    }

    private void createCamera() {
        camera = Camera.open(camera_type);
        supportedResolutions = camera.getParameters().getSupportedPreviewSizes();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(supportedResolutions.get(0).width, supportedResolutions.get(0).height);


        CameraUtils utils = new CameraUtils(parameters).invoke();
        parameters.setPreviewFpsRange(utils.getMaxFps0(), utils.getMaxFps1());

        String focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        String flashMode = Camera.Parameters.FLASH_MODE_OFF;

        // Check for auto focus support
        List<String> focus_modes = parameters.getSupportedFocusModes();
        if (focus_modes != null) {
            for (String mode : focus_modes) {
                if (mode.equals(focusMode)) {
                    autoFocusSupported = true;
                    parameters.setFocusMode(focusMode);
                    break;
                }
            }
        }

        // Check for auto flash support
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null) {
            for (String mode : flashModes) {
                if (mode.equals(flashMode)) {
                    autoFlashSupported = true;
                    parameters.setFlashMode(flashMode);
                    break;
                }
            }
        }

        camera.setParameters(parameters);

        if (autoFocusSupported) {
            startAutoFocus(null);
            ImageView focusRect = (ImageView) findViewById(R.id.focus_rect);
            focusRect.setVisibility(View.VISIBLE);
        }
    }

    private void startAutoFocus(Camera.AutoFocusCallback cb) {
        try {
            camera.autoFocus(cb);
        } catch (Exception e) {
            autoFocusSupported = false;

            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();
            showMessageBox(message, null);
        }
    }

    private void destroyCamera() {
        camera.release();
        camera = null;
    }

    private void configureMediaStreamFormat() {

        org.m4m.VideoFormat videoFormat = new VideoFormatAndroid("video/avc", encodedResolution.width(), encodedResolution.height());
        videoFormat.setVideoBitRateInKBytes(3000);
        videoFormat.setVideoFrameRate(25);
        videoFormat.setVideoIFrameInterval(1);
        capture.setTargetVideoFormat(videoFormat);

        if (recordAudio) {
            org.m4m.AudioFormat audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 1);
            capture.setTargetAudioFormat(audioFormat);

            if (muteAudioEffect != null) {
                capture.addAudioEffect(muteAudioEffect);
            }
        }
    }

    public void toggleStreaming(View view) {
        updateUI();

        if (isRecordingInProgress) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    public void changeCamera(View view) {
        if (camera_type == 0) {
            camera_type = 1;
        } else {
            camera_type = 0;
        }

        if (camera_type >= Camera.getNumberOfCameras()) {
            camera_type -= Camera.getNumberOfCameras();
        }

        if (camera != null) {
            Intent intent = getIntent();
            intent.putExtra("CAMERA_TYPE", camera_type);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    public void startRecording() {
        if (isRecordingInProgress) {
            Toast.makeText(this, "Can have only one active session.", Toast.LENGTH_SHORT).show();
        } else {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
            }
            captureButton.setEnabled(false);
            capture();
        }
    }

    private void capture() {

        try {
            capture.setTargetFile(getVideoFilePath());
        } catch (IOException e) {
            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();

            showMessageBox(message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }
        configureMediaStreamFormat();

        capture.start();
    }

    public void stopRecording() {
        if (isRecordingInProgress) {
            captureButton.setEnabled(false);
            capture.stop();

            configureEffects(factory);
            preview.setActiveEffect(allEffects);

            // return to focus mode FOCUS_MODE_CONTINUOUS_VIDEO
            if (autoFocusSupported && camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

                startAutoFocus(null);

                camera.setParameters(parameters);
            }
        }
    }

    private File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    public String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/capture.mp4";
    }

    public void onClickEffect(View view) {
        if (isRecordingInProgress) {
            return;
        }

        switch (view.getId()) {
            default: {
                String tag = (String) view.getTag();

                if (tag != null) {
                    allEffects.setActiveEffectId(Integer.parseInt(tag));
                    preview.setActiveEffect(allEffects);
                }
            }
            break;
        }
    }

    public void updateVideoFilePreview() {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(getVideoFilePath(), MediaStore.Video.Thumbnails.MINI_KIND);

        if (thumb == null) {
            videoFilePreview.setVisibility(View.INVISIBLE);
        } else {

            videoFilePreview.setImageBitmap(thumb);
        }
    }

    protected void playVideo() {
        String videoFilePath = getVideoFilePath();
        String videoUrl = "file:///" + videoFilePath;

        if (new File(videoFilePath).exists()) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

            android.net.Uri data = android.net.Uri.parse(videoUrl);
            intent.setDataAndType(data, "video/mp4");
            startActivity(intent);
        } else {
            ImageButton preview = (ImageButton) findViewById(R.id.preview);
            preview.setVisibility(View.INVISIBLE);
        }
    }

    public void showSettings(View view) {
        settingsPopup.show(view, false);
    }

    @Override
    public void displayResolutionChanged(int width, int height) {
        preview.stop();

        Camera.Parameters params = camera.getParameters();

        params.setPreviewSize(width, height);
        camera.setParameters(params);

        preview.updateCameraParameters();

        preview.start();
    }

    @Override
    public void videoResolutionChanged(int width, int height) {
        encodedResolution = new Resolution(width, height);
    }

    @Override
    public void audioRecordChanged(boolean bState) {
        recordAudio = bState;
    }

    private void updateUI() {
        ImageButton settingsButton = (ImageButton) findViewById(R.id.settings);
        ImageButton previewButton = (ImageButton) findViewById(R.id.preview);
        ImageButton changeCameraButton = (ImageButton) findViewById(R.id.change_camera);
        ScrollView container = (ScrollView) findViewById(R.id.effectsContainer);

        if (isRecordingInProgress) {
            captureButton.setImageResource(R.drawable.rec_inact);

            container.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            previewButton.setVisibility(View.VISIBLE);
            changeCameraButton.setVisibility(View.VISIBLE);
        } else {
            captureButton.setImageResource(R.drawable.rec_act);

            container.setVisibility(View.INVISIBLE);
            settingsButton.setVisibility(View.INVISIBLE);
            previewButton.setVisibility(View.INVISIBLE);
            changeCameraButton.setVisibility(View.INVISIBLE);
        }
    }
}
