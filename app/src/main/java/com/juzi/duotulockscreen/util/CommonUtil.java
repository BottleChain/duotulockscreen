package com.juzi.duotulockscreen.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.text.TextUtils;

public class CommonUtil {
    private static final String TAG = "CommonUtil";
    private static long sLastClickTime;
    public static boolean isQuickClick() {
        long time = SystemClock.uptimeMillis();
        if ( time - sLastClickTime < 350) {
            return true;
        }
        sLastClickTime = time;
        return false;
    }

    /**
     * 获取应用程序版本的名称，清单文件中的versionName属性
     */
    public static String getLocalVersionName(Context c) {
        try {
            PackageManager manager = c.getPackageManager();
            PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    /**
     * 获取应用程序版本的名称，清单文件中的versionCode属性
     */
    public static int getLocalVersionCode(Context c) {
        try {
            PackageManager manager = c.getPackageManager();
            PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 获取渠道id
     */
    public static String getChannelID(Context c) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = c.getPackageManager().getApplicationInfo(
                    c.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo == null) {
            return "212 ";
        }
        return String.valueOf(appInfo.metaData.getInt("UMENG_CHANNEL"));
    }


    /**
     * 获取一些设备信息
     * @param context
     * @return
     */
    public static String getDeviceInfo(Context context) {
        try{
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if( TextUtils.isEmpty(device_id) ){
                device_id = mac;
            }

            if( TextUtils.isEmpty(device_id) ){
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
