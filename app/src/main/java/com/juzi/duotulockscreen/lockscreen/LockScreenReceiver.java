package com.juzi.duotulockscreen.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.juzi.duotulockscreen.util.LogHelper;

public class LockScreenReceiver extends BroadcastReceiver {
    static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    public static final String ACTION_ALARM_ALERT = "com.android.deskclock.ALARM_ALERT";
    public static final String ACTION_ALARM_DONE = "com.android.deskclock.ALARM_DONE";
    public static final String ACTION_ALARM_DISMISS = "com.android.deskclock.ALARM_DISMISS";
    public static final String ACTION_ALARM_SNOOZE = "com.android.deskclock.ALARM_SNOOZE";
    /**
     * 网络状态改变
     */
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    /**
     * 闹铃响时是否已经锁屏
     */
    private boolean isLockAlarmRing;

    /**
     * 闹铃响期间
     */
//    private boolean isAlarmRing;

    private static boolean isStartService;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action=intent.getAction();
        LogHelper.i("LockScreenReceiver", "onReceive ACTION = " + action);
        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                if (!LockScreenService.isCalling) { //来电话期间不锁屏
                    Intent i = new Intent(context, LockScreenService.class);
                    i.putExtra(LockScreenService.SERVICE_TYPE, 1); //锁屏type
                    context.startService(i);
                }
                break;
            case Intent.ACTION_SCREEN_ON: { //亮屏，初始化一些锁屏上的东西
                Intent servie = new Intent(context, LockScreenService.class);
                servie.putExtra(LockScreenService.SERVICE_TYPE, 5);//亮屏type
                context.startService(servie);
                break;
            }
            case ACTION_ALARM_ALERT: { //开始时钟定时响铃，解锁----
                isLockAlarmRing = LockScreenService.isLock;
                Intent servie = new Intent(context, LockScreenService.class);
                servie.putExtra(LockScreenService.SERVICE_TYPE, 2); //解锁type
                context.startService(servie);
                break;
            }
            case Intent.ACTION_BOOT_COMPLETED: {// 开机启动
//                SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(context);
//                boolean isLockScreen = sp1.getBoolean(GlobalConstantValues.KEY_SHARED_LOCKSCREEN, false);
//                if (!isLockScreen) {
//                    return;
//                }
                Intent servie = new Intent(context, LockScreenService.class);
                servie.putExtra(LockScreenService.SERVICE_TYPE, 0);
                context.startService(servie);
                //            if (isStartService) {
                //                return;
                //            }
                //            isStartService = true;
                //            SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(context);
                //            boolean isLockScreen = sp1.getBoolean(GlobalConstantValues.KEY_SHARED_LOCKSCREEN, true);
                //            if (!isLockScreen) {
                //                return;
                //            }
                //            Intent lock = new Intent(context, LockScreenService.class);
                //            Dao dao;
                //            try {
                //                dao = MyDatabaseHelper.getInstance(context).getDaoQuickly(LockScreenImgBean.class);
                //                final List list = dao.queryForAll();
                //                if (list != null && list.size() > 0) {
                //                    lock.putExtra(LockScreenService.SERVICE_TYPE, 5); //至少有一张锁屏图片，并且锁屏
                //                } else {
                //                    lock.putExtra(LockScreenService.SERVICE_TYPE, 6); //没有一张图片
                //                }
                //            } catch (SQLException e) {
                //                e.printStackTrace();
                //            }
                //            context.startService(lock);
                break;
            }
            case ACTION_ALARM_DONE:
            case ACTION_ALARM_DISMISS:
            case ACTION_ALARM_SNOOZE:
                //系统闹铃不响了，如果之前是锁屏，恢复锁屏状态，如果之前没有锁屏，nothing
                if (isLockAlarmRing) {
                    Intent servie = new Intent(context, LockScreenService.class);
                    servie.putExtra(LockScreenService.SERVICE_TYPE, 1); //锁屏的type
                    context.startService(servie);
                }
                break;
            case Intent.ACTION_TIME_TICK: {
                // 改变系统时间
                Intent servie = new Intent(context, LockScreenService.class);
                servie.putExtra(LockScreenService.SERVICE_TYPE, 4); //改变时间type
                context.startService(servie);
                break;
            }
            case ACTION_CONNECTIVITY_CHANGE: {
                //网络链接改变的广播，用来在开机的时候调起服务，大部分手机开机都会发这个广播，并且不用开机启动的权限
//                SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(context);
//                boolean isLockScreen = sp1.getBoolean(GlobalConstantValues.KEY_SHARED_LOCKSCREEN, false);
//                if (!isLockScreen) {
//                    return;
//                }
                Intent servie = new Intent(context, LockScreenService.class);
                servie.putExtra(LockScreenService.SERVICE_TYPE, 0);
                context.startService(servie);
                break;
            }
        }

//        if (action.equals(Intent.no)) {
//            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
//            if (reason != null) {
//                LogHelper.i("wangzixu", "onReceive reason:" + reason);
////                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
////                    Intent i = new Intent(context, LockScreenService.class);
////                    i.putExtra(LockScreenService.SERVICE_TYPE, 2);
////                    context.startService(i);
////                } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
////
////                }
//            }
//        }
    }
}
