package com.juzi.duotulockscreen.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.bean.PickImgBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.List;

public class PickImgGridAdapter extends BaseAdapter {
    private Context mContext;
    private List<PickImgBean> mData;
    private List<PickImgBean> mCheckedImage;
    private int mItemWidth = 0, mItemHeight = 0;
    private int mType; //类型，0为所有图片，要显示相机，1为其他文件夹，不显示相机
    private View mItem1; //条目1的view，拍完照片时要用

    public PickImgGridAdapter(Context context, List<PickImgBean> data, List<PickImgBean> checkedImage) {
        mContext = context;
        mData = data;
        mCheckedImage = checkedImage;
        mItemWidth = context.getResources().getDimensionPixelSize(R.dimen.pickimg_grid_img_width);
        mItemHeight = context.getResources().getDimensionPixelSize(R.dimen.pickimg_grid_img_height);
    }

    public void setType(int type) {
        mType = type;
    }

    public void setData(List<PickImgBean> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        if (mType == 0) {
            return mData.size() + 1;//位置0是照相机
        }
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        if (mType == 0) {
            return mData.get(position - 1);//位置0是照相机
        }
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (mType == 0) {
            return position - 1;//位置0是照相机
        }
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mType == 0) {
            return position == 0 ? 0 : 1;//位置0是照相机
        }
        return 1;
    }

    public View getItem1() {
        return mItem1;
    }

    public int getType() {
        return mType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        boolean shouldInitBitmap;
        if (convertView == null) {
            if (type != 0) {
                convertView = View.inflate(mContext, R.layout.activity_pickimg_girditem, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
                shouldInitBitmap = true;
            } else {
                convertView = View.inflate(mContext, R.layout.activity_pickimg_griditem_camera, null);
                return convertView;
            }
        } else {
            if (type != 0) {
                holder = (ViewHolder) convertView.getTag();
                shouldInitBitmap = holder.oldPos != position;
            } else {
                return convertView;
            }
        }
        holder.oldPos = position;
        if (position == 1) {
            mItem1 = convertView;
        }
        if (mType == 0) {
            position -= 1;
        }
        PickImgBean bean = mData.get(position);

        //改变选中状态时用，记录位置
        holder.pos = position;
        boolean isChecked = mCheckedImage.contains(bean);

        if (isChecked) {
            holder.mTvMask.setVisibility(View.VISIBLE);
        }else{
            holder.mTvMask.setVisibility(View.GONE);
        }

        final String path = bean.getImg_url();
        if (shouldInitBitmap) {
            holder.mImageView.setImageBitmap(null);
        }
        ImageLoaderManager.getInstance().displayImage("file://" + path
                , holder.mImageView, mItemWidth, mItemHeight, true);
        return convertView;
    }

    private class ViewHolder {
        public ImageView mImageView;
        public View mTvMask;
        public int pos; //用来记录选中状态用的位置
        public int oldPos; //用来记录是否要清空bitmap

        public ViewHolder(View root) {
            mImageView = (ImageView) root.findViewById(R.id.iv_pickimage_item);
            mTvMask = root.findViewById(R.id.mask);
        }
    }
}
