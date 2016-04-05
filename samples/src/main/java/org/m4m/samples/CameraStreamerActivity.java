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
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import org.m4m.CameraCapture;
import org.m4m.IProgressListener;
import org.m4m.IVideoEffect;
import org.m4m.StreamingParameters;
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
import org.m4m.samples.controls.StreamingSettingsPopup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CameraStreamerActivity extends ActivityWithTimeline implements StreamingSettingsPopup.CameraStreamingSettings {
    StreamingParameters parameters;

    public IProgressListener progressListener = new IProgressListener() {
        @Override
        public void onMediaStart() {}

        @Override
        public void onMediaProgress(float progress) {}

        @Override
        public void onMediaDone() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inProgress = false;
                        onStreamingDone();
                    }
                });
            } catch (Exception e) {}
        }

        @Override
        public void onMediaPause() {}

        @Override
        public void onMediaStop() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inProgress = false;
                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
        public void onError(Exception exception) {
            try {
                final Exception e = exception;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inProgress = false;
                        onStreamingDone();

                        String message = (e.getMessage() != null) ? e.getMessage() : e.toString();
                        showMessageBox("Capturing failed" + "\n" + message, null);
                    }
                });
            } catch (Exception e) {
            }
        }
    };

    class AllEffects implements IVideoEffect {
        private FileSegment segment = new FileSegment(0l, 0l);
        private ArrayList<IVideoEffect> videoEffects = new ArrayList<IVideoEffect>();
        private int activeEffectId;
        private long l;
        private long msPerFrame = 1;
        ArrayList<Long> lst = new ArrayList<Long>();
        static final int window = 10;
        private TextureRenderer.FillMode fillMode = TextureRenderer.FillMode.PreserveAspectFit;

        @Override
        public FileSegment getSegment() {
            return segment;
        }

        @Override
        public void setSegment(FileSegment segment) {
        }

        @Override
        public void start() {
            for (IVideoEffect effect : videoEffects) {
                effect.start();
            }
        }

        @Override
        public void applyEffect(int inTextureId, long timeProgress, float[] transformMatrix) {
            long currentTime = System.nanoTime();
            msPerFrame = currentTime - l;
            l = currentTime;
            lst.add(msPerFrame);
            if (lst.size() > window) {
                lst.remove(0);
            }

            videoEffects.get(activeEffectId).applyEffect(inTextureId, timeProgress, transformMatrix);
        }

        @Override
        public void setInputResolution(Resolution resolution) {
            for (IVideoEffect videoEffect : videoEffects) {
                videoEffect.setInputResolution(resolution);
            }
        }

        @Override
        public void setFillMode(TextureRenderer.FillMode fillMode) {
            this.fillMode = fillMode;
            for (IVideoEffect videoEffect : videoEffects) {
                videoEffect.setFillMode(fillMode);
            }
        }

        @Override
        public TextureRenderer.FillMode getFillMode() {
            return fillMode;
        }

        @Override
        public void setAngle(int degrees) {
            for (IVideoEffect videoEffect : videoEffects) {
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

        public ArrayList<IVideoEffect> getVideoEffects() {
            return videoEffects;
        }
    }

    Camera camera = null;
    private int camera_type = 0;
    CameraCapture capture = null;
    private IPreview preview;

    private AndroidMediaObjectFactory factory;
    private int activeEffectId;
    AllEffects allEffects = new AllEffects();
    private MuteAudioEffect muteAudioEffect = new MuteAudioEffect();
    private GLSurfaceView surfaceView;

    private AudioFormatAndroid audioFormat;
    private VideoFormatAndroid videoFormat;

    private boolean inProgress = false;
    private boolean isActive = false;
    private boolean autoFocusSupported = false;
    private boolean autoFlashSupported = false;

    ScheduledExecutorService service;
    ScheduledFuture<?> scheduledFuture;
    private CheckBox muteCheckBox;


    public void onCreate(Bundle icicle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(icicle);
        service = Executors.newSingleThreadScheduledExecutor();
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = getIntent();
        camera_type = intent.getIntExtra("CAMERA_TYPE", 0);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_streamer_activity);
        setViewIDs();

        createCamera();

        factory = new AndroidMediaObjectFactory(getApplicationContext());

        parameters = new StreamingParameters();

        parameters.Host = getString(R.string.streaming_server_default_ip);
        parameters.Port = Integer.parseInt(getString(R.string.streaming_server_default_port));
        parameters.ApplicationName = getString(R.string.streaming_server_default_app);
        parameters.StreamName = getString(R.string.streaming_server_default_stream);

        parameters.isToPublishAudio = false;
        parameters.isToPublishVideo = true;

        configureEffects(factory);
        createCapturePipeline();
        createPreview();

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


    private void setViewIDs() {
        // Setup focus on click
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.streamer_layout);
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

    private void startAutoFocus(Camera.AutoFocusCallback cb) {
        try {
            camera.autoFocus(cb);
        } catch (Exception e) {
            autoFocusSupported = false;

            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();
            showMessageBox(message, null);
        }
    }

    private void configureEffects(final AndroidMediaObjectFactory factory) {

        ArrayList<IVideoEffect> videoEffects = allEffects.getVideoEffects();

        if(videoEffects != null) { videoEffects.clear(); }

        allEffects.getVideoEffects().add(new VideoEffect(0, factory.getEglUtil()) {
        });
        allEffects.getVideoEffects().add(new GrayScaleEffect(0, factory.getEglUtil()));
        allEffects.getVideoEffects().add(new SepiaEffect(0, factory.getEglUtil()));
        allEffects.getVideoEffects().add(new InverseEffect(0, factory.getEglUtil()));
    }

    @Override
    protected void onPause() {
        if (isActive) {
            stopStreaming();
            if (camera != null) {

                saveSettings();

                destroyPreview();
                destroyCamera();
                destroyCapturePipeline();
            }
            isActive = false;
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!isActive) {
            if  (camera == null) {
                createCamera();

                factory = new AndroidMediaObjectFactory(getApplicationContext());
                createCapturePipeline();

                configureEffects(factory);
                createPreview();
            }
            isActive = true;
        }

        restoreSettings();
        super.onResume();
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
        capture = new CameraCapture(factory, progressListener);
        if (allEffects != null) {
            capture.addVideoEffect(allEffects);
        }
        if (muteAudioEffect != null) {
            capture.addAudioEffect(muteAudioEffect);
        }
    }

    private void destroyCapturePipeline() {
        capture = null;
    }

    private void createPreview() {
        surfaceView = new GLSurfaceView(getApplicationContext());

        ((RelativeLayout)findViewById(R.id.streamer_layout)).addView(surfaceView, 0);
        preview = capture.createPreview(surfaceView, camera);

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            capture.setOrientation(90);
        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ){
            capture.setOrientation(0);
        }

        preview.start();
    }

    private void destroyPreview() {
        preview.stop();
        preview = null;
        ((RelativeLayout)findViewById(R.id.streamer_layout)).removeView(surfaceView);
        surfaceView = null;
    }

    private StreamingParameters prepareStreamingParams() {
        return parameters;
    }

    private void configureMediaStreamFormat() {

        videoFormat = new VideoFormatAndroid("video/avc", 640, 480);
        videoFormat.setVideoBitRateInKBytes(1000);
        videoFormat.setVideoFrameRate(25);
        videoFormat.setVideoIFrameInterval(1);

        audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 1);
    }

    private void createCamera() {
        camera = Camera.open(camera_type);

        List<Camera.Size> supportedResolutions = camera.getParameters().getSupportedPreviewSizes();

        Camera.Size maxCameraResolution = supportedResolutions.get(0);

        for (Camera.Size size : supportedResolutions) {

            if (maxCameraResolution.width < size.width) {
                maxCameraResolution = size;
            }
        }

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(maxCameraResolution.width, maxCameraResolution.height);

        //parameters.setRecordingHint(true);

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

    private void destroyCamera() {
        camera.release();
        camera = null;
    }

    public void startStreaming() {
        updateUI(true);

        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }

        configureMediaStreamFormat();
        capture.setTargetVideoFormat(videoFormat);
        capture.setTargetAudioFormat(audioFormat);
        capture.setTargetConnection(prepareStreamingParams());

        captureStart();
        inProgress = true;
    }


    private void captureStart ()
    {
        configureMediaStreamFormat();

        capture.start();
    }

    public void stopStreaming() {
        updateUI(false);
        if (inProgress && capture != null) {
            capture.stop();

            configureEffects(factory);
            preview.setActiveEffect(allEffects);

            if (autoFocusSupported && camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

                startAutoFocus(null);

                camera.setParameters(parameters);
            }
        }
        inProgress = false;
    }

    public void onStreamingDone() {

        updateUI(false);
    }

    private void updateUI(boolean inProgress) {
        ImageButton settingsButton = (ImageButton)findViewById(R.id.settings);
        ImageButton captureButton = (ImageButton)findViewById(R.id.start);
        ImageButton changeCameraButton = (ImageButton) findViewById(R.id.change_camera);

        ScrollView container = (ScrollView)findViewById(R.id.effectsContainer);

        if (inProgress == false) {
            captureButton.setImageResource(R.drawable.rec_inact);
            settingsButton.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
            changeCameraButton.setVisibility(View.VISIBLE);
        } else {
            captureButton.setImageResource(R.drawable.rec_act);
            settingsButton.setVisibility(View.INVISIBLE);
            container.setVisibility(View.INVISIBLE);
            changeCameraButton.setVisibility(View.INVISIBLE);
        }
    }

    public void changeCamera(View view) {
        if (camera_type == 0) {
            camera_type = 1;
        } else {
            camera_type = 0;
        }

        if (camera_type >= Camera.getNumberOfCameras())
            camera_type -= Camera.getNumberOfCameras();

        if (camera != null) {
            Intent intent = getIntent();
            intent.putExtra("CAMERA_TYPE", camera_type);
            overridePendingTransition(0,0);
            finish();
            overridePendingTransition(0,0);
            startActivity(intent);
        }
    }

    public void onSettings(View view) {
        StreamingSettingsPopup settingsPopup = new StreamingSettingsPopup(this);
        settingsPopup.setEventListener(this);
        settingsPopup.setSettings(parameters);

        settingsPopup.show(view, false);
    }

    public void onStreaming(View view) {
        if (inProgress) {
            stopStreaming();
        } else {
            startStreaming();
        }
    }

    public void onClickEffect(View v) {
        if(inProgress) {
            return;
        }

        switch (v.getId()) {
            default: {
                String tag = (String) v.getTag();

                if(tag != null)
                {
                   allEffects.setActiveEffectId(Integer.parseInt(tag));
                   preview.setActiveEffect(allEffects);
                }
            }
            break;
        }
    }

    @Override
    public void onStreamingParamsChanged(StreamingParameters parameters) {
        this.parameters = parameters;
    }
}
