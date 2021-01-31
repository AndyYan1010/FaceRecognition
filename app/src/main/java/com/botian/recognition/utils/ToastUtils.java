package com.botian.recognition.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import com.botian.recognition.MyApplication;


/**
 * @创建者 AndyYan
 * @创建时间 2018/4/12 19:43
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class ToastUtils {//吐司Toast工具类

    private static Toast sToast;

    public static void showToast(Context context, String msg) {
        if (sToast == null) {
            sToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        //如果这个Toast已经在显示了，那么这里会立即修改文本
        sToast.setText(msg);
        sToast.show();
    }

    public static void showToast(String msg) {
        try {
            //if (sToast == null) {
            //    sToast = Toast.makeText(MyApplication.applicationContext, msg, Toast.LENGTH_SHORT);
            //}
            ////如果这个Toast已经在显示了，那么这里会立即修改文本
            //sToast.setText(msg);
            //sToast.show();
            com.hjq.toast.ToastUtils.show(msg);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void show1per2Toast(String msg) {
        try {
            if (sToast == null) {
                sToast = Toast.makeText(MyApplication.applicationContext, msg, Toast.LENGTH_SHORT);
            } else {
                sToast.setText(msg);
            }
            WindowManager windowManager = (WindowManager) MyApplication.applicationContext.getSystemService(MyApplication.applicationContext.WINDOW_SERVICE);
            Point         size          = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            sToast.setGravity(Gravity.TOP, 0, (size.y * 1) / 2);
            sToast.show();
        } catch (Exception e) {
        }
    }
}
