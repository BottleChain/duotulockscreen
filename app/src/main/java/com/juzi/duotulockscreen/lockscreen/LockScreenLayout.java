package com.juzi.duotulockscreen.lockscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.juzi.duotulockscreen.R;
import com.juzi.duotulockscreen.bean.LockScreenImgBean;
import com.juzi.duotulockscreen.database.MyDatabaseHelper;
import com.juzi.duotulockscreen.util.UmengEventIds;
import com.umeng.analytics.MobclickAgent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LockScreenLayout extends RelativeLayout implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private ViewPager mViewpager;
    private TextView mTvTime1;
    private TextView mTvTime2;
    private TextView mTvTime4;
    private TextView mTvTime5;
    private TextView mTvDate;
    private ArrayList<LockScreenImgBean> mLockScreenImgBeans = new ArrayList<LockScreenImgBean>();
    private LockScreenAdapter mAdapter;
    private boolean mIsFirstLoad = true;

    private int mScreenH, mScreenW;

    //时间翻转的动画效果
    private ObjectAnimator animator_time1;
    private ObjectAnimator animator_time2;
    private ObjectAnimator animator_time4;
    private ObjectAnimator animator_time5;
    private View mIconUnlock;

    private Runnable rHideSystemBar = new Runnable() {
        @Override
        public void run() {
            int flags = 0;
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            flags = flags | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//            flags = flags | View.SYSTEM_UI_FLAG_LOW_PROFILE; //这个属性使状态栏部分图标消失，只剩电池和事件，并且变得很暗
            flags = flags | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//            flags = flags | View.SYSTEM_UI_FLAG_FULLSCREEN;
            flags = flags | View.SYSTEM_UI_FLAG_IMMERSIVE;
            flags = flags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            setSystemUiVisibility(flags);
        }
    };
    private View mRlBottom;

    public LockScreenLayout(Context context) {
        this(context, null);
    }

    public LockScreenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.lockscreeen_layout, this);
        assignViews();
    }

    private void assignViews() {
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        mViewpager.setOffscreenPageLimit(2);
        mTvTime1 = (TextView) findViewById(R.id.tv_time1);
        mTvTime2 = (TextView) findViewById(R.id.tv_time2);
        mTvTime4 = (TextView) findViewById(R.id.tv_time4);
        mTvTime5 = (TextView) findViewById(R.id.tv_time5);

        mRlBottom = findViewById(R.id.rl_bottom);

//        OpenSans-Light.ttf
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Light.ttf");
//        mTvTime1.setTypeface(tf);
//        mTvTime2.setTypeface(tf);
//        // tv_time3.setTypeface(tf);
//        mTvTime4.setTypeface(tf);
//        mTvTime5.setTypeface(tf);

        mTvDate = (TextView) findViewById(R.id.tv_date);
        mIconUnlock = findViewById(R.id.icon_unlock);

        mAdapter = new LockScreenAdapter(getContext(), mLockScreenImgBeans);
        mAdapter.setOnImgClickListener(this);
        mViewpager.addOnPageChangeListener(this);
        mViewpager.setAdapter(mAdapter);

        initData();
        setDateTime();

        mScreenH = getResources().getDisplayMetrics().heightPixels;
        mScreenW = getResources().getDisplayMetrics().widthPixels;
    }

    public void initData() {
        try {
            Dao dao = MyDatabaseHelper.getInstance(getContext()).getDaoQuickly(LockScreenImgBean.class);
            List list = dao.queryForAll();
            Collections.reverse(list); //倒序
//            if (list == null || list.size() == 0) {
//                if (mLockScreenImgBeans.size() > 0) {
//                    mLockScreenImgBeans.clear();
//                    Intent lock = new Intent(getContext(), LockScreenService.class);
//                    lock.putExtra(LockScreenService.SERVICE_TYPE, 6); //没有一张图片
//                    getContext().startService(lock);
//                }
//                return;
//            } else if (mLockScreenImgBeans.size() <= 0) {
//                Intent lock = new Intent(getContext(), LockScreenService.class);
//                lock.putExtra(LockScreenService.SERVICE_TYPE, 5); //由没图片变成了有图片了
//                getContext().startService(lock);
//            }

            mLockScreenImgBeans.clear();
            mLockScreenImgBeans.addAll(list);

            if (mLockScreenImgBeans.isEmpty()) {
                LockScreenImgBean bean = new LockScreenImgBean();
                bean.setIsDeflaut(1);
                mLockScreenImgBeans.add(bean);
            }

            mAdapter.notifyDataSetChanged();
            if (mAdapter.getCount() > 1) {
                mViewpager.setCurrentItem(mLockScreenImgBeans.size() * 10000 - 1);
            } else {
                mViewpager.setCurrentItem(0);
            }
//            if (mIsFirstLoad) {
//                mIsFirstLoad = false;
//            }
//            mTitle.setText(mLockScreenImgBeans.get(mViewpager.getCurrentItem() % mLockScreenImgBeans.size()).getImg_title());
//            mTvDescription.setText(mLockScreenImgBeans.get(mViewpager.getCurrentItem() % mLockScreenImgBeans.size()).getImg_desc());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showNextImg() {
        if (mAdapter.getCount() > 1) {
            mViewpager.setCurrentItem(mViewpager.getCurrentItem() + 1, false);
        }
    }

    public void setOnUnLockListener(LockView.onUnLockListener l) {
        mAdapter.setOnUnLockListener(l);
    }

    /**
     * 灭屏。。
     */
    public void offScreen() {
        disMissIconUnLock();

        //隐藏虚拟按键。。。华为手机下面的那3个键
        mViewpager.removeCallbacks(rHideSystemBar);
        mViewpager.postDelayed(rHideSystemBar, 50);

        setDateTime();
    }

    /**
     * 亮屏。。
     */
    public void wakeUpScreen() {
//        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
//        alphaAnimation.setDuration(1500);
//        alphaAnimation.setRepeatCount(-1);
//        alphaAnimation.setRepeatMode(Animation.REVERSE);

        mIsDisMissIconUnlock = false;
        if (mAdapter != null && mAdapter.getCount() == 1) {
            MobclickAgent.onEvent(getContext(), UmengEventIds.event_lockscreen_image_show);
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsDisMissIconUnlock) {
                    return;
                }
                mIconUnlock.setVisibility(VISIBLE);
                ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
                animator.setDuration(1500);
                animator.setRepeatMode(Animation.RESTART);
                animator.setRepeatCount(2);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float f = animation.getAnimatedFraction();
                        mIconUnlock.setTranslationY(-f * 50);
                        mIconUnlock.setAlpha(4 * (f - f * f));
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        disMissIconUnLock();
                    }
                });
                animator.start();
            }
        }, 300);
//        mIconUnlock.startAnimation(alphaAnimation);
    }

    public void onUnLockSuccess() {
        mRlBottom.setAlpha(1.0f);
        disMissIconUnLock();
    }

    private boolean mIsDisMissIconUnlock;
    public void disMissIconUnLock() {
        mIconUnlock.clearAnimation();
        mIconUnlock.setVisibility(INVISIBLE);
        mIsDisMissIconUnlock = true;
    }

//    public void disMissDes() {
//        if (mIsAnimating) {
//            return;
//        }
////        mTvDescription.setVisibility(GONE);
//        mIsShowDes = false;
//        mTvDescription.measure(MeasureSpec.makeMeasureSpec(mScreenW, MeasureSpec.AT_MOST)
//                , MeasureSpec.makeMeasureSpec(mScreenH, MeasureSpec.AT_MOST));//主动测量一次，才知道正确的高度
//        final int height = mTvDescription.getMeasuredHeight();
//        ValueAnimator animator = ValueAnimator.ofInt(0, height - mRlBotPaddingBot);
//        animator.setDuration(200);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int value = (Integer) animation.getAnimatedValue();
//                mRlIssueBottom.setPadding(mRlIssueBottom.getPaddingLeft()
//                        , mRlIssueBottom.getPaddingTop()
//                        , mRlIssueBottom.getPaddingRight()
//                        , -value);
//                mTvDescription.setAlpha(1.0f - animation.getAnimatedFraction());
//            }
//        });
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mIsShowDes = false;
//                mIsAnimating = false;
//                mTvDescription.setVisibility(GONE);
//                mRlIssueBottom.setPadding(mRlIssueBottom.getPaddingLeft()
//                        , mRlIssueBottom.getPaddingTop()
//                        , mRlIssueBottom.getPaddingRight()
//                        , mRlBotPaddingBot);
//            }
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mIsAnimating = true;
//            }
//        });
//        animator.start();
//    }

//    private boolean mIsAnimating;
//    private boolean mIsShowDes = true;
//    public void showDes() {
//        if (mIsAnimating) {
//            return;
//        }
//        mIsShowDes = true;
//
//        mTvDescription.measure(MeasureSpec.makeMeasureSpec(mScreenW, MeasureSpec.AT_MOST)
//                , MeasureSpec.makeMeasureSpec(mScreenH, MeasureSpec.AT_MOST)); //主动测量一次，才知道正确的高度
//        final int height = mTvDescription.getMeasuredHeight();
//        ValueAnimator animator = ValueAnimator.ofInt(height - mRlBotPaddingBot, 0);
//        animator.setDuration(200);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int value = (Integer) animation.getAnimatedValue();
//                mRlIssueBottom.setPadding(mRlIssueBottom.getPaddingLeft()
//                        , mRlIssueBottom.getPaddingTop()
//                        , mRlIssueBottom.getPaddingRight()
//                        , -value);
//                mTvDescription.setAlpha(animation.getAnimatedFraction());
//            }
//        });
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mIsAnimating = false;
//                mIsShowDes = true;
//            }
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mIsAnimating = true;
//                mTvDescription.setVisibility(VISIBLE);
//                mRlIssueBottom.setPadding(mRlIssueBottom.getPaddingLeft()
//                        , mRlIssueBottom.getPaddingTop()
//                        , mRlIssueBottom.getPaddingRight()
//                        , -height + mRlBotPaddingBot);
//            }
//        });
//        animator.start();
//        disMissIconUnLock();
////        mTvDescription.addOnLayoutChangeListener(new OnLayoutChangeListener() {
////            @Override
////            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
////                mTvDescription.removeOnLayoutChangeListener(this);
////            }
////        });
//    }

    /**
     * 设置时间
     */
    public void setDateTime() {
        try {
            if (mTvDate != null && mTvTime1 != null) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                final String time1, time2, time4, time5;
                if (hour < 10) {
                    time1 = String.valueOf(0);
                    time2 = String.valueOf(hour);
                } else {
                    time1 = String.valueOf(hour / 10);
                    time2 = String.valueOf(hour % 10);
                }
                if (minute < 10) {
                    time4 = String.valueOf(0);
                    time5 = String.valueOf(minute);
                } else {
                    time4 = String.valueOf(minute / 10);
                    time5 = String.valueOf(minute % 10);
                }
//                Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
//                tv_time1.setTypeface(tf);
//                tv_time2.setTypeface(tf);
//                // tv_time3.setTypeface(tf);
//                tv_time4.setTypeface(tf);
//                tv_time5.setTypeface(tf);

                if (!mTvTime1.getText().toString().equals(time1)) {
                    mTvTime1.setText(time1);
//                    if (isFirstSetTime) {
//                        mTvTime1.setPivotX(mTvTime1.getWidth() / 2f);
//                        mTvTime1.setPivotY(mTvTime1.getHeight() / 2f);
//                    } else {
//                        mTvTime1.setPivotX(mTvTime1.getWidth() / 2f);
//                        mTvTime1.setPivotY(mTvTime1.getHeight() / 2f);
//                        if (animator_time1 == null) {
//                            animator_time1 = ObjectAnimator.ofFloat(mTvTime1, "rotationX", 0, 90, 0).setDuration(2000);
//                        }
//                        animator_time1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            private boolean changed = false;
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator arg0) {
//                                // 动画走到一般的时候变换数值
//                                if (arg0.getAnimatedFraction() > 0.5f && !changed) {
//                                    mTvTime1.setText(time1);
//                                    changed = true;
//                                }
//                            }
//                        });
//                        animator_time1.start();
//                    }
                }

                if (!mTvTime2.getText().toString().equals(time2)) {
                    mTvTime2.setText(time2);
//                    if (isFirstSetTime) {
//                        mTvTime2.setPivotX(mTvTime2.getWidth() / 2f);
//                        mTvTime2.setPivotY(mTvTime2.getHeight() / 2f);
//                    } else {
//                        mTvTime2.setPivotX(mTvTime2.getWidth() / 2f);
//                        mTvTime2.setPivotY(mTvTime2.getHeight() / 2f);
//                        if (animator_time2 == null) {
//                            animator_time2 = ObjectAnimator.ofFloat(mTvTime2, "rotationX", 0, 90, 0).setDuration(2000);
//                        }
//                        animator_time2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            private boolean changed = false;
//
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator arg0) {
//                                // 动画走到一般的时候变换数值
//                                if (arg0.getAnimatedFraction() > 0.5 && !changed) {
//                                    mTvTime2.setText(time2);
//                                    changed = true;
//                                }
//                            }
//                        });
//                        animator_time2.start();
//                    }
                }
                if (!mTvTime4.getText().toString().equals(time4)) {
                    mTvTime4.setText(time4);
//                    if (isFirstSetTime) {
//                        mTvTime4.setPivotX(mTvTime4.getWidth() / 2f);
//                        mTvTime4.setPivotY(mTvTime4.getHeight() / 2f);
//                    } else {
//                        mTvTime4.setPivotX(mTvTime4.getWidth() / 2f);
//                        mTvTime4.setPivotY(mTvTime4.getHeight() / 2f);
//                        if (animator_time4 == null) {
//                            animator_time4 = ObjectAnimator.ofFloat(mTvTime4, "rotationX", 0, 90, 0).setDuration(2000);
//                        }
//                        animator_time4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            private boolean changed = false;
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator arg0) {
//                                // 动画走到一般的时候变换数值
//                                if (arg0.getAnimatedFraction() > 0.5 && !changed) {
//                                    mTvTime4.setText(time4);
//                                    changed = true;
//                                }
//                            }
//                        });
//                        animator_time4.start();
//                    }
                }

                if (!mTvTime5.getText().toString().equals(time5)) {
                    mTvTime5.setText(time5);
//                    if (isFirstSetTime) {
//                        isFirstSetTime = false;
//                        mTvTime5.setPivotX(mTvTime5.getWidth() / 2f);
//                        mTvTime5.setPivotY(mTvTime5.getHeight() / 2f);
//                    } else {
//                        mTvTime5.setPivotX(mTvTime5.getWidth() / 2f);
//                        mTvTime5.setPivotY(mTvTime5.getHeight() / 2f);
//                        if (animator_time5 == null) {
//                            animator_time5 = ObjectAnimator.ofFloat(mTvTime5, "rotationX", 0, 90, 0).setDuration(2000);
//                        }
//                        animator_time5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            boolean changed = false;
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator arg0) {
//                                // 动画走到一般的时候变换数值
//                                if (arg0.getAnimatedFraction() > 0.5 && !changed) {
//                                    mTvTime5.setText(time5);
//                                    changed = true;
//                                }
//                            }
//                        });
//                        animator_time5.start();
//                    }
                }

                int month = c.get(Calendar.MONTH);
                String[] monthStrs = getResources().getStringArray(R.array.month_strings);
                String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
                int week = c.get(Calendar.DAY_OF_WEEK);
                String[] weekStrs = getResources().getStringArray(R.array.week_strings);
                Locale locale = getContext().getResources().getConfiguration().locale;
                if (locale.getLanguage().equals("en")) {
                    mTvDate.setText(new StringBuffer().append(weekStrs[week]).append(" ").append(monthStrs[month]).append(" ").append(day));
                } else {
                    mTvDate.setText(new StringBuffer(monthStrs[month]).append(day).append("日").append(" / ").append(weekStrs[week]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUnLocking(float f) {
        float bottoma = Math.max(f * 4.0f - 3, 0); //底部字的透明度变化
        mRlBottom.setAlpha(bottoma);

//        float topa = Math.min(1.0f, f * 4.0f);
//        mRlIssueTop.setAlpha(topa);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        disMissIconUnLock();
    }

    @Override
    public void onPageSelected(int position) {
        MobclickAgent.onEvent(getContext(), UmengEventIds.event_lockscreen_image_show);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        // TODO: 2016/2/16 实现不同的点击次数有些不同的快捷功能，如：手电筒---！！
    }
}
