package com.juzi.duotulockscreen.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.bean.LocalPictureBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.List;

public class PickImgGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private List<LocalPictureBean> mData;
    private List<LocalPictureBean> mCheckedImage;
    final private static int CAMERA_BG_COLOR = Color.DKGRAY;
    final private static int NORMAL_BG_COLOR = 0xFFDDDCD8;
    private int mItemWidth = 0, mItemHeight = 0;
    private OnCheckedImageCountChangeListener mCheckedCountChangeLister;
    private static final int MAX_COUNT = 20;

    public PickImgGalleryAdapter(Context context, List<LocalPictureBean> data, List<LocalPictureBean> checkedImage) {
        mContext = context;
        mData = data;
        mCheckedImage = checkedImage;
        mItemWidth = context.getResources().getDimensionPixelSize(R.dimen.pickimg_grid_img_width);
        mItemHeight = context.getResources().getDimensionPixelSize(R.dimen.pickimg_grid_img_height);
    }

    public void setData(List<LocalPictureBean> data) {
        mData = data;
    }

    public void setOnCheckedImageCountChangeListener(OnCheckedImageCountChangeListener listener) {
        mCheckedCountChangeLister = listener;
    }

    public List<LocalPictureBean> getCheckedImage() {
        return mCheckedImage;
    }

    @Override
    public int getCount() {
        return mData.size() + 1; //位置0是照相机
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position - 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            if (type != 0) {
                convertView = View.inflate(mContext, R.layout.item_pickimg, null);
                holder = new ViewHolder(convertView);
                holder.mCheckBox.setOnClickListener(new GridViewCbOnClickListener());
                convertView.setTag(holder);
            } else {
                convertView = View.inflate(mContext, R.layout.item_pickimg_camera, null);
                return convertView;
            }
        } else {
            if (type != 0) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                return convertView;
            }
        }

        position -= 1;
        LocalPictureBean bean = mData.get(position);
        //改变选中状态时用，记录位置
        holder.mCheckBox.setTag(position);
        holder.mCheckBox.setChecked(bean.isChecked());

        if (bean.isChecked()) {
            holder.mImageView.setColorFilter(mContext.getResources().getColor(R.color.hei_65));
        }else{
            holder.mImageView.setColorFilter(null);
        }

        final String path = bean.getPath();
        ImageLoaderManager.getInstance().asyncLoadImage(holder.mImageView, "file://" + path, mItemWidth, mItemHeight);
        return convertView;
    }

    private class ViewHolder {
        public ImageView mImageView;
        public CheckBox mCheckBox;

        public ViewHolder(View root) {
            mImageView = (ImageView) root.findViewById(R.id.iv_pickimage_item);
            mCheckBox = (CheckBox) root.findViewById(R.id.cb_pickimage_item);
        }
    }

    public interface OnCheckedImageCountChangeListener {
        void onCheckedImageCountChange(int checkCount);
    }

    private class GridViewCbOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            CheckBox cb = (CheckBox) v;
            boolean isChecked = cb.isChecked();
            // can not > 9 checked
            if (mCheckedImage.size() >= MAX_COUNT && isChecked) {
                cb.setChecked(false);
                Toast.makeText(mContext, "不能选中超过20个", Toast.LENGTH_SHORT).show();
                return;
            }

            // get the item position
            int p = 0;
            Object tag = cb.getTag();
            if (tag != null && tag instanceof Integer) {
                p = (Integer) tag;
            }

            LocalPictureBean viewBean = mData.get(p);
            if (viewBean == null) {
                return;
            }

            mData.get(p).setIsChecked(isChecked);
            if (isChecked && !mCheckedImage.contains(viewBean)) {
                mCheckedImage.add(viewBean);
            } else if (!isChecked) {
                mCheckedImage.remove(viewBean);
            }

            if (mCheckedCountChangeLister != null) {
                mCheckedCountChangeLister.onCheckedImageCountChange(mCheckedImage.size());
            }

            // set animation
            View parentItem = (View) cb.getParent();
            ImageView view = (ImageView) parentItem.findViewById(R.id.iv_pickimage_item);
            if (isChecked) {
                view.setColorFilter(mContext.getResources().getColor(R.color.hei_65));
            } else {
                view.setColorFilter(null);
            }
        }
    }
}
