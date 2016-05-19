package com.juzi.duotulockscreen.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.bean.PickImgBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.ArrayList;
import java.util.List;

public class PickImgGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private List<PickImgBean> mData;
    private List<PickImgBean> mCheckedImage;
    private int mItemWidth = 0, mItemHeight = 0;
    private List<ViewHolder> mAllHolders = new ArrayList<>();

    public PickImgGalleryAdapter(Context context, List<PickImgBean> data, List<PickImgBean> checkedImage) {
        mContext = context;
        mData = data;
        mCheckedImage = checkedImage;
        mItemWidth = context.getResources().getDimensionPixelSize(R.dimen.pickimg_grid_img_width);
        mItemHeight = context.getResources().getDimensionPixelSize(R.dimen.pickimg_grid_img_height);
    }

    public void setData(List<PickImgBean> data) {
        mData = data;
    }

    public List<PickImgBean> getCheckedImage() {
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
                convertView.setTag(holder);
                mAllHolders.add(holder);
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
        PickImgBean bean = mData.get(position);

        //改变选中状态时用，记录位置
        holder.pos = position;
        boolean isChecked = mCheckedImage.contains(bean);

        if (isChecked) {
            holder.mTvChose.setVisibility(View.VISIBLE);
        }else{
            holder.mTvChose.setVisibility(View.GONE);
        }

        final String path = bean.getImg_url();
        ImageLoaderManager.getInstance().asyncLoadImage(holder.mImageView, "file://" + path, mItemWidth, mItemHeight);
        return convertView;
    }

    private class ViewHolder {
        public ImageView mImageView;
        public TextView mTvChose;
        public int pos;

        public ViewHolder(View root) {
            mImageView = (ImageView) root.findViewById(R.id.iv_pickimage_item);
            mTvChose = (TextView) root.findViewById(R.id.tv_pickimg_cuted);
        }
    }

//    public void updataCheckedItem(Handler handler) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                for (int j = 0; j < mAllHolders.size(); j++) {
//                    ViewHolder viewHolder = mAllHolders.get(j);
//                    viewHolder.mShadow.setVisibility(View.GONE);
//                    viewHolder.mIvChose.setSelected(false);
//                }
//                for (int i = 0; i < mCheckedImage.size(); i++) {
//                    PickImgBean pickImgBean = mCheckedImage.get(i);
//                    for (int j = 0; j < mAllHolders.size(); j++) {
//                        ViewHolder viewHolder = mAllHolders.get(j);
//                        if (viewHolder.pos >= mData.size()) {
//                            continue;
//                        }
//                        if (mData.get(viewHolder.pos) == pickImgBean) { //说明这个textview正在显示中，需要更新
//                            viewHolder.mShadow.setVisibility(View.VISIBLE);
//                            viewHolder.mIvChose.setSelected(true);
//                        }
//                    }
//                }
//            }
//        });
//    }
}
