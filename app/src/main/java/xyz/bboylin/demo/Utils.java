package xyz.bboylin.demo;

import android.util.Log;

/**
 * Created by bboylin on 2019-11-05.
 */
public class Utils {
    private static final String TAG = "Synth";

    public static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }

    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    public static void log(Object obj) {
        Log.d(TAG, String.valueOf(obj));
    }
}
