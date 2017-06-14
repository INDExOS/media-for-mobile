package org.m4m.android;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;


public class Utils {
    //private final String TAG = "UTILS";

    public static int getVideoRotationDegrees(Context context, Uri videoUri) {
        if(context == null || videoUri == null) {
            //Log.e(TAG, message);
            return 0;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
        } catch (IllegalArgumentException | SecurityException e) {
            //Log.e(TAG, message);
            return 0;
        } catch (RuntimeException e) {
            //Log.e(TAG, message);
            return 0;
        }

        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (rotation == null) {
            //Log.e(TAG, message);
            return 0;
        }
        return Integer.parseInt(rotation);
    }
}
