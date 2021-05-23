package com.botian.recognition.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.botian.recognition.MainActivity2;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent actionIntent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(actionIntent.getAction())) {
            Intent intent = new Intent(context, MainActivity2.class);
            //Intent intent = new Intent(context, MainWebViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isAutoFaceAct", true);
            intent.putExtra("stepWorkType", 2);//1上班人脸、2下班人脸
            context.startActivity(intent);
        }
    }
}
