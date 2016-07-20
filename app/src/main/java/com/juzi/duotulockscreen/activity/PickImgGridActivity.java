package com.juzi.duotulockscreen.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
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

import com.j256.ormlite.dao.Dao;
import com.juzi.duotulockscreen.App;
import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.adapter.PickImgGridAdapter;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.bean.PickImgBean;
import com.juzi.duotulockscreen.database.MyDatabaseHelper;
import com.juzi.duotulockscreen.util.CommonUtil;
import com.juzi.duotulockscreen.util.FileUtil;
import com.juzi.duotulockscreen.util.ImageLoaderManager;
import com.juzi.duotulockscreen.util.ImageUtil;
import com.juzi.duotulockscreen.util.Values;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class PickImgGridActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private GridView mGvGridview;
    private RelativeLayout mRlBottomBar;
    private TextView mTvPickimageBottomDirname;
    private ArrayList<ArrayList<PickImgBean>> mImgDirs = new ArrayList<>();
    private ArrayList<PickImgBean> mData = new ArrayList<>();
    private ArrayList<PickImgBean> mCheckedImgs = new ArrayList<>();
    private Handler mHandler = new Handler();
    private PopupWindow mPopWindow;
    private int mCurrentDirPosition = 0;
    /**
     * 读取手机中所有图片资源的url
     */
    final static Uri LOCAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private PickImgGridAdapter mAdapter;
    private View mPopBg;
    private ListView mPopLv;
    private View mRlPopContent;
    private TextView mTvDone;
    private Dialog mLoadingProgress;
    public static final int REQUEST_CODE_CLIPIMG = 101;
    public static final int REQUEST_CODE_CAMERA = 102;
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 103;
    private Dao mFavoriteDao;
    private String mImgFromCameraPath;
    private Uri mImgFromCameraUri;
    private BaseAdapter mPopupListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickimg_grid);
        assignViews();

        loadData();
    }

    @Override
    public void onBackPressed() {
        if (mPopWindow != null && mPopWindow.isShowing()) {
            disMissPop();
        } else if (mCheckedImgs.size() > 0) {
            if (mCheckedImgs.size() > 0) {
                View cv = LayoutInflater.from(this).inflate(R.layout.dialog_layout_exitsava, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setView(cv)
                        .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < mCheckedImgs.size(); i++) {
                                            PickImgBean pickImgBean = mCheckedImgs.get(i);
                                            FileUtil.deleteFile(new File(pickImgBean.getImg_url_cliped()));
                                        }
                                    }
                                }).start();
                                PickImgGridActivity.super.onBackPressed();
                                overridePendingTransition(R.anim.activity_in_left2right, R.anim.activity_out_left2right);
                            }
                        }).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                savaCutedImgToDB();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < mCheckedImgs.size(); i++) {
                            PickImgBean pickImgBean = mCheckedImgs.get(i);
                            FileUtil.deleteFile(new File(pickImgBean.getImg_url_cliped()));
                        }
                    }
                }).start();
                super.onBackPressed();
                overridePendingTransition(R.anim.activity_in_left2right, R.anim.activity_out_left2right);
            }
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.activity_in_left2right, R.anim.activity_out_left2right);
        }
    }

    private void savaCutedImgToDB() {
        final Dialog mLoadingProgress = new Dialog(this, R.style.loading_progress);
        mLoadingProgress.setContentView(R.layout.loading_progressbar_save);
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mFavoriteDao == null) {
                        mFavoriteDao = MyDatabaseHelper.getInstance(PickImgGridActivity.this).getDaoQuickly(LockScreenImgBean.class);
                    }
                    for (int i = 0; i < mCheckedImgs.size(); i++) {
                        PickImgBean pickImgBean = mCheckedImgs.get(i);
                        LockScreenImgBean bean = new LockScreenImgBean();
                        bean.setImg_url(pickImgBean.getImg_url_cliped());
                        mFavoriteDao.create(bean);
                    }
                    mCheckedImgs.clear();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingProgress.dismiss();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (CommonUtil.isQuickClick()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.bt_done: //注意保持已经截好的图到数据库
                if (mCheckedImgs.size() > 0) {
                   savaCutedImgToDB();
                } else {
                   onBackPressed();
                }
                break;
            case R.id.iv_back:
                Log.d("wangzixu", "mCheckedImgs.size() = " + mCheckedImgs.size());
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

            mPopupListAdapter = new PopupListAdapter();
            mPopLv.setAdapter((ListAdapter) mPopupListAdapter);
            mPopLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mPopWindow.dismiss();
                    if (mCurrentDirPosition == position) {
                        return;
                    }
                    mCurrentDirPosition = position;
                    ViewHolder holder = (ViewHolder) view.getTag();
                    mTvPickimageBottomDirname.setText(holder.textviewgallerypickimgpopitemname.getText());
                    mAdapter.setData(mImgDirs.get(position));
                    mAdapter.setType(position == 0 ? 0 : 1);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = PickImgGridActivity.this.getContentResolver();
                Cursor mCursor = contentResolver.query(LOCAL_IMAGE_URI, null, MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?", new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED
                        + " DESC");
                if (mCursor == null) {
                    mLoadingProgress.dismiss();
                    Toast.makeText(PickImgGridActivity.this, "读取图片失败！", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, ArrayList<PickImgBean>> mImgDirsMap = new HashMap<>();
                try {
                    while (mCursor.moveToNext()) {
                        PickImgBean bean = new PickImgBean();
                        // 获取图片路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        bean.setImg_url(path);
                        mData.add(bean);

                        // 获取父文件夹路径
                        String parentName = new File(path).getParentFile().getName();
                        if (mImgDirsMap.containsKey(parentName)) {
                            mImgDirsMap.get(parentName).add(bean);
                        } else {
                            ArrayList<PickImgBean> list = new ArrayList<>();
                            list.add(bean);
                            mImgDirsMap.put(parentName, list);
                            mImgDirs.add(list);
                        }
                    }
                    mImgDirs.add(0, mData);
                } finally {
                    mCursor.close();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        mAdapter.notifyDataSetChanged();
                        mTvPickimageBottomDirname.setText(String.format(getString(R.string.pickimg_popup_dirname), "所有图片", mData.size()));
                        mGvGridview.setAdapter(mAdapter);
                        mLoadingProgress.dismiss();
                    }
                });
            }
        }).start();
    }

    private String mImgPrefix; //用来标识一次设定锁屏的事件
    private void assignViews() {
        mLoadingProgress = new Dialog(this, R.style.loading_progress);
        mLoadingProgress.setContentView(R.layout.loading_progressbar_hei);
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();

        mImgPrefix = String.valueOf(System.currentTimeMillis() / 1000);
        mGvGridview = (GridView) findViewById(R.id.gv_gridview);
        //fing的时候暂停加载图片
        mGvGridview.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
        mGvGridview.setOnItemClickListener(this);

        mRlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
        //mRlBottomBar.setOnClickListener(this);
        mTvPickimageBottomDirname = (TextView) findViewById(R.id.tv_pickimage_bottom_dirname);
        mTvPickimageBottomDirname.setOnClickListener(this);

        findViewById(R.id.iv_back).setOnClickListener(this);
        mTvDone = (TextView) findViewById(R.id.bt_done);

        mAdapter = new PickImgGridAdapter(this, mData, mCheckedImgs);
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

    private View mCurrentClickView;
    private PickImgBean mCurrentClickImgBean;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageUtil.changeLight((ImageView)view.findViewById(R.id.iv_pickimage_item), true); //图片点击变暗
        if (mAdapter.getType() == 0) {
            if (position == 0) {
                if (Build.VERSION.SDK_INT >= 23) {
                    //需要用权限的地方之前，检查是否有某个权限
                    int checkCallPhonePermission = ContextCompat.checkSelfPermission(PickImgGridActivity.this, Manifest.permission.CAMERA);
                    Log.d("wangzixu", "checkCallPhonePermission = " + checkCallPhonePermission);
    //                if(checkCallPhonePermission == PackageManager.PERMISSION_DENIED){
    //                    askToOpenCamera();
    //                } else
                    if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(PickImgGridActivity.this,new String[]{Manifest.permission.CAMERA},REQUEST_CODE_PERMISSION_CAMERA);
                    }else{
                        getImgFromCamera();
                    }
                } else {
                    getImgFromCamera();;
                }
            } else {
                mCurrentClickImgBean = mImgDirs.get(mCurrentDirPosition).get(position - 1);
                mCurrentClickView = view;
                Intent intent = new Intent(this, ClipImgActivity.class);
                intent.putExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_SRC_PATH, mCurrentClickImgBean.getImg_url());
                intent.putExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_PREFIX, mImgPrefix);
                startActivityForResult(intent, REQUEST_CODE_CLIPIMG);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            }
        } else {
            mCurrentClickImgBean = mImgDirs.get(mCurrentDirPosition).get(position);
            mCurrentClickView = view;
            Intent intent = new Intent(this, ClipImgActivity.class);
            intent.putExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_SRC_PATH, mCurrentClickImgBean.getImg_url());
            intent.putExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_PREFIX, mImgPrefix);
            startActivityForResult(intent, REQUEST_CODE_CLIPIMG);
            overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
        }
    }

    private boolean getImgFromCamera() {
        String status= Environment.getExternalStorageState();
        boolean result = false;
        if(status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir=new File(Environment.getExternalStorageDirectory() + Values.CAMERA_DIR_TEMP);
                if(!dir.exists()) {
                    dir.mkdirs();
                }

                Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(dir, getImgFileName());
                mImgFromCameraPath = f.getAbsolutePath();
                mImgFromCameraUri = Uri.fromFile(f);
                //intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImgFromCameraUri);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "启动相机失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "请检查存储空间是否可用", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private String getImgFileName() {
        return new StringBuilder("HAOKAN_IMG_").append(System.currentTimeMillis()).append(".jpeg").toString();
    }

    //用户行为的回调，用户拒绝或者同意了授权的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户点击了同意
                    getImgFromCamera();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    askToOpenCamera();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 提示用户去设置界面开启相机权限
     */
    private void askToOpenCamera() {
        View cv = LayoutInflater.from(this).inflate(R.layout.dialog_layout_askcamera, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setView(cv)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CLIPIMG) { //截图归来
            if (resultCode == RESULT_OK) { //截图成功
                String imgPath = data.getStringExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_DONE_PATH);
                mCurrentClickImgBean.setImg_url_cliped(imgPath);
                mCurrentClickView.findViewById(R.id.mask).setVisibility(View.VISIBLE);
                if (!mCheckedImgs.contains(mCurrentClickImgBean)) {
                    mCheckedImgs.add(mCurrentClickImgBean);
                }
            } else { //取消或失败
                mCurrentClickImgBean.setImg_url_cliped(null);
                mCurrentClickView.findViewById(R.id.mask).setVisibility(View.GONE);
                if (mCheckedImgs.contains(mCurrentClickImgBean)) {
                    mCheckedImgs.remove(mCurrentClickImgBean);
                }
            }
            if (mCheckedImgs.size() > 0) {
                mTvDone.setOnClickListener(this);
            } else {
                mTvDone.setOnClickListener(null);
            }
        } else if (requestCode == REQUEST_CODE_CAMERA) { //拍照归来
            File f = new File(mImgFromCameraPath);
            if (f.exists()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(f));
                sendBroadcast(intent); //通知系统有张新拍的照片

                PickImgBean bean = new PickImgBean();
                bean.setImg_url(mImgFromCameraPath);
                mData.add(0, bean);
                mAdapter.notifyDataSetChanged();

                mCurrentClickImgBean = bean;
                mCurrentClickView = mAdapter.getItem1();
                Intent intent1 = new Intent(this, ClipImgActivity.class);
                intent1.putExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_SRC_PATH, mCurrentClickImgBean.getImg_url());
                intent1.putExtra(ClipImgActivity.KEY_INTENT_CLIPIMG_PREFIX, mImgPrefix);
                startActivityForResult(intent1, REQUEST_CODE_CLIPIMG);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            }
        }
    }

    @Override
    protected void onDestroy() {
        App app = (App) getApplication();
        app.setBigImgData(null);
        app.setCheckedImgs(null);
        super.onDestroy();
    }

    private class PopupListAdapter extends BaseAdapter {
        private int mItemWidth = 0, mItemHeight = 0;

        public PopupListAdapter() {
            mItemWidth = PickImgGridActivity.this.getResources().getDimensionPixelSize(R.dimen.pickimg_popup_icon_width);
            mItemHeight = PickImgGridActivity.this.getResources().getDimensionPixelSize(R.dimen.pickimg_popup_icon_height);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            boolean shouldInitBitmap;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.item_pickimg_popup_chossefolder, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
                shouldInitBitmap = true;
            } else {
                holder = (ViewHolder) convertView.getTag();
                shouldInitBitmap = holder.position != position;
            }

            holder.position = position;

            ArrayList<PickImgBean> list = mImgDirs.get(position);
            PickImgBean first = list.get(0);
            final String path = first.getImg_url();

            if (shouldInitBitmap) {
                holder.imageviewgallerypickimgpopitem.setImageBitmap(null);
            }

            ImageLoaderManager.getInstance().displayImage("file://" + path
                    , holder.imageviewgallerypickimgpopitem, mItemWidth, mItemHeight, true);

            // 数量
//            holder.textviewgallerypickimgpopitemcount.setText(String.format(getString(R.string.pickimg_popup_count), "所有图片", list.size()));

            // 文件夹名称
            if (position == 0) {
                holder.textviewgallerypickimgpopitemname.setText(String.format(getString(R.string.pickimg_popup_dirname), "所有图片", list.size()));
            } else {
                String parentPath = new File(path).getParentFile().getName();
                String title = parentPath.substring(parentPath.lastIndexOf("/") + 1);
                holder.textviewgallerypickimgpopitemname.setText(String.format(getString(R.string.pickimg_popup_dirname), title, list.size()));
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
        //public final TextView textviewgallerypickimgpopitemcount;
        public final ImageView imagegallerypickimgpopitemcheck;
        public final View root;
        public int position;

        public ViewHolder(View root) {
            imageviewgallerypickimgpopitembg = (ImageView) root.findViewById(R.id.imageview_gallery_pickimg_pop_item_bg);
            imageviewgallerypickimgpopitem = (ImageView) root.findViewById(R.id.imageview_gallery_pickimg_pop_item);
            textviewgallerypickimgpopitemname = (TextView) root.findViewById(R.id.textview_gallery_pickimg_pop_item_name);
            //textviewgallerypickimgpopitemcount = (TextView) root.findViewById(R.id.textview_gallery_pickimg_pop_item_count);
            imagegallerypickimgpopitemcheck = (ImageView) root.findViewById(R.id.image_gallery_pickimg_pop_itemcheck);
            this.root = root;
        }
    }
}
