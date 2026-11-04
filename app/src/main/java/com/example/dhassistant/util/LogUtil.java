package com.example.dhassistant.util;

import android.util.Log;

/**
 * 统一日志工具，方便后期按 TAG 过滤自动化日志。
 */
public final class LogUtil {

    private static final String TAG = "DHAssistant";

    private LogUtil() {
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable t) {
        Log.e(TAG, msg, t);
    }
}
