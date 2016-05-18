package com.juzi.duotulockscreen.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.util.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends FragmentActivity {
    protected SystemBarTintManager mSystemBarTintManager;
    protected boolean mIsDestory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

//                    window.setFlags(
//                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
//                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            if (!getIsFullScreen()) {
                mSystemBarTintManager = new SystemBarTintManager(this);
                // 设置状态栏状态
                mSystemBarTintManager.setStatusBarTintEnabled(true);
                // 设置状态栏颜色
                mSystemBarTintManager.setStatusBarTintColor(getResources().getColor(R.color.actionbar_bg_color));
            }
        }
    }

    /**
     * 是否是全屏界面，如果不是全屏，就会添加一个半透明的状态栏背景，如果是全屏，只是设置状态栏为透明
     * @return
     */
    protected boolean getIsFullScreen() {
        return false;
    }

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); //友盟统计基础
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this); //友盟统计基础
    }

    @Override
    protected void onDestroy() {
        System.gc();
        mIsDestory = true;
        super.onDestroy();
    }
}
