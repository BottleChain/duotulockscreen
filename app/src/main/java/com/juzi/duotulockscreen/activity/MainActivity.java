package com.juzi.duotulockscreen.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.j256.ormlite.dao.Dao;
import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.adapter.LockScreensGalleryAdapter;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.database.MyDatabaseHelper;
import com.juzi.duotulockscreen.lockscreen.LockScreenService;
import com.juzi.duotulockscreen.util.ToastManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        Intent lock = new Intent(this, LockScreenService.class);
        startService(lock);
        loadData();
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
                    List<LockScreenImgBean> list = mFavoriteDao.queryBuilder().offset(mCurrentPage * PAGE_COUNT).limit(PAGE_COUNT).query();
                    mCurrentPage ++;
                    if (list.size() >= PAGE_COUNT) {
                        mHasMoreData = true;
                    } else {
                        mHasMoreData = false;
                    }
                    Collections.reverse(list); //倒序
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
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                arrowDrawable.setProgress(slideOffset);
            }
        });
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
    }

    private long mLastBackTime = 0;
    @Override
    public void onBackPressed() {
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - mLastBackTime > 2000) {
            mLastBackTime = currentTime;
            ToastManager.showShort(this, "再按一次退出");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                    Intent intent = new Intent(MainActivity.this, PickImgGridActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_in_right2left, R.anim.activity_out_right2left);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (position == mData.size()) {
//            Intent intent = new Intent(this, PickImgGridActivity.class);
//            startActivity(intent);
//        }
    }
}
