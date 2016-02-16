package com.juzi.duotulockscreen.util;

import android.util.Log;

public class LogHelper {
    public static boolean DEBUG = false;

    public static void i(String TAG, String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String TAG, String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    // 使用Log来显示调试信息,因为log在实现上每个message有4k字符长度限制
    // 所以这里使用自己分节的方式来输出足够长度的message
    public static void iLongLog(String TAG, String str) {
        if (DEBUG) {
            str = str.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < str.length()) {
                // java的字符不允许指定超过总的长度end
                if (str.length() <= index + maxLength) {
                    sub = str.substring(index);
                } else {
                    sub = str.substring(index, index + maxLength);
                }

                index += maxLength;
                Log.i(TAG, sub);
            }
        }
    }
}
