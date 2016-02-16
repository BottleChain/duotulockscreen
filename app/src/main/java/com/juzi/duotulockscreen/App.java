package com.juzi.duotulockscreen;

import android.app.Application;

import com.juzi.duotulockscreen.util.ImageLoaderManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //MobclickAgent.setDebugMode(true);
        ImageLoaderManager.initImageLoader(getApplicationContext()); //初始化imageLoader
    }
}
