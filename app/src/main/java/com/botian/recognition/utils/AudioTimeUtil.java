package com.botian.recognition.utils;

import com.botian.recognition.utils.netUtils.ThreadUtils;

import java.util.Timer;
import java.util.TimerTask;

public class AudioTimeUtil {
    private static AudioTimeUtil timeUtilInstance;
    private        int           waitTime    = 2;
    private        TimeListener  timeListener;
    private        Timer         timer;
    private        boolean       isCountDown = false;

    public static AudioTimeUtil getInstance() {
        if (null == timeUtilInstance) {
            synchronized (AudioTimeUtil.class) {
                if (null == timeUtilInstance)
                    timeUtilInstance = new AudioTimeUtil();
            }
        }
        return timeUtilInstance;
    }

    public AudioTimeUtil initData(int waitTime) {
        this.waitTime = waitTime;
        if (null != timer && isCountDown)
            timer.cancel();
        isCountDown = false;
        return timeUtilInstance;
    }

    /***
     * 开始倒计时任务
     * */
    public void countDownTime() {
        isCountDown = true;
        timer       = new Timer();
        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (null != timeListener)
                    timeListener.onStart("" + waitTime % 60);
            }
        });
        timer.schedule(new TimerTask() {
            public void run() {
                if (waitTime != 0) {
                    waitTime--;
                    isCountDown = true;
                    long ss = waitTime % 60;
                    //System.out.println("还剩" + ss + "秒");
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null != timeListener)
                                timeListener.onChange("" + (ss + 1));
                        }
                    });
                } else {
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null != timeListener)
                                timeListener.onFinish();
                        }
                    });
                    timer.cancel();
                    isCountDown = false;
                }
            }
        }, 1000, 1000);
    }

    /***
     * 是否在倒计时
     * */
    public boolean isCountDown() {
        return isCountDown;
    }

    /***清除缓存数据*/
    public void clearOldData() {
        if (null != timer && isCountDown)
            timer.cancel();
        isCountDown = false;
    }

    /***
     * 取消倒计时
     * */
    public void cancelCountDown() {
        if (null != timer)
            timer.cancel();
        isCountDown = false;
        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (null != timeListener)
                    timeListener.onCancel();
            }
        });
    }

    /***关闭*/
    public void stop() {
        if (null != timer)
            timer.cancel();
        isCountDown = false;
    }

    /**
     * 设置时间器监听
     */
    public AudioTimeUtil setOnTimeListener(TimeListener timeListener) {
        this.timeListener = timeListener;
        countDownTime();
        return timeUtilInstance;
    }

    public interface TimeListener {
        void onStart(String cont);

        void onChange(String cont);

        void onCancel();

        void onFinish();
    }
}
