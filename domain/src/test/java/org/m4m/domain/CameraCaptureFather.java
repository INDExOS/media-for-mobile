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

package org.m4m.domain;

import org.m4m.CameraCapture;
import org.m4m.IProgressListener;
import org.m4m.IVideoEffect;
import org.m4m.VideoFormat;

import org.m4m.domain.dsl.CameraSourceFather;
import org.m4m.domain.dsl.Father;
import org.m4m.domain.dsl.FatherOf;
import org.m4m.domain.mediaComposer.AndroidMediaObjectFactoryFake;

import java.io.IOException;

import static org.mockito.Mockito.doThrow;

public class CameraCaptureFather extends FatherOf<CameraCapture> {
    private AndroidMediaObjectFactoryFake factory;
    private CameraCapture cameraCapture;
    private CameraSourceFather cameraSourceFather;
    CameraSource camera;
    private VideoFormat videoFormat = create.videoFormat()
            .withFrameRate(0)
            .withIFrameInterval(0)
            .withVideoBitRateInKBytes(0)
            .construct();

    public CameraCaptureFather(Father create, IProgressListener progressListener) {
        super(create);
        factory = new AndroidMediaObjectFactoryFake(create);
        cameraCapture = new CameraCapture(factory, progressListener);
        cameraSourceFather = new CameraSourceFather(create);
        camera = cameraSourceFather.construct();
    }

    public CameraCaptureFather withSourceFather(CameraSourceFather father) {
        this.cameraSourceFather = father;
        camera = cameraSourceFather.construct();
        return this;
    }

    public CameraCaptureFather withSourceFrame(Frame frame) {
        cameraSourceFather.with(frame);
        return this;
    }


    public CameraCaptureFather with(IVideoEffect videoEffect) {
        cameraCapture.addVideoEffect(videoEffect);
        return this;
    }

    public CameraCaptureFather with(VideoEncoder videoEncoder) {
        factory.withVideoEncoder(videoEncoder);
        cameraCapture.setTargetVideoFormat(videoFormat);
        return this;
    }

    public CameraCaptureFather withEncoderMediaCodec(IMediaCodec mediaCodec) {
        VideoEncoder videoEncoder = create.videoEncoder().with(mediaCodec).construct();
        factory.withVideoEncoder(videoEncoder);
        cameraCapture.setTargetVideoFormat(videoFormat);
        return this;
    }

    public CameraCaptureFather withRender(Render render) {
        factory.withSink(render);
        try {
            cameraCapture.setTargetFile("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public CameraCaptureFather with(VideoEffector videoEffector) {
        factory.withVideoEffector(videoEffector);
        return this;
    }

    public CameraCaptureFather with(Pipeline pipeline) {
        cameraCapture.pipeline = pipeline;
        return this;
    }

    public CameraCaptureFather withBrokenStop() {
        doThrow(new RuntimeException()).when(camera).stop();
        return this;
    }

    @Override
    public CameraCapture construct() throws IOException {
        factory.withCameraSource(camera);
        cameraCapture.setCamera(null);

        cameraCapture.setTargetVideoFormat(videoFormat);
        cameraCapture.setTargetFile("");
        return cameraCapture;
    }

    public CameraCaptureFather withSurface(ISurface surface) {
        cameraSourceFather.with(surface);
        return this;
    }
}
