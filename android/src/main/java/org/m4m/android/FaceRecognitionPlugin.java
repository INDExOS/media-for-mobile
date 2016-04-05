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

package org.m4m.android;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import org.m4m.IRecognitionPlugin;

import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionPlugin implements IRecognitionPlugin {
    private static final int MAX_FACES = 10;

    private FaceDetector faceDetector;

    private int lastBitmapWidth;
    private int lastBitmapHeight;

    public class Face {
        private int mId;

        private float mConfidence;
        private float mMidPointX;
        private float mMidPointY;
        private float mEyeDistance;

        private Face() {
        }

        public int getId() {
            return mId;
        }

        public void getBounds(RectF bounds) {
            bounds.set(mMidPointX - 20, mMidPointY - 20, mMidPointX + 20, mMidPointY + 20);
        }

        public float getConfidence() {
            return mConfidence;
        }

        public void getMidPoint(PointF midPoint) {
            midPoint.set(mMidPointX, mMidPointY);
        }

        public float getEyeDistance() {
            return mEyeDistance;
        }
    }

    static public class FaceRecognitionInput extends RecognitionInput {
        private Bitmap bitmap;

        public FaceRecognitionInput(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }

    static public class FaceRecognitionOutput extends RecognitionOutput {
        private List<Face> facesList;

        public FaceRecognitionOutput() {
            facesList = new ArrayList<Face>();
        }

        public void add(Face face) {
            facesList.add(face);
        }

        public int size() {
            return facesList.size();
        }

        public Face get(int index) {
            return facesList.get(index);
        }
    }

    @Override
    public void start() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RecognitionOutput recognize(RecognitionInput input) {
        FaceRecognitionInput recognitioInput = null;

        if (input instanceof FaceRecognitionInput) {
            recognitioInput = (FaceRecognitionInput) input;
        } else {
            throw new IllegalArgumentException("Invalid arguments.");
        }

        Bitmap bitmap = recognitioInput.getBitmap();

        if (lastBitmapWidth != bitmap.getWidth() || lastBitmapHeight != bitmap.getHeight()) {
            lastBitmapWidth = bitmap.getWidth();
            lastBitmapHeight = bitmap.getHeight();

            faceDetector = new FaceDetector(lastBitmapWidth, lastBitmapHeight, MAX_FACES);
        }

        FaceRecognitionOutput result = new FaceRecognitionOutput();

        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];

        int facesDetected = faceDetector.findFaces(bitmap, faces);

        PointF midPoint = new PointF();

        for (int i = 0; i < facesDetected; i++) {
            Face face = new Face();

            face.mId = 0;

            face.mConfidence = faces[i].confidence();
            face.mEyeDistance = faces[i].eyesDistance();

            faces[i].getMidPoint(midPoint);

            face.mMidPointX = midPoint.x;
            face.mMidPointY = midPoint.y;

            result.add(face);
        }

        return result;
    }
}
