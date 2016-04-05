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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class TriangleVerticesCalculatorTest {

    private final float[] verticalBlackBars = new float[] {
            //  X,      Y,     Z,     U,     V
            -1.00f, -0.75f, 0.00f, 0.00f, 0.00f,
            1.00f, -0.75f, 0.00f, 1.00f, 0.00f,
            -1.00f, 0.75f, 0.00f, 0.00f, 1.00f,
            1.00f, 0.75f, 0.00f, 1.00f, 1.00f,
    };

    private final float[] horizontalBlackBars = new float[] {
        //  X,      Y,     Z,     U,     V
        -0.75f, -1.00f, 0.00f, 0.00f, 0.00f,
        0.75f, -1.00f, 0.00f, 1.00f, 0.00f,
        -0.75f, 1.00f, 0.00f, 0.00f, 1.00f,
        0.75f, 1.00f, 0.00f, 1.00f, 1.00f,
    };

    private final float[] defaultData = new float[] {
        //  X,     Y,    Z,    U,     V
        -1.0f, -1.0f, 0.0f, 0.00f, 0.00f,
        1.0f, -1.0f, 0.0f, 1.00f, 0.00f,
        -1.0f, 1.0f, 0.0f, 0.00f, 1.00f,
        1.0f, 1.0f, 0.0f, 1.00f, 1.00f,
    };

    private final float[] verticalBlackBarsRotated90 = new float[] {
          //    X,     Y,  Z,   U,   V
            -1.0f,  1.0f, 0, 0.f, 0.f,
            -1.0f, -1.0f, 0, 1.f, 0.f,
             1.0f,  1.0f, 0, 0.f, 1.f,
             1.0f, -1.0f, 0, 1.f, 1.f,
    };

    private final float[] verticalBlackBarsRotated90ArCorrected = new float[] {
            //    X,     Y,  Z,   U,   V
           -0.5f,  1.0f, 0, 0.f, 0.f,
           -0.5f, -1.0f, 0, 1.f, 0.f,
            0.5f,  1.0f, 0, 0.f, 1.f,
            0.5f, -1.0f, 0, 1.f, 1.f,
    };


    @Test
    public void testScaleHorizontally() {
        int inWidth = 10;
        int inheight = 5;

        int outWidth = 20;
        int outheight = 5;

        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();
        float scale [] = calculator.getScale_PreserveAspectFit(0, inWidth, inheight, outWidth, outheight);
        assertAlmostEqual(scale, new float[] {0.5f, 1f}, 1e-7);
    }

    @Test
    public void testScaleVertically() {
        int inWidth = 10;
        int inheight = 5;

        int outWidth = 5;
        int outheight = 20;

        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();
        float scale [] = calculator.getScale_PreserveAspectFit(0, inWidth, inheight, outWidth, outheight);
        assertAlmostEqual(scale, new float[] {1f, 0.125f}, 1e-7);
    }

    @Test
    public void testScaleHorizontallyThenVertically() {
        int inWidth = 10;
        int inheight = 5;

        int outWidth = 20;
        int outheight = 5;

        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();
        float scale [] = calculator.getScale_PreserveAspectFit(0, inWidth, inheight, outWidth, outheight);
        assertAlmostEqual(scale, new float[] {0.5f, 1f}, 1e-7);

        inWidth = 10;
        inheight = 5;

        outWidth = 5;
        outheight = 20;

        scale = calculator.getScale_PreserveAspectFit(0, inWidth, inheight, outWidth, outheight);
        assertAlmostEqual(scale, new float[] {1f, 0.125f}, 1e-7);
    }

    @Test
    public void testScaleVerticallyRotate270() {
        int inWidth = 10;
        int inheight = 5;

        int outWidth = 5;
        int outheight = 20;

        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();
        float scale [] = calculator.getScale_PreserveAspectFit(270, inWidth, inheight, outWidth, outheight);
        assertAlmostEqual(scale, new float[] {1f, 0.5f}, 1e-7);
    }


    @Test
    public void testVerticalBlackBarsWhenRotate90() {
        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator(verticalBlackBarsRotated90);

        float[] verticesData = calculator.getAspectRatioVerticesData(5, 10, 10, 10);

        assertAlmostEqual(verticesData, verticalBlackBarsRotated90ArCorrected, 1e-7);
    }

    @Test
    public void testVerticalBlackBars() {
        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();

        float[] verticesData = calculator.getAspectRatioVerticesData(1280, 720, 640, 480);

        assertAlmostEqual(verticesData, verticalBlackBars, 0.01);
    }

    @Test
    public void testHorizontalBlackBars() {
        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();

        float[] verticesData = calculator.getAspectRatioVerticesData(640, 480, 1280, 720);

        assertAlmostEqual(verticesData, horizontalBlackBars, 0.01);
    }

    @Test
    public void testHorizontalThanVerticalBlackBars() {
        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();

        float[] verticesData = calculator.getAspectRatioVerticesData(1280, 720, 640, 480);

        assertAlmostEqual(verticesData, verticalBlackBars, 0.01);

        verticesData = calculator.getAspectRatioVerticesData(640, 480, 1280, 720);

        assertAlmostEqual(verticesData, horizontalBlackBars, 0.01);
    }

    @Test
    public void testDefaultData() {
        TriangleVerticesCalculator calculator = new TriangleVerticesCalculator();

        float[] verticesData = calculator.getDefaultTriangleVerticesData();

        assertAlmostEqual(verticesData, defaultData, 0.01);
    }

    private void assertAlmostEqual(float[] verticesData, float[] expected, double delta) {
        for (int i = 0; i < expected.length; i++) {
            Assert.assertThat("Element " + i + " doesn't match.", (double) verticesData[i], is(closeTo((double) expected[i], delta)));
        }
    }
}
