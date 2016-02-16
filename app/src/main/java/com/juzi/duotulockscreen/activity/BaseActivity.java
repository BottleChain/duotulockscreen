package com.juzi.duotulockscreen.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                    window.setFlags(
                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }
    }

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); //友盟统计基础
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this); //友盟统计基础
    }
}
