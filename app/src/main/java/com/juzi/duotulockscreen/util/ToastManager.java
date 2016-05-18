package com.juzi.duotulockscreen.util;

import android.content.Context;
import android.widget.Toast;

public class ToastManager {
	public static void showShort(Context c, String t) {
        if (c == null) {
            return;
        }
        Toast.makeText(c, t, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(Context c, int id) {
        if (c == null) {
            return;
        }
        Toast.makeText(c, id, Toast.LENGTH_SHORT).show();
    }

    public static void showCustomToast(Context c) {

    }
}
