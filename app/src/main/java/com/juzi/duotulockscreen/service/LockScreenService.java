package com.juzi.duotulockscreen.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.juzi.duotulockscreen.View.LockScreenLayout;
import com.juzi.duotulockscreen.View.LockView;
import com.juzi.duotulockscreen.activity.LockScreenNullActivity;
import com.juzi.duotulockscreen.receiver.LockScreenReceiver;
import com.juzi.duotulockscreen.util.LogHelper;

public class LockScreenService extends Service implements LockView.onUnLockListener {

    /**
     * 0默认不操作，1显示锁屏，2取消锁屏
     */
    public static final String SERVICE_TYPE = "service_type";
    private LockScreenReceiver mReceiver;
    private KeyguardManager.KeyguardLock mKeyguardLock;
    private KeyguardManager mKeyguardManager;
    private WindowManager.LayoutParams mParams;
    // 创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private LockScreenLayout mLockScreenLayout;
//    private boolean mHasLockImg; //至少有一张锁屏图片，才可以显示锁屏
    /**
     * 是否是打电话期间
     */
    public static boolean isCalling = false;
    /**
     * 当前锁屏状态：true锁屏
     */
    public static boolean isLock = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_TIME_TICK); //每分钟一次的更新时间
        filter.addAction(LockScreenReceiver.ACTION_ALARM_ALERT);
        filter.addAction(LockScreenReceiver.ACTION_ALARM_DONE);
        filter.addAction(LockScreenReceiver.ACTION_ALARM_DISMISS);
        filter.addAction(LockScreenReceiver.ACTION_ALARM_SNOOZE);
//        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.setPriority(Integer.MAX_VALUE);

        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, filter);

        LogHelper.i("LockscreenService", "onCreate --");

        TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {
            boolean isLockCalling;

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE: // 空闲
                        isCalling = false;
                        if (isLockCalling) {
                            // 在锁屏下，来电到挂断启动锁屏;
                            showLockView();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING: // 来电
                        //来电话了 。则取消锁屏;
                        isCalling = true;
                        isLockCalling = isLock;
                        disMissLockView();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机（正在通话中）
                        isCalling = true;
                        //"通话中";
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        mKeyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("");
        mKeyguardLock.disableKeyguard();

//        Dao dao;
//        try {
//            dao = MyDatabaseHelper.getInstance(getBaseContext()).getDaoQuickly(LockScreenImgBean.class);
//            final List list = dao.queryForAll();
//            if (list != null && list.size() > 0) {
//                if (!mHasLockImg) {
//                    //至少有一张锁屏图片，并且锁屏
//                    mHasLockImg = true;
//                    mKeyguardLock.disableKeyguard();
//                } else {
//                    mHasLockImg = false;
//                }
//            }
////            else {
////                if (mHasLockImg) {
////                    mHasLockImg = false;
////                    if (mKeyguardLock == null) {
////                        mKeyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
////                        mKeyguardLock = mKeyguardManager.newKeyguardLock("");
////                    }
////                    //mKeyguardLock.reenableKeyguard();
////                } //没有一张图片
////            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //保证进程不被杀的一个通知监听器
            Intent notify = new Intent(getBaseContext(), NotificationMonitor.class);
            startService(notify);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLockScreenLayout == null) {
            initWindowView();
        }
        if (intent != null) {
            int type = intent.getIntExtra(SERVICE_TYPE, 0);
            LogHelper.i("LockscreenService", "onStartCommand is called! type = " + type);
            switch (type) {
                case 1: //锁屏
                    showLockView();
                    break;
                case 2: //解锁
                    disMissLockView();
                    break;
                case 3: //锁屏图片有变动，更新数据
                    updataLockScreenImgs();
                    break;
                case 4: //更新时间
                    if (isLock) {
                        mLockScreenLayout.setDateTime();
                    }
                    break;
                case 5: //亮屏
                    if (isLock && mLockScreenLayout != null) {
                        mLockScreenLayout.wakeUpScreen();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    /**
     * 更新锁屏图片
     */
    private void updataLockScreenImgs() {
        mLockScreenLayout.initData();
    }

    public void showLockView() {
//        if (mKeyguardLock == null) {
//            mKeyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
//            mKeyguardLock = mKeyguardManager.newKeyguardLock("");
//        }

        //mKeyguardLock.reenableKeyguard();
        //mKeyguardLock.disableKeyguard();

        mLockScreenLayout.setVisibility(View.VISIBLE);
        mLockScreenLayout.offScreen();

        if (isLock) {
             //如果已经锁了 ，则换图
            mLockScreenLayout.showNextImg();
        } else {
            if (mLockScreenLayout.getParent() != null) {
                mWindowManager.removeView(mLockScreenLayout);
            }
            mLockScreenLayout.showNextImg();
            mWindowManager.addView(mLockScreenLayout, mParams);
        }
        Intent intent = new Intent(getBaseContext(), LockScreenNullActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("startType", 1);
        startActivity(intent);
        isLock = true;
    }

    public void disMissLockView() {
        if (isLock) {
            mWindowManager.removeView(mLockScreenLayout);
            //needShowNext = true;
            Intent intent1 = new Intent(getBaseContext(), LockScreenNullActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("startType", 0);
            startActivity(intent1);
            isLock = false;
        }
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        if (mKeyguardLock != null) {
            mKeyguardLock.reenableKeyguard();
        }
//        if(mIntent!=null){
//            startService(mIntent);
//        }
    }

    private void initWindowView() {
        if (mParams == null) {
            mParams = new WindowManager.LayoutParams();
        }

        // 获取的是WindowManagerImpl.CompatModeWrapper
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        // 设置window type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 设置图片格式，效果为背景透明
//        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.format = PixelFormat.RGBA_8888;

//         mParams.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        // 设置全屏显示
        mParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        if (android.os.Build.MANUFACTURER.equals("LENOVO") || android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // 设置Window flag
            mParams.flags = mParams.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            mParams.flags = mParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        // 调整悬浮窗显示的停靠位置为左侧置顶
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        mParams.x = 0;
        mParams.y = 0;
        // 不起作用是为啥
        mParams.flags = mParams.flags | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        mParams.flags = mParams.flags | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        // 不起作用是为啥
        // 屏幕方向
        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        // 设置悬浮窗口长宽数据
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        // 获取浮动窗口视图所在布局
        if (mLockScreenLayout == null) {
            mLockScreenLayout = new LockScreenLayout(this);
            mLockScreenLayout.setOnUnLockListener(this);
        }
        mLockScreenLayout.setFocusableInTouchMode(true);
    }

    @Override
    public void onUnLockSuccess() {
        mLockScreenLayout.setVisibility(View.INVISIBLE);
        mLockScreenLayout.onUnLockSuccess();
        disMissLockView();
//        Intent intent = new Intent(this, LockScreenService.class);
//        intent.putExtra(LockScreenService.SERVICE_TYPE, 2); //解锁
//        mContext.startService(intent);
    }

    @Override
    public void onUnLockFailed() {

    }

    @Override
    public void onUnLocking(float f) {
        if (mLockScreenLayout != null) {
            mLockScreenLayout.onUnLocking(f);
        }
    }
}
