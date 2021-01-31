package com.botian.recognition.adapter;

import android.content.Context;
import android.view.View;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.activity.CameraPhotoActivity;
import com.botian.recognition.bean.CommonBean;
import com.botian.recognition.bean.UpResultBean;
import com.botian.recognition.utils.MyAlertDialogHelper;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;

public class PersonListAdapter extends BaseQuickAdapter<UpResultBean.ListBean, BaseViewHolder> {
    private Context                     mContext;
    private List<UpResultBean.ListBean> mList;

    public PersonListAdapter(Context context, int layoutResId, List<UpResultBean.ListBean> list) {
        super(layoutResId, list);
        mContext = context;
        mList    = list;
    }

    @Override
    protected void convert(BaseViewHolder helper, UpResultBean.ListBean item) {
        helper.setText(R.id.tv_name, item.getUsername());
        helper.setText(R.id.tv_msg, item.getMessage());
        if ("0".equals(item.getFtype())) {
            helper.setTextColor(R.id.tv_msg, mContext.getResources().getColor(R.color.red));
        } else if ("1".equals(item.getFtype())) {
            helper.setTextColor(R.id.tv_msg, mContext.getResources().getColor(R.color.green_100));
        } else if ("2".equals(item.getFtype())) {
            helper.setTextColor(R.id.tv_msg, mContext.getResources().getColor(R.color.orange));
        } else {
            helper.setTextColor(R.id.tv_msg, mContext.getResources().getColor(R.color.black));
        }
        View tv_open = helper.getView(R.id.tv_open);
        tv_open.setOnClickListener(v -> {
            //确认开机
            makeSure2OpenMachine(helper);
        });
        //为开机时
        if (2 == ((CameraPhotoActivity) mContext).getFtype() && "2".equals(mList.get(helper.getPosition()).getFtype())) {
            tv_open.setVisibility(View.VISIBLE);
        } else {
            tv_open.setVisibility(View.INVISIBLE);
        }
    }

    /***确认开机
     * @param helper*/
    private void makeSure2OpenMachine(BaseViewHolder helper) {
        MyAlertDialogHelper dialogHelper = new MyAlertDialogHelper();
        dialogHelper.setDataNoView(mContext, "提示", "您确认开机？");
        dialogHelper.setDialogClicker("确认", "取消", new MyAlertDialogHelper.DialogClickListener() {
            @Override
            public void onPositive() {
                //提交数据
                subSureData(helper, dialogHelper);
            }

            @Override
            public void onNegative() {
                dialogHelper.disMiss();
            }
        });
        dialogHelper.show();
    }

    private void subSureData(BaseViewHolder helper, MyAlertDialogHelper dialogHelper) {
        RequestParamsFM params = new RequestParamsFM();
        params.put("workid", MyApplication.workID);
        params.put("username", mList.get(helper.getPosition()).getUsername());
        params.put("userid", mList.get(helper.getPosition()).getUserid());
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.ISKAIJI, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络连接错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    return;
                }
                Gson       gson       = new Gson();
                CommonBean commonBean = gson.fromJson(resbody, CommonBean.class);
                ToastUtils.showToast(commonBean.getMessage());
                if ("1".equals(commonBean.getCode())) {
                    dialogHelper.disMiss();
                }
            }
        });
    }
}
