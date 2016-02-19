package com.juzi.duotulockscreen.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.activity.PickImgActivity;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.ArrayList;

public class LockScreensGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<LockScreenImgBean> mData;
    private int mImgHeight;
    private int mImgWidth;
    private boolean mIsInEdit = false;

    //用hashmap实现checkbox的显示与隐藏,存储所有的checkBox对象
    private ArrayList<CheckBox> mCheckBoxes = new ArrayList<CheckBox>();

    private boolean[] mCheckedStatus; //一个数组用来记录某个位置是否是被选中的状态，如果被选中则为true，否则为false

    public LockScreensGalleryAdapter(Context context, ArrayList<LockScreenImgBean> data) {
        mContext = context;
        mData = data;
        mCheckedStatus = new boolean[mData.size()];

        mImgWidth = context.getResources().getDimensionPixelSize(R.dimen.grid_img_width);
        mImgHeight = context.getResources().getDimensionPixelSize(R.dimen.grid_img_height);
    }

    @Override
    public int getCount() {
        return mData.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mData.size()) {
            return 1;
        }
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        boolean isShowFadiIn;
        if (getItemViewType(position) == 1) {
            if (convertView == null) {
                convertView =  View.inflate(mContext, R.layout.activity_mylockscreenimgs_grid_item_add, null);
                convertView.findViewById(R.id.ib_item_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: 2016/2/17 点击添加图片
                        Intent intent = new Intent(mContext, PickImgActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            }
            return convertView;
        }

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.activity_mylockscreenimgs_grid_item, null);
            holder = new ViewHolder(convertView);
            holder.cbitem.setVisibility(View.INVISIBLE);
            convertView.setTag(holder);
            isShowFadiIn = true;
        } else {
            holder = (ViewHolder) convertView.getTag();
            isShowFadiIn = holder.oldPos != position;
        }
        holder.oldPos = position;
        holder.ivitemcontent.setTag(R.string.TAG_KEY_IS_FADEIN, isShowFadiIn);

        String url = mData.get(position).getImg_url();

        if (!mCheckBoxes.contains(holder.cbitem)) {
            mCheckBoxes.add(holder.cbitem);
        }

        holder.cbitem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCheckedStatus[position] = isChecked;
            }
        });

        if (mIsInEdit) {
            holder.cbitem.setVisibility(View.VISIBLE);
            holder.cbitem.setChecked(mCheckedStatus[position]);
        } else {
            holder.cbitem.setVisibility(View.INVISIBLE);
        }

        ImageLoaderManager.getInstance().asyncLoadImage(holder.ivitemcontent, "file://" + url, mImgWidth, mImgHeight);
//        ImageLoaderManager.getInstance().loadLocalPic(url, holder.ivitemcontent);
        return convertView;
    }

    private class ViewHolder {
        public int oldPos;
        public final CheckBox cbitem;
        public final ImageView ivitemcontent;
        public final TextView tvitemtitle;
        public final View root;

        public ViewHolder(View root) {
            cbitem = (CheckBox) root.findViewById(R.id.cb_item);
            ivitemcontent = (ImageView) root.findViewById(R.id.iv_item_content);
            tvitemtitle = (TextView) root.findViewById(R.id.tv_item_title);
            this.root = root;
        }
    }

    public void setIsInEdit(boolean isInEdit) {
        mIsInEdit = isInEdit;

        for (int i = 0; i < mCheckBoxes.size(); i++) {
            mCheckBoxes.get(i).setVisibility(isInEdit ? View.VISIBLE : View.INVISIBLE);
            mCheckBoxes.get(i).setChecked(false);
        }

        if (isInEdit) {
            for (int i = 0; i < mCheckedStatus.length; i++) {
                mCheckedStatus[i] = false;
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        mIsInEdit = false;
        mCheckBoxes.clear();
        mCheckedStatus = new boolean[mData.size()];
        super.notifyDataSetChanged();
    }

    public void setAllCkPick(boolean allPick) {

        for (int i = 0; i < mCheckBoxes.size(); i++) {
            mCheckBoxes.get(i).setChecked(allPick);
        }

        for (int i = 0; i < mCheckedStatus.length; i++) {
            mCheckedStatus[i] = allPick;
        }
    }

    public boolean isInEdit() {
        return mIsInEdit;
    }

    public boolean[] getCheckBoxStatus() {
        return mCheckedStatus;
    }
}
