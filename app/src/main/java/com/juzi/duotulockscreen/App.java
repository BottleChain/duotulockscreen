package com.juzi.duotulockscreen;

import android.app.Application;

import com.juzi.duotulockscreen.bean.PickImgBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.ArrayList;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //MobclickAgent.setDebugMode(true);
        ImageLoaderManager.initImageLoader(getApplicationContext()); //初始化imageLoader
    }

    //图库选择图片时，点击大图时预览，预览大图时可以改变选中的状态，
    //为了方便，使这两个activity公用一套数据，以便于一个界面改变了数据中选中状态另一边可以直接拿来用，
    //所以用app来做为了中转，持有了数据集合的引用。
    //目前为了方便，所有跳转到大图预览的activity负责给这个data赋值，大图activity直接从这取就可以，
    // 注意！！！--- 给这个data赋值的activity在销毁时一定负责置空此引用，减少不必要的内存开销
    private ArrayList<PickImgBean> mBigImgData;
    private ArrayList<PickImgBean> mCheckedImgs;

    public ArrayList<PickImgBean> getBigImgData() {
        return mBigImgData;
    }

    public void setBigImgData(ArrayList<PickImgBean> bigImgData) {
        mBigImgData = bigImgData;
    }

    public ArrayList<PickImgBean> getCheckedImgs() {
        return mCheckedImgs;
    }

    public void setCheckedImgs(ArrayList<PickImgBean> checkedImgs) {
        mCheckedImgs = checkedImgs;
    }
}
