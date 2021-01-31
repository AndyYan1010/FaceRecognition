package com.botian.recognition.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import com.botian.recognition.MyApplication;

public class PhoneInfoUtil {
    private static TelephonyManager tm;

    /**
     * 获取SIM硬件信息
     *
     * @return
     */
    public static TelephonyManager getTelephonyManager() {
        if (tm == null)
            tm = (TelephonyManager) MyApplication.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
//        StringBuffer sb = new StringBuffer();
//        sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
//        sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
//        sb.append("\nLine1Number = " + tm.getLine1Number());
//        sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
//        sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
//        sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
//        sb.append("\nNetworkType = " + tm.getNetworkType());
//        sb.append("\nPhoneType = " + tm.getPhoneType());
//        sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
//        sb.append("\nSimOperator = " + tm.getSimOperator());
//        sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
//        sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
//        sb.append("\nSimState = " + tm.getSimState());
//        sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
//        sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
//        LogUtils.i(sb.toString());
        return tm;
    }
    
    

    /**
     * 获取屏幕分辨率
     *
     * @return
     */
    public static int[] getMetrics() {
        WindowManager wm      = (WindowManager) MyApplication.applicationContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point   point   = new Point();
        display.getSize(point);
        int   width   = point.x;
        int   height  = point.y;
        int[] metrics = {width, height};
        return metrics;
    }

    /**
     * 设备厂商
     *
     * @return
     */
    public static String getPhoneBrand() {
        return Build.BOARD + "  " + Build.MANUFACTURER;
    }

    /**
     * 设备名称
     *
     * @return
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * 得到软件版本号
     *
     * @param context 上下文
     * @return 当前版本Code
     */
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            String packageName = context.getPackageName();
            verCode = context.getPackageManager()
                    .getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    /**
     * 获得APP名称
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager  packageManager  = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            appName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }
}
