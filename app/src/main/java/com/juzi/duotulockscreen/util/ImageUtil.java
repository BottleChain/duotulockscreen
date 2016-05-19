package com.juzi.duotulockscreen.util;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.widget.ImageView;

public class ImageUtil {

    /**
     * 改变图片的亮度，点击图片变暗
     * @param imageview
     * @param toDark
     */
    public static void changeLight(final ImageView imageview, boolean toDark) {
        //方法一：用ColorMatrixColorFilter
        //        ColorMatrix matrix = new ColorMatrix();
        //        matrix.set(new float[] {
        //                1, 0, 0, 0, brightness,
        //                0, 1, 0, 0, brightness,
        //                0, 0, 1, 0, brightness,
        //                0, 0, 0, 1, 0 });
        //        imageview.setColorFilter(new ColorMatrixColorFilter(matrix));
        //方法二：用 setColorFilter(Color.GRAY, Mode.MULTIPLY);
        if (toDark) {
            imageview.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
            imageview.postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeLight(imageview, false);
                }
            }, 30);
        } else {
            imageview.clearColorFilter();
        }
        imageview.invalidate();
    }
}
