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

package org.m4m.domain.pipeline;

import org.m4m.domain.Pair;

import java.util.Arrays;
import java.util.Comparator;

public class TriangleVerticesCalculator {
    static int stride = 5;
    private final float[] triangleVerticesData;
    private final float[] defaultTriangleVerticesData ;
    private final float[] scale = {1, 1};

    public TriangleVerticesCalculator() {
        defaultTriangleVerticesData = getDefaultTriangleVerticesData();
        triangleVerticesData= getDefaultTriangleVerticesData();
    }

    public TriangleVerticesCalculator(float[] triangleVerticesData) {
        defaultTriangleVerticesData = triangleVerticesData;
        this.triangleVerticesData = triangleVerticesData;
    }

    public float[] getScale_PreserveAspectFit(int angle, int widthIn, int heightIn, int widthOut, int heightOut) {
        scale[0] = scale[1] = 1;
        if (angle == 90 || angle == 270) {
            int cx = widthIn;
            widthIn = heightIn;
            heightIn = cx;
        }

        float aspectRatioIn = (float) widthIn / (float) heightIn;
        float heightOutCalculated = (float) widthOut / aspectRatioIn;

        if (heightOutCalculated < heightOut) {
            scale[1] = heightOutCalculated / heightOut;
        } else {
            scale[0] = heightOut * aspectRatioIn / widthOut;
        }

        return scale;
    }

    public float[] getScale_PreserveAspectCrop(int angle, int widthIn, int heightIn, int widthOut, int heightOut) {
        scale[0] = scale[1] = 1;
        if (angle == 90 || angle == 270) {
            int cx = widthIn;
            widthIn = heightIn;
            heightIn = cx;
        }

        float aspectRatioIn = (float) widthIn / (float) heightIn;
        float aspectRatioOut = (float) widthOut / (float) heightOut;

        if (aspectRatioIn > aspectRatioOut) {
            float widthOutCalculated = (float) heightOut * aspectRatioIn;
            scale[0] = widthOutCalculated / widthOut;
        } else {
            float heightOutCalculated = (float) widthOut / aspectRatioIn;
            scale[1] = heightOutCalculated / heightOut;
        }

        return scale;
    }

    public float[] getAspectRatioVerticesData(int widthIn, int heightIn, int widthOut, int heightOut) {
        System.arraycopy(defaultTriangleVerticesData, 0, triangleVerticesData, 0, defaultTriangleVerticesData.length);

        float aspectRatioIn = (float) widthIn / heightIn;
        float heightOutCalculated = (float) widthOut / aspectRatioIn;

        if (heightOutCalculated < heightOut) {
            float deltaHeight = (heightOut - heightOutCalculated) / heightOut;
            GetMinMax vertical = new GetMinMax(1).invoke();
            triangleVerticesData[vertical.getMin1()] += deltaHeight;
            triangleVerticesData[vertical.getMin2()] += deltaHeight;
            triangleVerticesData[vertical.getMax1()] -= deltaHeight;
            triangleVerticesData[vertical.getMax2()] -= deltaHeight;
        } else {
            float deltaWidth = (widthOut - heightOut * aspectRatioIn) / widthOut;
            GetMinMax horizontal = new GetMinMax(0).invoke();
            triangleVerticesData[horizontal.getMin1()] += deltaWidth;
            triangleVerticesData[horizontal.getMin2()] += deltaWidth;
            triangleVerticesData[horizontal.getMax1()] -= deltaWidth;
            triangleVerticesData[horizontal.getMax2()] -= deltaWidth;
        }

        return triangleVerticesData;
    }

    public static float[] getDefaultTriangleVerticesData() {
        return new float[] {
            //  X,     Y,    Z,    U,     V
            -1.0f, -1.0f, 0.0f, 0.00f, 0.00f,
            1.0f, -1.0f, 0.0f, 1.00f, 0.00f,
            -1.0f, 1.0f, 0.0f, 0.00f, 1.00f,
            1.0f, 1.0f, 0.0f, 1.00f, 1.00f,
        };
    }

    private class GetMinMax {
        private Pair<Integer, Float>[] array = new Pair[4];
        private int offset;

        GetMinMax(int offset) {
            this.offset = offset;
        }

        private int getOutIdx(int internalIdx) {
            return array[internalIdx].left * stride + offset;
        }

        public int getMax1() {
            return getOutIdx(2);
        }

        public int getMax2() {
            return getOutIdx(3);
        }

        public int getMin1() {
            return getOutIdx(0);
        }

        public int getMin2() {
            return getOutIdx(1);
        }


        public GetMinMax invoke() {
            for (int i = 0; i < 4; i++) {
                array[i] = new Pair(i, triangleVerticesData[offset + stride * i]);
            }
            Arrays.sort(array, new Comparator<Pair<Integer, Float>>() {
                @Override
                public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
                    return Float.compare(o1.right, o2.right);
                }
            });
            return this;
        }
    }
}
