package com.juzi.duotulockscreen.lockscreen;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.ArrayList;

public class LockScreenAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<LockScreenImgBean> mScreenImgBeans = new ArrayList<LockScreenImgBean>();

    public LockScreenAdapter(Context context, ArrayList<LockScreenImgBean> screenImgBeans) {
        mScreenImgBeans = screenImgBeans;
        mContext = context;
    }

    @Override
    public int getCount() {
        if (mScreenImgBeans == null || mScreenImgBeans.size() == 0) {
            return 0;
        } else if (mScreenImgBeans.size() == 1) {
            return 1;
        }
        return Integer.MAX_VALUE; //伪无限循环，目前最好的实现无限循环的方式，简单高效符合MVC设计模式
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int acturlPos = position % mScreenImgBeans.size(); //第几页，范围就是0-用户的锁屏图片数

        LockView lockView = new LockView(mContext);
        lockView.setScaleType(ImageView.ScaleType.FIT_XY);
        lockView.setOnUnLockListener(mL);
        lockView.setOnClickListener(mOnClickListener);
        LockScreenImgBean bean = mScreenImgBeans.get(acturlPos);

        if (lockView.getParent() != null) {
            ((ViewGroup) lockView.getParent()).removeView(lockView);
        }
        container.addView(lockView);
        if (bean.getIsDeflaut() == 0) {
            ImageLoaderManager.getInstance().loadLocalPic(bean.getImg_url(), lockView);
        } else {
            lockView.setImageResource(R.drawable.wallpaper_default);
        }
        return lockView;
    }

    private LockView.onUnLockListener mL;
    public void setOnUnLockListener(LockView.onUnLockListener l) {
        mL = l;
    }

    private View.OnClickListener mOnClickListener;
    public void setOnImgClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public int getItemPosition(Object object) {
//        return super.getItemPosition(object);
        return POSITION_NONE;
    }
}
