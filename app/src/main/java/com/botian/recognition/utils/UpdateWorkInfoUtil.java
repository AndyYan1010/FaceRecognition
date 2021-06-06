package com.botian.recognition.utils;

import android.util.Log;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.bean.CheckFaceHistory;
import com.botian.recognition.bean.UpCheckResultBean;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.btface.greendaodemo.DaoSession;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class UpdateWorkInfoUtil {
    private static UpdateWorkInfoUtil     instance;
    private        List<CheckFaceHistory> mCheckFaceHistories;

    private UpdateWorkInfoUtil() {
    }

    public static UpdateWorkInfoUtil getInstance() {
        if (null == instance) {
            synchronized (UpdateWorkInfoUtil.class) {
                if (null == instance)
                    instance = new UpdateWorkInfoUtil();
            }
        }
        return instance;
    }

    /***上传打卡记录*/
    public void sendWorkInfo() {
        if (isHasWorkInfo()) {
            //开始上传
            JSONArray peoplelist = new JSONArray();
            for (CheckFaceHistory workInfo : mCheckFaceHistories) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userid", workInfo.getUserID());
                    //jsonObject.put("username", "");
                    jsonObject.put("type", workInfo.getCheckType());
                    jsonObject.put("ftime", workInfo.getFtime());
                    peoplelist.put(jsonObject);
                } catch (Exception e) {
                }
            }
            if (peoplelist.length() == 0) {
                return;
            }
            RequestParamsFM params = new RequestParamsFM();
            params.put("peoplelist", peoplelist);
            params.setUseJsonStreamer(true);
            OkHttpUtils.getInstance().doPost(NetConfig.UPDATEWORK, params, new OkHttpUtils.HttpCallBack() {
                @Override
                public void onError(Request request, IOException e) {
                    //上传失败
                }

                @Override
                public void onSuccess(int code, String resbody) {
                    if (code != 200) {
                        return;
                    }
                    Gson              gson       = new Gson();
                    UpCheckResultBean resultBean = gson.fromJson(resbody, UpCheckResultBean.class);
                    if ("1".equals(resultBean.getCode())) {
                        if (mCheckFaceHistories != null)
                            mCheckFaceHistories.clear();
                        //清除本地记录
                        clearLocalCheckHistory();
                    }
                }
            });
        }
    }

    /***是否有打卡记录*/
    private boolean isHasWorkInfo() {
        List<CheckFaceHistory> checkFaceHistories = readLocalCheckHistory();
        if (checkFaceHistories != null && checkFaceHistories.size() > 0) {
            if (mCheckFaceHistories == null) {
                mCheckFaceHistories = new ArrayList<>();
            } else {
                mCheckFaceHistories.clear();
            }
            mCheckFaceHistories.addAll(checkFaceHistories);
            return true;
        }
        return false;
    }

    /****读取本地数据库打卡信息*/
    private List<CheckFaceHistory> readLocalCheckHistory() {
        //查询全部
        return MyApplication.getDaoSession().loadAll(CheckFaceHistory.class);
    }

    /****清除本地打卡记录*/
    private void clearLocalCheckHistory() {
        try {
            MyApplication.getDaoSession().deleteAll(CheckFaceHistory.class);
            //MyApplication.getDaoSession().clear();
            Log.d("ClearSuc","清除记录成功");
        } catch (Exception e) {
            Log.d("ClearFail","清除记录失败");
        }
    }

    /***保存打卡记录*/
    public void keepCheckHistory(JSONArray peoplelist, OnKeepWorkInfoListener onKeepWorkInfoListener) {
        if (peoplelist == null || peoplelist.length() == 0) {
            if (onKeepWorkInfoListener != null)
                onKeepWorkInfoListener.onFail();
            return;
        }
        try {
            for (int i = 0; i < peoplelist.length(); i++) {
                JSONObject jsonObject = (JSONObject) peoplelist.get(i);
                //获取GreenDao数据库操作对象
                DaoSession       daoSession       = MyApplication.getDaoSession();
                CheckFaceHistory checkFaceHistory = new CheckFaceHistory();
                checkFaceHistory.setUserID(jsonObject.getString("userid"));
                checkFaceHistory.setCheckType(jsonObject.getString("type"));
                checkFaceHistory.setFtime(jsonObject.getString("ftime"));
                daoSession.insert(checkFaceHistory);
            }
            //保存成功
            if (onKeepWorkInfoListener != null)
                onKeepWorkInfoListener.onKeepSuccess();
        } catch (Exception e) {
            if (onKeepWorkInfoListener != null)
                onKeepWorkInfoListener.onFail();
        }
    }

    public interface OnKeepWorkInfoListener {
        void onKeepSuccess();

        void onFail();
    }
}
