package com.juzi.duotulockscreen.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.juzi.duotulockscreen.App;
import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.adapter.PickImgBigImgViewPagerAdapter;
import com.juzi.duotulockscreen.bean.PickImgBean;
import com.juzi.duotulockscreen.view.ZoomImageView;
import com.juzi.duotulockscreen.view.ZoomImageViewPager;

import java.util.ArrayList;

public class PickImgBigActivity extends BaseActivity implements View.OnClickListener, ZoomImageViewPager.onCustomClikListener {
    private ZoomImageViewPager mZoomImageViewPager;
    public static final int VIEWPAGE_CACHE_COUNT = 2;
    public static final String EXTRA_INIT_POSITION = "initpos";
    private ArrayList<ZoomImageView> mViewsList;
    private Handler mHandler = new Handler();
    private View mTopBar;
    private ImageView mIvTopChosed;
    private ArrayList<PickImgBean> mData;
    private ArrayList<PickImgBean> mCheckedImgs;
    private PopupWindow mPopupWindow;
    private boolean mChangedChose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickimgbig);
        assignViews();
    }

    @Override
    protected boolean getIsFullScreen() {
        return true;
    }

    private void assignViews() {
        final Dialog mLoadingProgress = new Dialog(this, R.style.loading_progress);
        mLoadingProgress.setContentView(R.layout.loading_progressbar_bai);
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              mLoadingProgress.dismiss();
            }
        }, 300);

        mTopBar = findViewById(R.id.rl_topbar);
        ImageView ivBack = (ImageView) mTopBar.findViewById(R.id.iv_back);
        mIvTopChosed = (ImageView) mTopBar.findViewById(R.id.iv_chose);
        ivBack.setOnClickListener(this);
        mIvTopChosed.setOnClickListener(this);

        mTopBar.setVisibility(View.VISIBLE);

        mZoomImageViewPager = (ZoomImageViewPager) findViewById(R.id.zivp_bigimg);
        mZoomImageViewPager.setOnCustomClikListener(this);

        //初始化viewpager相关
        int size = VIEWPAGE_CACHE_COUNT * 2 + 1;
        mViewsList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ZoomImageView view = new ZoomImageView(this);
            view.setId(R.id.iv_for_pickbigimg);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            view.setBackgroundColor(0xFF000000);
            view.setScaleType(ImageView.ScaleType.MATRIX);
            view.setScaleMode(2); //宽边符合宽高的
            view.setParentCanScroll(true);
            //view.setOnClickListener(this);
//            view.setFocusable(false);
//            view.setFocusableInTouchMode(false);
            mViewsList.add(view);
        }

        Intent intent = getIntent();
        int position = intent.getIntExtra(EXTRA_INIT_POSITION, 0);

        App app = (App) getApplication();
        mData = app.getBigImgData();
        mCheckedImgs = app.getCheckedImgs();

        PickImgBigImgViewPagerAdapter mAdapter = new PickImgBigImgViewPagerAdapter(mData, getApplicationContext(), mViewsList);
        mZoomImageViewPager.setAdapter(mAdapter);
        mZoomImageViewPager.setInitCurrentItem(position);
        handleItem(position);

        mZoomImageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                handleItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private PickImgBean mCurrentBean;
    private void handleItem(int position) {
        int viewPosition = position % mViewsList.size();
        ZoomImageView imgView = mViewsList.get(viewPosition);
        mZoomImageViewPager.setZoomImageView(imgView);

        mCurrentBean = mData.get(position);
        mIvTopChosed.setSelected(mCheckedImgs.contains(mCurrentBean));
    }

    final long[] hits = new long[2];

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_chose:
                mChangedChose = true;
                boolean checked = mCheckedImgs.contains(mCurrentBean);
                if (checked) {
                    mCheckedImgs.remove(mCurrentBean);
                } else {
                    mCheckedImgs.add(mCurrentBean);
                }
                v.setSelected(!checked);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mChangedChose) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_retain, R.anim.activity_fade_out);
    }

    private int mTopBarBottom; //顶部栏的底边位置
    private Runnable onSingleClikRun = new Runnable() {
        @Override
        public void run() {
            if (mTopBar.getVisibility() == View.VISIBLE) {
                //开始隐藏动画
                mTopBarBottom = mTopBar.getBottom();
                final ValueAnimator anim = ValueAnimator.ofFloat(0, mTopBarBottom);
                anim.setDuration(200);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mTopBar.setVisibility(View.GONE);
                    }
                });
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Float i = -(Float) animation.getAnimatedValue();
                        mTopBar.setTranslationY(i);
                    }
                });
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        anim.start();
                    }
                });
            } else {
                //开始显示动画
                mTopBar.setVisibility(View.VISIBLE);
                //mTopBarBottom = mIbBigimgClose.getBottom();
                final ValueAnimator anim = ValueAnimator.ofFloat(mTopBarBottom, 0);
                anim.setDuration(200);
                //anim.setInterpolator(Login_Register_SwAnim_Manager.sInterpolatorIn);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Float i = -(Float) animation.getAnimatedValue();
                        mTopBar.setTranslationY(i);
                    }
                });
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        anim.start();
                    }
                });
            }
        }
    };

    @Override
    public void onCustomClick(ZoomImageView v) { //点击事件
        // src 要拷贝的源数组
        // srcPos 从源数组的哪个位子开始拷贝
        // dst 要拷贝的目标数组
        // dstPos 目标数组的哪个位子开始拷贝
        // length 要拷贝多少个元素.
        // 思路就是使数组每次点击左移一位，判断最后一位和第一位的时间差
        Log.d("wangzixu", "zivp_bigimg on click");
        System.arraycopy(hits, 1, hits, 0, hits.length - 1);
        hits[hits.length - 1] = SystemClock.uptimeMillis();
        if ((hits[hits.length - 1] - hits[0]) <= 310) {
            mHandler.removeCallbacks(onSingleClikRun);
            v.onDubleClick();
        } else {
            mHandler.postDelayed(onSingleClikRun, 320);
        }
    }
}
