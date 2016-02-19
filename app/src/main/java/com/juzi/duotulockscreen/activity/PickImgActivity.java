package com.juzi.duotulockscreen.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.adapter.PickImgGalleryAdapter;
import com.juzi.duotulockscreen.bean.LocalPictureBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PickImgActivity extends BaseActivity implements View.OnClickListener {
    private GridView mGvGridview;
    private RelativeLayout mRlBottomBar;
    private TextView mTvPickimageBottomDirname;
    private HashMap<String, ArrayList<LocalPictureBean>> mImgDirs = new HashMap<String, ArrayList<LocalPictureBean>>();
    private ArrayList<LocalPictureBean> mData = new ArrayList<>();
    private ArrayList<LocalPictureBean> mCheckedImgs = new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();
    private PopupWindow mPopWindow;

    /**
     * 读取手机中所有图片资源的url
     */
    final static Uri LOCAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private PickImgGalleryAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomActionBar();
        setContentView(R.layout.activity_priview);
        assignViews();

        loadData();
    }

    private void initPopwindow() {
        if (mPopWindow == null) {
            View content = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pickimg_popcontent_layout, null);
            ListView lv = (ListView) content.findViewById(R.id.listview_popwindow);
            Adapter ada = new PopupListAdapter();
            lv.setAdapter((ListAdapter) ada);
            lv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCurrentDir = position;
                    mPopWindow.dismiss();
                    Holder holder = (Holder) view.getTag();
                    mTvBottom.setText(holder.title.getText());
                    tv_home.setText(holder.title.getText());
                    mAdapter.setData(mImageDirs.get(position));
                    mAdapter.notifyDataSetChanged();
                }
            });
            mPopWindow = new PopupWindow(content, LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7f), true);
            mPopWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopWindow.setOutsideTouchable(true);
            mPopWindow.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    // 设置背景颜色变亮
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1.0f;
                    getWindow().setAttributes(lp);
                }
            });
        }
    }

    /**
     * 加载手机中所有的图片
     */
    private void loadData() {
        mProgressDialog = ProgressDialog.show(this, null, "读取图片中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = PickImgActivity.this.getContentResolver();
                Cursor mCursor = contentResolver.query(LOCAL_IMAGE_URI, null, MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?", new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED
                        + " DESC");
                if (mCursor == null) {
                    mProgressDialog.dismiss();
                    Toast.makeText(PickImgActivity.this, "读取图片失败！", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    while (mCursor.moveToNext()) {
                        LocalPictureBean bean = new LocalPictureBean();
                        // 获取图片路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        bean.setPath(path);
                        bean.setIsChecked(false);
                        mData.add(bean);

                        // 获取父文件夹路径
                        String parentName = new File(path).getParentFile().getName();
                        if (mImgDirs.containsKey(parentName)) {
                            mImgDirs.get(parentName).add(bean);
                        } else {
                            ArrayList<LocalPictureBean> list = new ArrayList<LocalPictureBean>();
                            list.add(bean);
                            mImgDirs.put(parentName, list);
                        }
                    }
                    mImgDirs.put("全部图片", mData);
                } finally {
                    mImgDirs.clear();
                    mCursor.close();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void assignViews() {
        mGvGridview = (GridView) findViewById(R.id.gv_gridview);
        mRlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
        mTvPickimageBottomDirname = (TextView) findViewById(R.id.tv_pickimage_bottom_dirname);

        mAdapter = new PickImgGalleryAdapter(this, mData, mCheckedImgs);
        mGvGridview.setAdapter(mAdapter);
    }

    //自定义actionbar
    private void initCustomActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            return;
        }

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.preview_activity_top_bar);

        View ivTopBarLeft = getActionBar().getCustomView().findViewById(R.id.iv_top_bar_left);
        ivTopBarLeft.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    private class PopupListAdapter extends BaseAdapter {
        private int sItemWidth = 0, sItemHeight = 0;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.gallery_pickimg_popcontent_item, null);
                holder = new Holder();
                holder.leftImg = (ImageView) convertView.findViewById(R.id.imageview_gallery_pickimg_pop_item);
                holder.rightImg = (ImageView) convertView.findViewById(R.id.image_gallery_pickimg_pop_itemcheck);
                holder.title = (TextView) convertView.findViewById(R.id.textview_gallery_pickimg_pop_item_name);
                holder.count = (TextView) convertView.findViewById(R.id.textview_gallery_pickimg_pop_item_count);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            ArrayList<LocalPictureBean> list = mImageDirs.get(position);
            LocalPictureBean first = list.get(0);
            final String path = first.getPath();
            // 加载左侧icon图片
            if (sItemHeight == 0 || sItemWidth == 0) {
                final ImageView imageView = holder.leftImg;
                holder.leftImg.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        sItemHeight = imageView.getHeight();
                        sItemWidth = imageView.getWidth();
                        imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        MyGalleryLocalImageLoader.getInstance().loadImage(path, imageView, MyGalleryLocalImageLoader.TYPE_TUMBSNAIL, sItemHeight,
                                sItemWidth);
                    }
                });
            } else {
                MyGalleryLocalImageLoader.getInstance().loadImage(path, holder.leftImg, MyGalleryLocalImageLoader.TYPE_TUMBSNAIL, sItemHeight,
                        sItemWidth);
            }

            // 数量
            holder.count.setText(String.format(getString(R.string.mapdepot_name_number), list.size()));
            // 文件夹名称
            if (position == 0) {
                holder.title.setText(R.string.mygallery_pickimage_pupwindow_firsttitle);
            } else {
                String parentPath = new File(path).getParentFile().getName();
                String title = parentPath.substring(parentPath.lastIndexOf("/") + 1);
                holder.title.setText(title);
            }
            // 是否显示选中框
            if (position == mCurrentDir) {
                holder.rightImg.setVisibility(View.VISIBLE);
            } else {
                holder.rightImg.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mImageDirs.size();
        }
    }
}
