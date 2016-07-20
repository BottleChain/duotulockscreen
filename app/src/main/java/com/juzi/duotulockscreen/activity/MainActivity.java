package com.juzi.duotulockscreen.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;

import com.j256.ormlite.dao.Dao;
import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.adapter.LockScreensGalleryAdapter;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.database.MyDatabaseHelper;
import com.juzi.duotulockscreen.lockscreen.LockScreenService;
import com.juzi.duotulockscreen.util.FileUtil;
import com.juzi.duotulockscreen.util.ImageUtil;
import com.juzi.duotulockscreen.util.LogHelper;
import com.juzi.duotulockscreen.util.MyActivityTransanimHelper;
import com.juzi.duotulockscreen.util.ToastManager;
import com.juzi.duotulockscreen.util.Values;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";
    private GridView mGridView;
    private ArrayList<LockScreenImgBean> mData  = new ArrayList<>();
    private LockScreensGalleryAdapter mAdapter;
    private DrawerLayout mDrawerLayout;
    //分页查询
    private int mCurrentPage;
    public static long PAGE_COUNT = 50;
    private boolean mHasMoreData = true;
    private boolean mIsLoading;

    private Dao mFavoriteDao;
    private Handler mHandler = new Handler();
    private Dialog mLoadingProgress;
    private View mLayoutNoContent;
    private final int REQUEST_CODE_ADDIMG = 100;
    private final int REQUEST_CODE_DELETEIMG = 101;
    private final int REQUEST_CODE_PERMISSION_EXTERNALSD = 103;
    private View mRlOpenNotify;
    private View mRlAutoStart;
    private View mRlCloseSysLock;
    private View mRlFloaWindow;
    public static int sDeleteImgPos = -1;
    private SwipeRefreshLayout mSwipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        loadData();
    }

    @Override
    protected void onResume() {
//        if (sDeleteImgPos != -1) {
//            final LockScreenImgBean bean = mData.remove(sDeleteImgPos);
//            mAdapter.notifyDataSetChanged();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Dao dao = MyDatabaseHelper.getInstance(MainActivity.this).getDaoQuickly(LockScreenImgBean.class);
//                        dao.delete(bean);
//                        FileUtil.deleteFile(new File(bean.getImg_url()));
//                        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//                        boolean isLockScreen = sp1.getBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, true);
//                        if (isLockScreen) {
//                            Intent intent = new Intent(MainActivity.this, LockScreenService.class);
//                            intent.putExtra(LockScreenService.SERVICE_TYPE, 3);
//                            startService(intent);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            if (mAdapter.getCount() <= 0) {
//                mLayoutNoContent.setVisibility(View.VISIBLE);
//                mGridView.setVisibility(View.GONE);
//            }
//            sDeleteImgPos = -1;
//        }
        super.onResume();
    }

    private void loadData() {
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mFavoriteDao = MyDatabaseHelper.getInstance(MainActivity.this).getDaoQuickly(LockScreenImgBean.class);
                    List<LockScreenImgBean> list = mFavoriteDao.queryBuilder().orderBy("insert_time", false).offset(mCurrentPage * PAGE_COUNT).limit(PAGE_COUNT).query();
                    mCurrentPage ++;
                    if (list.size() >= PAGE_COUNT) {
                        mHasMoreData = true;
                    } else {
                        mHasMoreData = false;
                    }
                    mData.addAll(list);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
//                            if (mData.size() > 0) {
//                                mRlMyfavoriteNofavorite.setVisibility(View.GONE);
//                                mGvMyfavorite.setVisibility(View.VISIBLE);
//                                mAdapter.notifyDataSetChanged();
//                            } else {
//                                mRlMyfavoriteNofavorite.setVisibility(View.VISIBLE);
//                                mGvMyfavorite.setVisibility(View.GONE);
//                            }
                            if (mData.size() == 0) {
                                mLayoutNoContent.setVisibility(View.VISIBLE);
                            } else {
                                mLayoutNoContent.setVisibility(View.GONE);
                            }
                            if (mAdapter == null) {
                                mAdapter = new LockScreensGalleryAdapter(MainActivity.this, mData);
                                mGridView.setAdapter(mAdapter);
                            } else {
                                mAdapter.notifyDataSetChanged();
                            }
                            mLoadingProgress.dismiss();
                            mIsLoading = false;
                        }
                    });
                } catch (SQLException e) {
                    mIsLoading = false;
                    mLoadingProgress.dismiss();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void assignViews() {
        mLoadingProgress = new Dialog(this, R.style.loading_progress);
        mLoadingProgress.setContentView(R.layout.loading_progressbar_hei);
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();

        findViewById(R.id.iv_add).setOnClickListener(this);
        mLayoutNoContent = findViewById(R.id.layout_nocontent);
        ImageView ivHeaderLeft = (ImageView) findViewById(R.id.iv_back);
        final DrawerArrowDrawable arrowDrawable = new DrawerArrowDrawable(this);
        arrowDrawable.setColor(getResources().getColor(R.color.bai));
        ivHeaderLeft.setImageDrawable(arrowDrawable);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.draw_layout);
        View rlSwitch = mDrawerLayout.findViewById(R.id.rl_drawer_lockswitch);
        rlSwitch.setOnClickListener(this);
        Switch aSwitch = (Switch) rlSwitch.findViewById(R.id.sw_addlock);
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLockScreen = sp1.getBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, true);
        aSwitch.setChecked(isLockScreen);
        aSwitch.setOnCheckedChangeListener(this);
        if (isLockScreen) {
            Intent lock = new Intent(this, LockScreenService.class);
            startService(lock);
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                arrowDrawable.setProgress(slideOffset);
            }
        });
        mDrawerLayout.findViewById(R.id.divider2).setOnClickListener(this);
        mDrawerLayout.findViewById(R.id.left_drawer_bottomview).setOnClickListener(this);
        mRlOpenNotify = mDrawerLayout.findViewById(R.id.rl_drawer_opennotify);
        mRlOpenNotify.setOnClickListener(this);
        mRlAutoStart = mDrawerLayout.findViewById(R.id.rl_drawer_autostart);
        mRlAutoStart.setOnClickListener(this);
        mRlCloseSysLock = mDrawerLayout.findViewById(R.id.rl_drawer_closesyslock);
        mRlCloseSysLock.setOnClickListener(this);
        mRlFloaWindow = mDrawerLayout.findViewById(R.id.rl_drawer_flowwindow);
        mRlFloaWindow.setOnClickListener(this);

        mDrawerLayout.findViewById(R.id.rl_drawer_checkupdata).setOnClickListener(this);
        mDrawerLayout.findViewById(R.id.rl_drawer_about).setOnClickListener(this);
        ivHeaderLeft.setOnClickListener(this);

        mGridView = (GridView) findViewById(R.id.gv_gridview);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (mHasMoreData) {
                        int lastVisiblePosition = mGridView.getLastVisiblePosition();
                        if (lastVisiblePosition + 15 >= mData.size()) {
                            loadData();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        }));

        String os = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(os)) {
            if (os.equalsIgnoreCase("Xiaomi")) {
                mRlAutoStart.setVisibility(View.VISIBLE);
                mRlFloaWindow.setVisibility(View.VISIBLE);
            } else if (os.equalsIgnoreCase("HUAWEI")) {
                mRlAutoStart.setVisibility(View.VISIBLE);
                mRlFloaWindow.setVisibility(View.GONE);
            } else if (os.equalsIgnoreCase("OPPO")) {
                mRlAutoStart.setVisibility(View.VISIBLE);
                mRlFloaWindow.setVisibility(View.GONE);
            }else {
                mRlAutoStart.setVisibility(View.GONE);
                mRlFloaWindow.setVisibility(View.GONE);
            }
        }

        //系统自带下拉刷新-------------------
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
//        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
//        mSwipeLayout.setDistanceToTriggerSync(400);// 设置手指在屏幕下拉多少距离会触发下拉刷新
//        mSwipeLayout.setProgressBackgroundColor(android.R.color.holo_green_light); // 设定下拉圆圈的背景
//        mSwipeLayout.setSize(SwipeRefreshLayout.LARGE); // 设置圆圈的大小

    }

    private long mLastBackTime = 0;
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - mLastBackTime > 2000) {
            mLastBackTime = currentTime;
            ToastManager.showShort(this, "再按一次退出");
        } else {
            super.onBackPressed();
        }
    }

    private boolean close_systemlock() {
        boolean success = false;
        if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            // 关闭系统锁屏 进入开发者选项
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // 无密码
                Intent intent = new Intent();
                ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.ChooseLockGeneric");
                intent.setComponent(cm);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private void start_auto() {
        String os = Build.MANUFACTURER;
        if (os.equalsIgnoreCase("Xiaomi")) {
            try {
                // 开机自启动
                Intent intent = new Intent();
                intent.setAction("miui.intent.action.OP_AUTO_START");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, 2);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (os.equalsIgnoreCase("HUAWEI")) {
            try {
                // 跳转到华为手机管家受保护应用
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
                intent.setComponent(cn);
                startActivityForResult(intent, 2);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (os.equalsIgnoreCase("OPPO")) {
            try {
                // 跳转到OPPO安全中心清理白名单
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings", "com.oppo.settings.exp.WhiteList2");
                intent.setComponent(cn);
                startActivityForResult(intent, 2);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void show_floatingwindow() {
        String os = Build.MANUFACTURER;
        if (os.equalsIgnoreCase("Xiaomi")) {
            try {
                // 显示悬浮窗
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", getPackageName());
                startActivityForResult(localIntent, 3);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            } catch (ActivityNotFoundException localActivityNotFoundException) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 3);
                    overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (os.equalsIgnoreCase("HUAWEI")) {
            try {
                // 跳转到华为手机管家悬浮窗管理
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");
                intent.setComponent(cn);
                startActivityForResult(intent, 3);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (os.equalsIgnoreCase("OPPO")) {
            try {
                // 跳转到oppo安全中心自启动管理
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                intent.setComponent(cn);
                startActivityForResult(intent, 3);
                overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_drawer_about:
                break;
            case R.id.rl_drawer_checkupdata:
                break;
            case R.id.rl_drawer_closesyslock:
                close_systemlock();
                break;
            case R.id.rl_drawer_autostart:
                start_auto();
                break;
            case R.id.rl_drawer_flowwindow:
                show_floatingwindow();
                break;
            case R.id.rl_drawer_opennotify:
                try {
                    Intent notify = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(notify);//显示授予通知权限的界面
                    overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.iv_back:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.iv_add:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        //需要用权限的地方之前，检查是否有某个权限
                        int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        Log.d("wangzixu", "checkCallPhonePermission = " + checkCallPhonePermission);
                        if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_PERMISSION_EXTERNALSD);
                        }else{
                            gotoPickImgActivity();
                        }
                    } else {
                        gotoPickImgActivity();
                    }
                }
                break;
            default:
                break;
        }
    }

    //用户行为的回调，用户拒绝或者同意了授权的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_EXTERNALSD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户点击了同意
                    gotoPickImgActivity();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    askToOpenCamera();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 提示用户去设置界面开启读写权限权限
     */
    private void askToOpenCamera() {
        View cv = LayoutInflater.from(this).inflate(R.layout.dialog_layout_askexternalsd, null);
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

    private void gotoPickImgActivity() {
        Intent intent = new Intent(MainActivity.this, PickImgGridActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADDIMG);
        overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADDIMG) {
                mCurrentPage = 0;
                mData.clear();
                loadData();
                SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean isLockScreen = sp1.getBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, true);
                if (isLockScreen) {
                    Intent intent = new Intent(MainActivity.this, LockScreenService.class);
                    intent.putExtra(LockScreenService.SERVICE_TYPE, 3);
                    startService(intent);
                }
            } else if (requestCode == REQUEST_CODE_DELETEIMG) {
                int pos = data.getIntExtra(LockScreenBigImgActivity.IMAGE_POSITION, -1);
                if (pos != -1) { //删除指定位置条目
                    final LockScreenImgBean bean = mData.remove(pos);
                    mAdapter.notifyDataSetChanged();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Dao dao = MyDatabaseHelper.getInstance(MainActivity.this).getDaoQuickly(LockScreenImgBean.class);
                                dao.delete(bean);
                                FileUtil.deleteFile(new File(bean.getImg_url()));
                                SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                boolean isLockScreen = sp1.getBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, true);
                                if (isLockScreen) {
                                    Intent intent = new Intent(MainActivity.this, LockScreenService.class);
                                    intent.putExtra(LockScreenService.SERVICE_TYPE, 3);
                                    startService(intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    if (mAdapter.getCount() <= 0) {
                        mLayoutNoContent.setVisibility(View.VISIBLE);
                        mGridView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageView img = (ImageView) view.findViewById(R.id.iv_item_content);
        if (img == null) {
            return;
        }

        Intent intent = new Intent(this, LockScreenBigImgActivity.class);

//        Drawable drawable = img.getDrawable().getConstantState().newDrawable();
//        TransitionAnimTestActivity.sPreviewDrawable = drawable;

        intent.putExtra(LockScreenBigImgActivity.IMAGE_POSITION, position);
        intent.putExtra(LockScreenBigImgActivity.IMAGE_PATH, mData.get(position).getImg_url());

        MyActivityTransanimHelper.getInstance().startActivity(this, intent, img, "image", position, new Point(0, 0));
        ImageUtil.changeLight(img, true); //图片点击变暗

//        startActivityForResult(intent, REQUEST_CODE_DELETEIMG);
//        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, img, "image");
//        ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
//        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_retain);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLockScreen = mDefaultSharedPreferences.getBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, true);
        SharedPreferences.Editor edit = mDefaultSharedPreferences.edit();
        if (!isChecked) {//由开到关
            LogHelper.d(TAG, "锁屏 由开到关");
            edit.putBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, false).apply();
            Intent lock = new Intent(this, LockScreenService.class);
            stopService(lock);
        } else if (isChecked) { //由关到开
            LogHelper.d(TAG, "锁屏 由关到开");
            edit.putBoolean(Values.KEY_PREFERENCE_LOCKSCREEN, true).apply();
            Intent lock = new Intent(this, LockScreenService.class);
            lock.putExtra(LockScreenService.SERVICE_TYPE, 0);
//            Dao dao;
//            try {
//                dao = MyDatabaseHelper.getInstance(this).getDaoQuickly(LockScreenImgBean.class);
//                final List list = dao.queryForAll();
//                if (list != null && list.size() > 0) {
//                    lock.putExtra(LockScreenService.SERVICE_TYPE, 5); //至少有一张锁屏图片
//                } else {
//                    lock.putExtra(LockScreenService.SERVICE_TYPE, 6); //没有一张图片
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
            startService(lock);
        }
    }

    @Override
    public void onRefresh() {
        Log.d("wangzixu", "onRefresh");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 停止刷新
                mSwipeLayout.setRefreshing(false);
            }
        }, 2000); // 5秒后发送消息，停止刷新
    }
}
