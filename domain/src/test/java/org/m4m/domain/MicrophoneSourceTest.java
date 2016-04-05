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

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

public class MicrophoneSourceTest extends TestBase {

    @Test
    public void stopAndPullFrame_putsEndOfStreamCommand() {
        MicrophoneSource microphoneSource = new MicrophoneSource();

        microphoneSource.stop();
        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData);

        microphoneSource.pull(create.frame().construct());

        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData, Command.EndOfFile);
    }

    //TODO: refactor eof
    @Test
    public void stop_generatesHasData_thenPullEOFframe_thenGeneratesEOFCommand() {
        MicrophoneSource microphoneSource = new MicrophoneSource();

        microphoneSource.stop();
        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData);

        Frame frame = create.frame(0).construct();

        microphoneSource.pull(frame);

        assertEquals(Frame.EOF(), frame);

        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData, Command.EndOfFile);
    }

    @Test
    public void start_putsHasDataCommand() {
        MicrophoneSource microphoneSource = new MicrophoneSource();

        microphoneSource.start();

        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData);

    }

    @Test
    public void start_resetStopFlag() {
        MicrophoneSource microphoneSource = new MicrophoneSource();

        assertEquals(microphoneSource.isStopped(), true);
        microphoneSource.start();
        assertEquals(microphoneSource.isStopped(), false);
        microphoneSource.stop();
        assertEquals(microphoneSource.isStopped(), true);
        microphoneSource.start();
        assertEquals(microphoneSource.isStopped(), false);
    }

    @Test
    public void pullGeneratesHasDataIfNotStopped_andIfEOFFrameNotPulled() {
        MicrophoneSource microphoneSource = new MicrophoneSource();

        microphoneSource.start();
        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData);

        microphoneSource.stop();
        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData, Command.HasData);

        Frame frame = mock(Frame.class);
        microphoneSource.pull(frame);

        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData, Command.HasData, Command.EndOfFile);

        microphoneSource.getOutputCommandQueue().clear();

        microphoneSource.start();
        assertThat(microphoneSource.getOutputCommandQueue()).equalsTo(Command.HasData);
    }

	@Test
    public void pullConvertInvalidFrameIntoEOFFrame() {
        MicrophoneSource microphoneSource = new MicrophoneSource();
        microphoneSource.start();

        //Construct invalid frame
        Frame frame = create.frame(0).construct();
        frame.setLength(-3); //-3 -- AudioRecord ERROR_INVALID_OPERATION

        microphoneSource.pull(frame);

        assertEquals(Frame.EOF(), frame);
    }
}
