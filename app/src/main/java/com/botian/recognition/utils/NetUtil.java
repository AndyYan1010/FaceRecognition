package com.botian.recognition.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.botian.recognition.MyApplication;

public class NetUtil {

    public static boolean checkInternetStatus() {
        ConnectivityManager manager     = (ConnectivityManager) MyApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo         networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
