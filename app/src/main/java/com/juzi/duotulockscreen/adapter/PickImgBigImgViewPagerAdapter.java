package com.juzi.duotulockscreen.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.juzi.duotulockscreen.bean.PickImgBean;
import com.juzi.duotulockscreen.view.ZoomImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.List;

public class PickImgBigImgViewPagerAdapter extends PagerAdapter {
    private List<PickImgBean> mData;
    private Context mContext;
    private List<ZoomImageView> mViewsList;
    //private int mScreenW, mScreenH;

    public PickImgBigImgViewPagerAdapter(List<PickImgBean> data, Context context, List<ZoomImageView> viewsList) {
        mData = data;
        mContext = context;
        mViewsList = viewsList;
//        mScreenH = context.getResources().getDisplayMetrics().heightPixels;
//        mScreenW = context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int viewPosition = position % mViewsList.size();
        final ZoomImageView imageView = mViewsList.get(viewPosition);
        PickImgBean imageViewBean = mData.get(position);
        if (imageView.getParent() != null) {
            ((ViewGroup)imageView.getParent()).removeView(imageView);
        }
        imageView.setImageBitmap(null);
        container.addView(imageView);

        final String path = imageViewBean.getImg_url();
        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(path), imageView);
        return imageView;
    }
}
