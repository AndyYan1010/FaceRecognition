package com.botian.recognition.utils.devUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.jws.JwsIntents;
import com.botian.recognition.MyApplication;

public class MyInfraredReceiver extends BroadcastReceiver {
    private Object object;

    private Handler handler;

    public MyInfraredReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        object = extras.get(JwsIntents.EXTRA_IR_STATE_RESPONSE);
//        int anInt = extras.getInt(Intent.EXTRA_DRUNK_DRIVING_STATE);
        boolean state = intent.getBooleanExtra(JwsIntents.EXTRA_IR_STATE_RESPONSE, false);
        Log.i("byf-infraed", "onReceive: intent " + "object  " + object + "state " + state);
        if (state) {
//            WindowManagerUtil.wakeWindow();
//            WindowManagerUtil.wakeWindowV2();
            MyApplication.getJwsManager().jwsOpenLED();
            MyApplication.getJwsManager().jwsSetLcdBackLight(1);
            if (null != handler)
                handler.removeCallbacksAndMessages(null);
        } else {
            MyApplication.getJwsManager().jwsCloseLED();
            //延迟关闭灯
            if (null != handler)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.getJwsManager().jwsSetLcdBackLight(0);
                    }
                }, 1000 * 60 * 5);
        }
    }
}
