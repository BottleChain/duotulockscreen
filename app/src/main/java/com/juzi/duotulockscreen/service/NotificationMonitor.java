package com.juzi.duotulockscreen.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.juzi.duotulockscreen.util.GlobalConstantValues;
import com.juzi.duotulockscreen.util.LogHelper;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationMonitor extends NotificationListenerService {
    private static final String TAG = "NotificationMonitor";

//    public static final String ACTION_NLS_CONTROL = "com.seven.notificationlistenerdemo.NLSCONTROL";
//    private CancelNotificationReceiver mReceiver = new CancelNotificationReceiver();
//    class CancelNotificationReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            LogHelper.i(TAG, "CancelNotificationReceiver action = " + intent.getAction());
//            String action;
//            if (intent.getAction() != null) {
//                action = intent.getAction();
//                if (action.equals(ACTION_NLS_CONTROL)) {
//                    String command = intent.getStringExtra("command");
//                    LogHelper.i(TAG, "CancelNotificationReceiver command = " + command);
////                    if (TextUtils.equals(command, "cancel_last")) {
////                        if (mCurrentNotifications != null && mCurrentNotificationsCounts >= 1) {
////                            StatusBarNotification sbnn = getCurrentNotifications()[mCurrentNotificationsCounts - 1];
////                            cancelNotification(sbnn.getPackageName(), sbnn.getTag(), sbnn.getId());
////                        }
////                    } else if (TextUtils.equals(command, "cancel_all")) {
////                        cancelAllNotifications();
////                    } else if (TextUtils.equals(command, "cancel_one")) {
////                        StatusBarNotification[] sbnns = getCurrentNotifications();
////                        if (sbnns != null && sbnns.length > 0) {
////                            for (int i = 0; i < sbnns.length; i++) {
////                                StatusBarNotification sbnn = sbnns[i];
////                                if (sbnn.getPackageName().equals(intent.getStringExtra("packageName"))) {
////                                    cancelNotification(sbnn.getPackageName(), sbnn.getTag(), sbnn.getId());
////                                    break;
////                                }
////                            }
////                        }
////                    }
//                }
//            }
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.i(TAG, "onCreate...");
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLockScreen = sp1.getBoolean(GlobalConstantValues.KEY_SHARED_LOCKSCREEN, false);
        if (isLockScreen) {
            Intent servie = new Intent(getBaseContext(), LockScreenService.class);
            servie.putExtra(LockScreenService.SERVICE_TYPE, 0);
            startService(servie);
        }
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_NLS_CONTROL);
//        registerReceiver(mReceiver, filter);
        //mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogHelper.i(TAG, "onDestroy...");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.i(TAG, "onBind...");
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        LogHelper.i(TAG, "onNotificationPosted...");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        LogHelper.i(TAG, "onNotificationRemoved...");
        LogHelper.i(TAG, sbn.toString());
    }
}
