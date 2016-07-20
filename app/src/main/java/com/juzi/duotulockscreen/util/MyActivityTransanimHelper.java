package com.juzi.duotulockscreen.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.juzi.duotulockscreen.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;

/**
 * Created by wangzixu on 2016/7/18.
 */
public class MyActivityTransanimHelper {
    private static MyActivityTransanimHelper sInstance;
    private long mDuration = 400;
    private MyActivityTransanimHelper() {
    }
    /**
     * 单例
     */
    public static MyActivityTransanimHelper getInstance() {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new MyActivityTransanimHelper();
                }
            }
        }
        return sInstance;
    }

    public MyActivityTransanimHelper setDuration(long duration) {
        mDuration = duration;
        return sInstance;
    }

    private HashMap<String, ImageView> mTargetViewHashMap = new HashMap<>();
    private HashMap<String, View> mTargetViewBgHashMap = new HashMap<>();
    private HashMap<String, int[]> mTargetLocationHashMap = new HashMap<>();
    private HashMap<String, Integer> mOriPosHashMap = new HashMap<>();
    private HashMap<String, Point> mOriDeltaXYHashMap = new HashMap<>();

    public void startActivity(Activity activity, Intent intent, ImageView shareImageView, String name, int pos, Point deltaXy) {
        initIntent(activity, intent, shareImageView, name, pos, deltaXy);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, R.anim.activity_retain);
    }

    public void startActivityForResult(Activity activity, Intent intent, int requestCode, ImageView shareImageView, String name, int pos, Point deltaXy) {
        initIntent(activity, intent, shareImageView, name, pos, deltaXy);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(0, R.anim.activity_retain);
    }

    private void initIntent(Activity activity, Intent intent, ImageView shareImageView, String name, int pos, Point deltaXy) {
        mOriPosHashMap.put(name, pos);
        mOriDeltaXYHashMap.put(name, deltaXy);
        int[] location = new int[2];
        shareImageView.getLocationInWindow(location);
//        Log.d("wangzixu", "startActivity location = " + location[0] + ", " + location[1]
//                + ", " + img.getWidth() + ", " + img.getHeight()
//        );
        intent.putExtra("left", location[0]);
        intent.putExtra("top", location[1]);
        intent.putExtra("w", shareImageView.getWidth());
        intent.putExtra("h", shareImageView.getHeight());
//        Drawable drawable1 = shareImageView.getDrawable();
//        Drawable.ConstantState constantState = drawable1.getConstantState();
//        Drawable drawable = constantState.newDrawable();

        shareImageView.setDrawingCacheEnabled(true);
//        Bitmap drawingCache = Bitmap.createBitmap(shareImageView.getDrawingCache());
        shareImageView.destroyDrawingCache();
        Bitmap drawingCache = shareImageView.getDrawingCache();
//        shareImageView.setDrawingCacheEnabled(false);
        ImageUtil.changeLight(shareImageView, true);
        final ImageView mView = new ImageView(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(shareImageView.getWidth(), shareImageView.getHeight());
        mView.setLayoutParams(lp);
        mView.setPivotX(0);
        mView.setPivotY(0);
        mView.setImageBitmap(drawingCache);
        mTargetViewHashMap.put(name, mView);
    }

    public void setTransitionImageView(final Activity activity, final View targetView, final String name) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }

        final int fromLeft = intent.getIntExtra("left", 0);
        final int fromTop = intent.getIntExtra("top", 0);
        final int fromW = intent.getIntExtra("w", 0);
        final int fromH = intent.getIntExtra("h", 0);

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ImageView mView = mTargetViewHashMap.get(name);
        decorView.addView(mView, 0);
//        activity.getWindow().addContentView(mView, lp);
        mView.setTranslationX(fromLeft);
        mView.setTranslationY(fromTop);

        final View bgView = decorView.findViewById(Window.ID_ANDROID_CONTENT);
        mTargetViewBgHashMap.put(name, bgView);
//        ViewGroup contentP = (ViewGroup) activity.getWindow().getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
//        final View bgView = contentP.getChildAt(0);

        targetView.setVisibility(View.INVISIBLE);
        targetView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                targetView.removeOnLayoutChangeListener(this);
                int[] location = new int[2];
                targetView.getLocationInWindow(location);
//                Log.d("wangzixu", "setTransitionImageView location = " + location[0] + ", " + location[1]);

                int toLeft = location[0];
                int toTop = location[1];
                int toW = targetView.getWidth();
                int toH = targetView.getHeight();

                int[] pos = new int[4];
                pos[0] = toLeft;
                pos[1] = toTop;
                pos[2] = toW;
                pos[3] = toH;
                mTargetLocationHashMap.put(name, pos);

                final int deltaLeft = toLeft - fromLeft;
                final int deltaTop = toTop - fromTop;
                final float deltaScaleX = (float)toW / fromW - 1.0f;
                final float deltaScaleY = (float)toH / fromH - 1.0f;

                final ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
                animator.setDuration(mDuration);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float f = (float) animation.getAnimatedValue();
                        mView.setScaleX(1.0f + f * deltaScaleX);
                        mView.setScaleY(1.0f + f * deltaScaleY);
                        mView.setTranslationX(fromLeft + f * deltaLeft);
                        mView.setTranslationY(fromTop + f * deltaTop);
                        bgView.setAlpha(f);
                        mView.invalidate();
                    }
                });

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        targetView.setVisibility(View.VISIBLE);
                        mView.setVisibility(View.GONE);
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                }, 0);
            }
        });
    }

    public void exit(final Activity activity, final View targetView, String name, int currentPos) {
        final ImageView mView = mTargetViewHashMap.get(name);
        mTargetViewHashMap.remove(name);
        final View bgView = mTargetViewBgHashMap.get(name);
        mTargetViewBgHashMap.remove(name);
        int[] targetPos = mTargetLocationHashMap.get(name);
        mTargetLocationHashMap.remove(name);
        int oriPos = mOriPosHashMap.get(name);
        mOriPosHashMap.remove(name);
        Point point = mOriDeltaXYHashMap.get(name);
        mOriDeltaXYHashMap.remove(name);

        Intent intent = activity.getIntent();
        int deltaX, deltaY = 0;
        if (point.y == 0) { //一行展示，没有换行逻辑
            deltaX = (currentPos - oriPos) * point.x;
            deltaY = 0;
        } else { //有换行逻辑，暂时定位3个一行，以后需要用户传入
            deltaX = (currentPos % 3 - oriPos % 3) * point.x;
            deltaY = (currentPos / 3 - oriPos / 3) * point.y;
        }
        final int fromLeft = intent.getIntExtra("left", 0) + deltaX;
        final int fromTop = intent.getIntExtra("top", 0) + deltaY;
        final int fromW = intent.getIntExtra("w", 0);
        final int fromH = intent.getIntExtra("h", 0);


        final int deltaLeft = targetPos[0] - fromLeft;
        final int deltaTop = targetPos[1] - fromTop;
        final float deltaScaleX = (float)targetPos[2] / fromW - 1.0f;
        final float deltaScaleY = (float)targetPos[3] / fromH - 1.0f;

        targetView.setDrawingCacheEnabled(true);
        Bitmap drawingCache = targetView.getDrawingCache();
//        Bitmap drawingCache = Bitmap.createBitmap(targetView.getDrawingCache());
//        targetView.setDrawingCacheEnabled(false);
        mView.setImageBitmap(drawingCache);

        final ValueAnimator mAnimatorOut = ValueAnimator.ofFloat(1.0f, 0);
        mAnimatorOut.setDuration(mDuration);
        mAnimatorOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                mView.setScaleX(1.0f + f * deltaScaleX);
                mView.setScaleY(1.0f + f * deltaScaleY);
                mView.setTranslationX(fromLeft + f * deltaLeft);
                mView.setTranslationY(fromTop + f * deltaTop);
                bgView.setAlpha(f);
                mView.invalidate();
            }
        });

        mAnimatorOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mView.setVisibility(View.GONE);
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mView.setVisibility(View.VISIBLE);
                targetView.setVisibility(View.INVISIBLE);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAnimatorOut.start();
            }
        }, 0);
    }

}
