package com.juzi.duotulockscreen.pickimg;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.activity.BaseActivity;
import com.juzi.duotulockscreen.util.ImageLoaderManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PickImgActivity extends BaseActivity implements View.OnClickListener, PickImgGalleryAdapter.OnCheckedImageCountChangeListener {
    private GridView mGvGridview;
    private RelativeLayout mRlBottomBar;
    private TextView mTvPickimageBottomDirname;
    private ArrayList<ArrayList<PickImgBean>> mImgDirs = new ArrayList<>();
    private ArrayList<PickImgBean> mData = new ArrayList<>();
    private ArrayList<PickImgBean> mCheckedImgs = new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();
    private PopupWindow mPopWindow;
    private int mCurrentDirPosition;

    /**
     * 读取手机中所有图片资源的url
     */
    final static Uri LOCAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private PickImgGalleryAdapter mAdapter;
    private View mPopBg;
    private ListView mPopLv;
    private View mRlPopContent;
    private Button mBtNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomActionBar();
        setContentView(R.layout.activity_priview);
        assignViews();

        loadData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_next:

                break;
            case R.id.iv_top_bar_left:
                onBackPressed();
                break;
            case R.id.shadow:
                if (mPopWindow.isShowing()) {
                    disMissPop();
                }
                break;
            case R.id.rl_bottom_bar:
            case R.id.tv_pickimage_bottom_dirname:
                initPopwindow();
                if (mPopWindow.isShowing()) {
                    disMissPop();
                } else {
                    mPopWindow.setClippingEnabled(false);
                    mPopWindow.showAtLocation(mRlBottomBar, Gravity.BOTTOM, 0, mRlBottomBar.getHeight());
                    mPopBg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.popupwindow_bg_in));
                    mRlPopContent.startAnimation(AnimationUtils.loadAnimation(this, R.anim.popupwindow_bottom_in));
                }
                break;
            default:
                break;
        }
    }

    private void initPopwindow() {
        if (mPopWindow == null) {
            View content = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pickimg_popcontent_layout, null);
            mRlPopContent = content.findViewById(R.id.rl_pop_content);
            mPopLv = (ListView) content.findViewById(R.id.listview_popwindow);
            mPopBg = content.findViewById(R.id.shadow);
            mPopBg.setOnClickListener(this);

            Adapter ada = new PopupListAdapter();
            mPopLv.setAdapter((ListAdapter) ada);
            mPopLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCurrentDirPosition = position;
                    mPopWindow.dismiss();
                    ViewHolder holder = (ViewHolder) view.getTag();
                    mTvPickimageBottomDirname.setText(holder.textviewgallerypickimgpopitemname.getText());
                    mAdapter.setData(mImgDirs.get(position));
                    mAdapter.notifyDataSetChanged();
                }
            });

            mPopWindow = new PopupWindow(content, AbsListView.LayoutParams.MATCH_PARENT
                    , getResources().getDisplayMetrics().heightPixels - mRlBottomBar.getHeight(), true);
//            mPopWindow = new PopupWindow(content, AbsListView.LayoutParams.MATCH_PARENT
//                    , AbsListView.LayoutParams.MATCH_PARENT, true);
            mPopWindow.setAnimationStyle(0);
            mPopWindow.setFocusable(false);
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

                HashMap<String, ArrayList<PickImgBean>> tempMap = new HashMap<String, ArrayList<PickImgBean>>();
                try {
                    while (mCursor.moveToNext()) {
                        PickImgBean bean = new PickImgBean();
                        // 获取图片路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        bean.setPath(path);
                        bean.setIsChecked(false);
                        mData.add(bean);

                        // 获取父文件夹路径
                        String parentName = new File(path).getParentFile().getName();
                        if (tempMap.containsKey(parentName)) {
                            tempMap.get(parentName).add(bean);
                        } else {
                            ArrayList<PickImgBean> list = new ArrayList<PickImgBean>();
                            list.add(bean);
                            tempMap.put(parentName, list);
                            mImgDirs.add(list);
                        }
                    }
                    mImgDirs.add(mData);
                } finally {
                    tempMap.clear();
                    mCursor.close();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        mAdapter.notifyDataSetChanged();
                        mGvGridview.setAdapter(mAdapter);
                        mProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void assignViews() {
        mGvGridview = (GridView) findViewById(R.id.gv_gridview);
        //fing的时候暂停加载图片
        mGvGridview.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));

        mRlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
        //mRlBottomBar.setOnClickListener(this);
        mTvPickimageBottomDirname = (TextView) findViewById(R.id.tv_pickimage_bottom_dirname);
        mTvPickimageBottomDirname.setOnClickListener(this);

        mBtNext = (Button) findViewById(R.id.bt_next);
        mBtNext.setOnClickListener(this);

        mAdapter = new PickImgGalleryAdapter(this, mData, mCheckedImgs);
        //mGvGridview.setAdapter(mAdapter);
        mAdapter.setOnCheckedImageCountChangeListener(this);
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

    private void disMissPop() {
        Animation outAnim = AnimationUtils.loadAnimation(this, R.anim.popupwindow_bg_out);
        outAnim.setFillAfter(true);
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mPopWindow.dismiss();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });

        mPopBg.startAnimation(outAnim);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.popupwindow_bottom_out);
        animation.setFillAfter(true);
        mRlPopContent.startAnimation(animation);
    }

    @Override
    public void onCheckedImageCountChange(int checkCount) {
        mBtNext.setText(getResources().getString(R.string.pickimg_next_count, checkCount));
    }

    private class PopupListAdapter extends BaseAdapter {
        private int mItemWidth = 0, mItemHeight = 0;

        public PopupListAdapter() {
            mItemWidth = PickImgActivity.this.getResources().getDimensionPixelSize(R.dimen.pickimg_popup_icon_width);
            mItemHeight = PickImgActivity.this.getResources().getDimensionPixelSize(R.dimen.pickimg_popup_icon_height);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.item_pickimg_popup_chossefolder, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ArrayList<PickImgBean> list = mImgDirs.get(position);
            PickImgBean first = list.get(0);
            final String path = first.getPath();

            // 加载左侧icon图片
            ImageLoaderManager.getInstance().asyncLoadImage(holder.imageviewgallerypickimgpopitem, "file://" + path, mItemWidth, mItemHeight);

            // 数量
            holder.textviewgallerypickimgpopitemcount.setText(String.format(getString(R.string.pickimg_popup_count), list.size()));

            // 文件夹名称
            if (position == 0) {
                holder.textviewgallerypickimgpopitemname.setText("所有图片");
            } else {
                String parentPath = new File(path).getParentFile().getName();
                String title = parentPath.substring(parentPath.lastIndexOf("/") + 1);
                holder.textviewgallerypickimgpopitemname.setText(title);
            }

            // 是否显示选中框
            if (position == mCurrentDirPosition) {
                holder.imagegallerypickimgpopitemcheck.setVisibility(View.VISIBLE);
            } else {
                holder.imagegallerypickimgpopitemcheck.setVisibility(View.INVISIBLE);
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
            return mImgDirs.size();
        }

    }
    private class ViewHolder {
        public final ImageView imageviewgallerypickimgpopitembg;
        public final ImageView imageviewgallerypickimgpopitem;
        public final TextView textviewgallerypickimgpopitemname;
        public final TextView textviewgallerypickimgpopitemcount;
        public final ImageView imagegallerypickimgpopitemcheck;
        public final View root;

        public ViewHolder(View root) {
            imageviewgallerypickimgpopitembg = (ImageView) root.findViewById(R.id.imageview_gallery_pickimg_pop_item_bg);
            imageviewgallerypickimgpopitem = (ImageView) root.findViewById(R.id.imageview_gallery_pickimg_pop_item);
            textviewgallerypickimgpopitemname = (TextView) root.findViewById(R.id.textview_gallery_pickimg_pop_item_name);
            textviewgallerypickimgpopitemcount = (TextView) root.findViewById(R.id.textview_gallery_pickimg_pop_item_count);
            imagegallerypickimgpopitemcheck = (ImageView) root.findViewById(R.id.image_gallery_pickimg_pop_itemcheck);
            this.root = root;
        }
    }
}
