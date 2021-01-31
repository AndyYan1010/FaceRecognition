package com.botian.recognition.utils.netUtils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @创建者 AndyYan
 * @创建时间 2018/4/12 19:43
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class ThreadUtils {

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private static Executor sExecutor = Executors.newSingleThreadExecutor();

    public static void runOnSubThread(Runnable runnable) {
        sExecutor.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        sHandler.post(runnable);
    }

    public static void sleepSec(long waitSecTime) {
        try {
            Thread.sleep(waitSecTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
