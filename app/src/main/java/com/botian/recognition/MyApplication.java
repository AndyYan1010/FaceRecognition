package com.botian.recognition;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.android.jws.JwsManager;
import com.btface.greendaodemo.DaoMaster;
import com.btface.greendaodemo.DaoSession;

import java.util.ArrayList;


/**
 * Andy
 */
public class MyApplication extends Application {
    public static Context             applicationContext;
    public static ArrayList<Activity> listActivity = new ArrayList<Activity>();//用来装载activity
    public static String              tempUserID   = "";
    public static String              tempUserName = "";
    public static String              devID;
    public static String              workID;//班线id
    public static String              workStartTime;//班线开启时间
    public static String              workUserName;//班线人员姓名
    public static String              workGongXu;//班线工序
    public static int                 flag         = -1;//判断是否被回收
    public static boolean             flagScreen   = false;//是否是竖屏设备


    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        com.hjq.toast.ToastUtils.init(this);
        initGreenDao();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    static JwsManager jwsManager;

    public static JwsManager getJwsManager() {
        if (jwsManager == null) {
            jwsManager = JwsManager.create(applicationContext);
        }
        return jwsManager;
    }

    private void initGreenDao() {
        DaoMaster.DevOpenHelper helper    = new DaoMaster.DevOpenHelper(this, "checkFaceHistory.db");
        DaoMaster               daoMaster = new DaoMaster(helper.getWritableDatabase());
        daoSession = daoMaster.newSession();
    }

    private static DaoSession daoSession;

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    /**
     * 退出程序
     */
    public static void mineDoExit() {
        try {
            for (Activity activity : listActivity) {
                activity.finish();
            }
            // 结束进程
            System.exit(0);
        } catch (Exception e) {
        }
    }
}
