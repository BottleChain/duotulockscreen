package com.juzi.duotulockscreen.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.bean.DemoImgBean;
import com.juzi.duotulockscreen.util.ImageLoaderManager;

import java.util.ArrayList;
import java.util.List;

public class PickImgOutOfOrderAdapter extends BaseAdapter {
    private Context mContext;
    private List<DemoImgBean> mCheckedImage;
    private ArrayList<ArrayList<DemoImgBean>> mItems;

    public PickImgOutOfOrderAdapter(Context context, ArrayList<ArrayList<DemoImgBean>> items, List<DemoImgBean> checkedImage) {
        mContext = context;
        mItems = items;
        mCheckedImage = checkedImage;
    }

    @Override
    public int getCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(ArrayList<ArrayList<DemoImgBean>> items) {
        mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.activity_pickimg_outoforder_item, null);
        }

        RelativeLayout p = (RelativeLayout)convertView;
        p.removeAllViews();
        ArrayList<DemoImgBean> item = mItems.get(position);
        for (int i = 0; i < item.size(); i++) {
            DemoImgBean bean = item.get(i);
            ViewGroup content = (ViewGroup)View.inflate(mContext, R.layout.activity_pickimg_outoforder_item_img, null);
            ImageView img = (ImageView) content.findViewById(R.id.iv_pick_girdimg);
            View mask = content.findViewById(R.id.mask);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(bean.getItemWidth(), bean.getItemHeigh());
            lp.leftMargin = bean.getMarginLeft();
//            Log.d("wangzixu", "getView pos acturlItemW,H = " + position
//                    + ", " + bean.getItemWidth()
//                    + ", " + bean.getItemHeigh()
//                    + ", " + bean.getMarginLeft());
            content.setLayoutParams(lp);
            content.setTag(bean);
            content.setId(R.id.rl_pick_girdimg);
            content.setOnClickListener((View.OnClickListener) mContext);
            if (mCheckedImage.contains(bean)) {
                mask.setVisibility(View.VISIBLE);
            } else {
                mask.setVisibility(View.GONE);
            }
            p.addView(content);
            String path = bean.getImg_url();
            ImageLoaderManager.getInstance().asyncLoadImage(img, "file://" + path, bean.getItemWidth(), bean.getItemHeigh());
        }
        return convertView;
    }

}
