package com.botian.recognition.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import com.botian.recognition.MyApplication;

public class WindowManagerUtil {

    public static void wakeWindow() {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) MyApplication.applicationContext.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
        Log.e("xxx", "wake");
    }

    public static void wakeWindowV2() {
        KeyguardManager km = (KeyguardManager) MyApplication.applicationContext.getSystemService(Context.KEYGUARD_SERVICE);// 得到键盘锁管理器
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        kl.disableKeyguard(); // 解锁
        PowerManager pm = (PowerManager) MyApplication.applicationContext.getSystemService(Context.POWER_SERVICE);
        // 点亮屏幕
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock pm_wl = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        pm_wl.acquire();
        pm_wl.release();//发出命令
    }

    //覆盖息屏页面，在需要跳转的activity中添加

    /****记得添加在super之前。。
     这样该activity就会在锁屏页面的上面显示。
     比如我们收到语音消息的推送时，首先调用wake()方法；
     然后跳转activity即可。*/
    public static void setActivity(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }


    //启用屏幕常亮功能  
    private void turnOnScreen(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = 1;
        activity.getWindow().setAttributes(params);
    }

    //关闭 屏幕常亮功能  
    private void turnOffScreen(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.screenBrightness = 0.005f;
        activity.getWindow().setAttributes(params);
    }
}
