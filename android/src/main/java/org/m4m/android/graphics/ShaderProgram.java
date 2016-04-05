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

package org.m4m.android.graphics;

import android.opengl.GLES20;
import org.m4m.domain.graphics.IEglUtil;
import org.m4m.domain.graphics.IShaderProgram;

import java.util.HashMap;

public class ShaderProgram implements IShaderProgram {
    private static int INVALID_VALUE = -1;
    private int programHandle = INVALID_VALUE;
    private HashMap<String, Integer> attributes = new HashMap<String, Integer>();
    private IEglUtil eglUtil;

    public ShaderProgram(IEglUtil eglUtil) {
        this.eglUtil = eglUtil;
    }

    public void create(String vertexCode, String fragmentCode) {
        int vertexShader = INVALID_VALUE;
        if (vertexCode != null) {
            vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
        }
        if (vertexShader == 0) {
            programHandle = 0;
            return;
        }

        int fragmentShader = INVALID_VALUE;
        if (fragmentCode != null) {
            fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode);
        }
        if (fragmentShader == 0) {
            programHandle = 0;
            return;
        }

        programHandle = GLES20.glCreateProgram();
        eglUtil.checkEglError("glCreateProgram");

        GLES20.glAttachShader(programHandle, vertexShader);
        eglUtil.checkEglError("glAttachShader");

        GLES20.glAttachShader(programHandle, fragmentShader);
        eglUtil.checkEglError("glAttachShader");

        GLES20.glLinkProgram(programHandle);
    }

    public void use() {
        GLES20.glUseProgram(programHandle);
    }

    public void unUse() {
        GLES20.glUseProgram(0);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        eglUtil.checkEglError("glCreateShader");

        GLES20.glShaderSource(shader, shaderCode);
        eglUtil.checkEglError("glShaderSource");

        GLES20.glCompileShader(shader);
        eglUtil.checkEglError("glCompileShader");

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String info = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new IllegalArgumentException("Shader compilation failed with: " + info);
        }

        return shader;
    }

    public int getAttributeLocation(String attribute) {
        if (attributes.containsKey(attribute)) {
            return attributes.get(attribute);
        }
        int location = GLES20.glGetAttribLocation(programHandle, attribute);
        eglUtil.checkEglError("glGetAttribLocation " + attribute);
        if (location == -1) {
            location = GLES20.glGetUniformLocation(programHandle, attribute);
            eglUtil.checkEglError("glGetUniformLocation " + attribute);
        }
        if (location == -1) {
            throw new IllegalStateException("Can't find a location for attribute " + attribute);
        }
        attributes.put(attribute, location);
        return location;
    }

    public int getProgramHandle() {
        return programHandle;
    }
}
