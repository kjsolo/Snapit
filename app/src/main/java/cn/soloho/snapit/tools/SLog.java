package cn.soloho.snapit.tools;

import android.util.Log;

import cn.soloho.snapit.BuildConfig;

/**
 * Created by solo on 15/6/2.
 */
public class SLog {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static boolean sDebugThis;

    public static void debugThis() {
        sDebugThis = true;
    }

    public static void d(String tag, String msg) {
        if (DEBUG || sDebugThis) {
            sDebugThis = false;
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        Log.e(tag, msg, e);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable e) {
        Log.w(tag, msg, e);
    }
}
