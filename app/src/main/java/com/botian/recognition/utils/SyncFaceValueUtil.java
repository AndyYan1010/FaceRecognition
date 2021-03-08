package com.botian.recognition.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.botian.recognition.NetConfig;
import com.botian.recognition.bean.FnoteListBean;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.ThreadUtils;
import com.google.gson.Gson;
import com.tencent.cloud.ai.fr.sdksupport.FloatsFileHelper;

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Request;

import static com.botian.recognition.sdksupport.SaveFeaturesToFileStep.FACE_LIB_PATH;

/**
 * Created by Andy on 3/7/21.
 * Modification time 3/7/21.
 * Describe 同步人脸数据
 */
public class SyncFaceValueUtil {
    private static Context mContext;

    public static void startSyncValue(Context context) {
        mContext = context;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //更新设备人脸库
                syncFaceValue();
            }
        };
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");// 可以方便地修改日期格式
        String hehe = dateFormat.format(now);
        Timer timer = new Timer(true);
        timer.schedule(task, strToDateLong(hehe + " 04:00:00"));
    }

    /**
     * string类型时间转换为date
     *
     * @param strDate
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /***同步人脸特种值*/
    private static void syncFaceValue() {
        ProgressDialogUtil.startShow(mContext, "正在同步人脸特征值信息...");
        OkHttpUtils.getInstance().doGet(NetConfig.FNOTELIST, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("网络错误，同步失败！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ProgressDialogUtil.hideDialog();
                    ToastUtils.showToast("网络请求错误，同步失败！");
                    return;
                }
                Gson gson = new Gson();
                FnoteListBean resultBean = gson.fromJson(resbody, FnoteListBean.class);
                if (!"1".equals(resultBean.getCode())) {
                    ProgressDialogUtil.hideDialog();
                    ToastUtils.showToast("数据请求错误，同步失败！");
                    return;
                }
                ToastUtils.showToast(resultBean.getMessage());
                //存储人脸特征值
                keepStoreFaceValue(resultBean.getList());
            }
        });
    }

    /***存储特征值
     * @param list*/
    private static void keepStoreFaceValue(List<FnoteListBean.ListBean> list) {
        if (null == list || list.size() == 0) {
            ProgressDialogUtil.hideDialog();
            return;
        }
        ProgressDialogUtil.startShow(mContext, "正在存储特征值");
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                for (FnoteListBean.ListBean bean : list) {
                    String filePath = new File(FACE_LIB_PATH + bean.getId() + ".feature").getAbsolutePath();
                    boolean writeResult = FloatsFileHelper.writeFloatsToFile(CommonUtil.getFloatArray(bean.getFnote()), filePath);
                }
                ThreadUtils.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogUtil.hideDialog();
                        ToastUtils.showToast("特征值存储成功");
                    }
                });
            }
        });
    }
}

