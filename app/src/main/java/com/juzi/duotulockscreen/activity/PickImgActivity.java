package com.juzi.duotulockscreen.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.util.LogHelper;

import java.io.File;

public class PickImgActivity extends BaseActivity implements View.OnClickListener {
    private GridView mGvGridview;
    private RelativeLayout mRlBottomBar;
    private TextView mTvPickimageBottomDirname;
    private ProgressDialog mProgressDialog;

    /**
     * 读取手机中所有图片资源的url
     */
    final static Uri LOCAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomActionBar();
        setContentView(R.layout.activity_priview);
        assignViews();

        loadData();
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

                HashMap<String, ArrayList<LocalPictureBean>> mImgCacheDirs = new HashMap<String, ArrayList<LocalPictureBean>>();
                try {
                    while (mCursor.moveToNext()) {
                        LocalPictureBean bean = new LocalPictureBean();
                        // 获取图片路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        bean.setPath(path);
                        bean.setChecked(false);
                        mData.add(bean);

                        // 获取父文件夹路径
                        String parentName = new File(path).getParentFile().getName();
                        if (mImgCacheDirs.containsKey(parentName)) {
                            mImgCacheDirs.get(parentName).add(bean);
                        } else {
                            ArrayList<LocalPictureBean> list = new ArrayList<LocalPictureBean>();
                            list.add(bean);
                            mImgCacheDirs.put(parentName, list);
                            mImageDirs.add(list);
                        }
                    }
                    mImageDirs.add(0, mData);
                } finally {
                    mImgCacheDirs.clear();
                    mCursor.close();
                }
                mHandler.sendEmptyMessage(LOAD_DATA_OK);
            }
        }).start();
    }

    private void assignViews() {
        mGvGridview = (GridView) findViewById(R.id.gv_gridview);
        mRlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
        mTvPickimageBottomDirname = (TextView) findViewById(R.id.tv_pickimage_bottom_dirname);
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
}
