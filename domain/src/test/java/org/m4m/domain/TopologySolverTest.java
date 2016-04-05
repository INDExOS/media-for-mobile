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

import org.m4m.domain.mediaComposer.ProgressListenerFake;
import org.m4m.domain.pipeline.ConnectorFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class TopologySolverTest extends TestBase {

    MediaSource source;
    AudioDecoder audioDecoder;
    VideoDecoder videoDecoder;
    AudioEffector audioEffector;
    VideoEffector videoEffector;
    VideoEncoder videoEncoder;
    AudioEncoder audioEncoder;
    Render render;
    ICameraSource cameraSource;
    TopologySolver topologySolver = new TopologySolver();
    private ICaptureSource screenCaptureSource;
    private IMediaSource mediaSource;
    private IMicrophoneSource micSource;
    private SurfaceRender surfaceRender;
    private ICommandProcessor commandProcessor;
    private ConnectorFactory connectorFactory;

    @Before
    public void setUp() throws RuntimeException {
        source = create.mediaSource().construct();
        audioDecoder = create.audioDecoder().construct();
        videoDecoder = create.videoDecoder().construct();
        videoEffector = create.videoEffector().construct();
        videoEncoder = create.videoEncoder().construct();
        render = create.render().construct();
        audioEncoder = create.audioEncoder().construct();
        audioEffector = create.audioEffector().construct();
        cameraSource = create.cameraSource().construct();
        micSource = create.microphoneSource().construct();
        screenCaptureSource = create.screenCaptureSource().construct();
        mediaSource = create.multipleMediaSource().construct();
        surfaceRender = create.surfaceRender().construct();


        connectorFactory = new ConnectorFactory(new CommandProcessor(new ProgressListenerFake()), null);
        Collection<IsConnectable> connectionRules = connectorFactory.createConnectionRules();
        for (IsConnectable connectionRule : connectionRules) {
            topologySolver.addConnectionRule(connectionRule);
        }

    }

    private ITopologyTree getNextNodeFor(Collection<ITopologyTree> next, Object vdecoder) {
        Iterator<ITopologyTree> i = next.iterator();

        for (; i.hasNext(); ) {
            ITopologyTree next1 = i.next();
            if (next1.current() == vdecoder) {
                return next1;
            }
        }
        throw new RuntimeException("notFound");
    }

    private boolean hasItem(Collection<ITopologyTree> next, Object vdecoder) {
        Iterator i = next.iterator();

        for (; i.hasNext(); ) {
            ITopologyTree next1 = (ITopologyTree) i.next();
            if (next1.current() == vdecoder) {
                return true;
            }
        }

        return false;
    }


    @Test
    public void canResolve_SourceDecodeEncodeMuxPipeline() {
        topologySolver.add(videoDecoder);
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), source);
        assertTrue(hasItem(tree.next(), videoDecoder));
        ITopologyTree second = getNextNodeFor(tree.next(), videoDecoder);
        assertTrue(hasItem(second.next(), videoEncoder));
        ITopologyTree third = getNextNodeFor(second.next(), videoEncoder);
        assertTrue(hasItem(third.next(), render));
    }

    @Test
    public void canResolve_SourceDecodeEffectEncodeMuxPipeline() {
        topologySolver.add(videoDecoder);
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);
        topologySolver.add(videoEffector);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), source);
        assertTrue(hasItem(tree.next(), videoDecoder));

        ITopologyTree video = getNextNodeFor(tree.next(), videoDecoder);
        assertTrue(hasItem(video.next(), videoEffector));

        video = getNext(video);
        assertTrue(hasItem(video.next(), videoEncoder));

        video = getNext(video);
        assertTrue(hasItem(video.next(), render));
    }

    @Test
    public void srcDecAdecEncAencMux() {
        topologySolver.add(audioDecoder);
        topologySolver.add(audioEncoder);
        topologySolver.add(videoDecoder);
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), source);
        assertTrue(hasItem(tree.next(), videoDecoder));
        assertTrue(hasItem(tree.next(), audioDecoder));

        ITopologyTree videoSecond = getNextNodeFor(tree.next(), videoDecoder);
        assertTrue(hasItem(videoSecond.next(), videoEncoder));

        ITopologyTree videoThird = getNextNodeFor(videoSecond.next(), videoEncoder);
        assertTrue(hasItem(videoThird.next(), render));

        ITopologyTree audioSecond = getNextNodeFor(tree.next(), audioDecoder);
        assertTrue(hasItem(audioSecond.next(), audioEncoder));

        ITopologyTree audioThird = getNextNodeFor(audioSecond.next(), audioEncoder);
        assertTrue(hasItem(audioThird.next(), render));
    }

    @Test
    public void srcDecAdecEffectAeffectEncAencMux() {
        topologySolver.add(audioDecoder);
        topologySolver.add(audioEncoder);
        topologySolver.add(audioEffector);
        topologySolver.add(videoEffector);
        topologySolver.add(videoDecoder);
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), source);
        assertTrue(hasItem(tree.next(), videoDecoder));
        assertTrue(hasItem(tree.next(), audioDecoder));

        ITopologyTree video = getNextNodeFor(tree.next(), videoDecoder);
        assertTrue(hasItem(video.next(), videoEffector));

        video = getNext(video);
        assertTrue(hasItem(video.next(), videoEncoder));

        video = getNext(video);
        assertTrue(hasItem(video.next(), render));


        ITopologyTree audio = getNextNodeFor(tree.next(), audioDecoder);
        assertTrue(hasItem(audio.next(), audioEffector));

        audio = getNext(audio);
        assertTrue(hasItem(audio.next(), audioEncoder));

        audio = getNext(audio);
        assertTrue(hasItem(audio.next(), render));
    }

    @Test
    public void cameraCaptureEffectEncMux() {
        topologySolver.add(videoEffector);
        topologySolver.add(videoEncoder);
        topologySolver.add(cameraSource);
        topologySolver.add(render);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), cameraSource);
        assertTrue(hasItem(tree.next(), videoEffector));

        tree = getNext(tree);
        assertTrue(hasItem(tree.next(), videoEncoder));

        tree = getNext(tree);
        assertTrue(hasItem(tree.next(), render));
    }

    @Test
    public void screenCaptureSourceEffectEncoderMuxPipeline() {
        topologySolver.add(videoEncoder);
        topologySolver.add(screenCaptureSource);
        topologySolver.add(render);
        topologySolver.add(videoEffector);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), screenCaptureSource);
        assertTrue(hasItem(tree.next(), videoEffector));

        tree = getNext(tree);
        assertTrue(hasItem(tree.next(), videoEncoder));

        tree = getNext(tree);
        assertTrue(hasItem(tree.next(), render));
    }

    private ITopologyTree getNext(ITopologyTree tree) {
        return (ITopologyTree) tree.next().iterator().next();
    }

    @Test
    public void cameraCaptureEffectEncodeAudioFileAudioDecodeAudioEffectAudioEncodeMux() {
        topologySolver.add(cameraSource);
        topologySolver.add(videoEffector);
        topologySolver.add(videoEncoder);

        topologySolver.add(source);
        topologySolver.add(audioDecoder);
        topologySolver.add(audioEncoder);
        topologySolver.add(audioEffector);

        topologySolver.add(render);

        Collection<ITopologyTree> trees = topologySolver.resolve();

        ITopologyTree video = getNextNodeFor(trees, cameraSource);
        ITopologyTree audio = getNextNodeFor(trees, source);

        assertTrue(hasItem(video.next(), videoEffector));

        video = getNext(video);
        assertTrue(hasItem(video.next(), videoEncoder));

        video = getNext(video);
        assertTrue(hasItem(video.next(), render));


        assertTrue(hasItem(audio.next(), audioDecoder));

        audio = getNext(audio);
        assertTrue(hasItem(audio.next(), audioEffector));

        audio = getNext(audio);
        assertTrue(hasItem(audio.next(), audioEncoder));

        audio = getNext(audio);
        assertTrue(hasItem(audio.next(), render));
    }

    @Test
    public void canModifyTopologyAfterUnsucessSolving() {
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);
        try {
            topologySolver.resolve();
        } catch (Throwable exception) {}
        topologySolver.add(videoDecoder);
        topologySolver.resolve();
    }

    @Test(expected = RuntimeException.class)
    public void cannotModifyTopologyAfterSucessSolving() {
        topologySolver.add(videoDecoder);
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);
        topologySolver.resolve();

        topologySolver.add(audioDecoder);
    }

    @Test
    public void effectsConnectedToEncodersThanToDecoders() {
        topologySolver.add(audioDecoder);
        topologySolver.add(audioEncoder);
        topologySolver.add(audioEffector);
        topologySolver.add(videoEffector);
        topologySolver.add(videoDecoder);
        topologySolver.add(videoEncoder);
        topologySolver.add(source);
        topologySolver.add(render);

        Collection<Pair<IOutputRaw, IInputRaw>> queue = topologySolver.getConnectionsQueue();
        int efffectToEncodeIndex = connectionIndex(queue, VideoEffector.class, Encoder.class);
        int decodeToEffecIndex = connectionIndex(queue, VideoDecoder.class, VideoEffector.class);

        assertTrue(efffectToEncodeIndex < decodeToEffecIndex);
    }

    private int connectionIndex(Collection<Pair<IOutputRaw, IInputRaw>> queue, Class leftClass, Class rightClass) {
        int index = 0;
        for (Pair<IOutputRaw, IInputRaw> rawPair : queue) {
            if (leftClass.isInstance(rawPair.left) &&
                rightClass.isInstance(rawPair.right)) {
                return index;
            }
            index++;
        }
        throw new RuntimeException("cannot find such connection");
    }

    @Test
    public void srcPlusRender() {
        topologySolver.add(source);
        topologySolver.add(render);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), source);
        assertTrue(hasItem(tree.next(), render));

    }

    @Test
    public void multipleMediaSourcePlusRender() {
        topologySolver.add(mediaSource);
        topologySolver.add(render);

        ITopologyTree tree = topologySolver.resolve().iterator().next();

        assertEquals(tree.current(), mediaSource);
        assertTrue(hasItem(tree.next(), render));

    }

    @Test
    public void multiple2PassThroughPluginRender() {
        topologySolver.add(mediaSource);
        topologySolver.add(render);
        PassThroughPlugin pipe1 = new PassThroughPlugin(10, MediaFormatType.AUDIO);
        PassThroughPlugin pipe2 = new PassThroughPlugin(10, MediaFormatType.VIDEO);
        topologySolver.add(pipe1);
        topologySolver.add(pipe2);

        Collection<ITopologyTree> trees = topologySolver.resolve();

        assertEquals(4, topologySolver.getConnectionsQueue().size());
    }

    @Test
    public void camMicVideoEffect2EncodersMuxer() {
        topologySolver.add(micSource);
        topologySolver.add(cameraSource);
        topologySolver.add(videoEffector);
        topologySolver.add(render);
        topologySolver.add(audioEncoder);
        topologySolver.add(videoEncoder);

        Collection<ITopologyTree> trees = topologySolver.resolve();

        ITopologyTree video = getNextNodeFor(trees, cameraSource);
        ITopologyTree audio = getNextNodeFor(trees, micSource);

        assertTrue(hasItem(video.next(), videoEffector));

        video = getNext(video);
        assertTrue(hasItem(video.next(), videoEncoder));

        video = getNext(video);
        assertTrue(hasItem(video.next(), render));

        assertTrue(hasItem(audio.next(), audioEncoder));

        audio = getNext(audio);
        assertTrue(hasItem(audio.next(), render));

        Assert.assertEquals(5, topologySolver.getConnectionsQueue().size());
    }

    @Test
    public void sorceDecSurfaceRender() {
        topologySolver.add(mediaSource);
        topologySolver.add(videoDecoder);
        topologySolver.add(surfaceRender);

        Collection<ITopologyTree> trees = topologySolver.resolve();

        ITopologyTree video = getNextNodeFor(trees, mediaSource);

        assertTrue(hasItem(video.next(), videoDecoder));
        video = getNext(video);

        assertTrue(hasItem(video.next(), surfaceRender));

        Assert.assertEquals(2, topologySolver.getConnectionsQueue().size());

        //also connection checks
        connectorFactory.connect(mediaSource, videoDecoder);

        connectorFactory.connect(videoDecoder, surfaceRender);
    }
}