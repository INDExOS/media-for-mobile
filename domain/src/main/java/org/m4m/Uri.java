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

package org.m4m;

/**
 * This class is used to store and pass Uri references that conform to RFC 2396 in String form.
 */
public class Uri {
    private String uri;

    /**
     * Instantiates an object using a specified URI.
     *
     * @param uri URI.
     */
    public Uri(String uri) {
        this.uri = uri;
    }

    /**
     * Returns an URI.
     *
     * @return URI.
     */
    public String getString() {
        return uri;
    }
}
