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

package org.m4m.domain.dsl;

import org.m4m.StreamingParameters;

import static org.mockito.Mockito.mock;

public class StreamingParametersFather extends FatherOf<StreamingParameters> {

    private StreamingParameters streamingParameters = mock(StreamingParameters.class);

    public StreamingParametersFather(Father create) {
        super(create);
    }

    public  StreamingParametersFather withHost(String Host) {
        this.streamingParameters.Host = Host;
        return this;
    }

    public  StreamingParametersFather withPort (int Port) {
        this.streamingParameters.Port = Port;
        return this;
    }

    public  StreamingParametersFather withApplicationName (String ApplicationName) {
        this.streamingParameters.ApplicationName = ApplicationName;
        return this;
    }

    public  StreamingParametersFather withStreamName (String StreamName) {
        this.streamingParameters.StreamName = StreamName;
        return this;
    }

    public  StreamingParametersFather withSecure (boolean Secure) {
        this.streamingParameters.Secure = Secure;
        return this;
    }

    public  StreamingParametersFather withUsername (String Username) {
        this.streamingParameters.Username = Username;
        return this;
    }

    public  StreamingParametersFather withPassword (String Password) {
        this.streamingParameters.Password = Password;
        return this;
    }

    public  StreamingParametersFather withIsToPublishAudio (boolean isToPublishAudio) {
        this.streamingParameters.isToPublishAudio = isToPublishAudio;
        return this;
    }

    public  StreamingParametersFather withIsToPublishVideo (boolean isToPublishVideo) {
        this.streamingParameters.isToPublishVideo = isToPublishVideo;
        return this;
    }

    public StreamingParameters construct() {
        return new StreamingParameters();
    }

}
