package com.botian.recognition.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.activity.CameraPhotoActivity;
import com.botian.recognition.bean.CommonBean;
import com.botian.recognition.bean.ReportPersonListBean;
import com.botian.recognition.utils.PopupOpenHelper;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;

public class PersonsShowAdapter extends BaseQuickAdapter<ReportPersonListBean.ListBean, BaseViewHolder> {
    private Context         mContext;
    private List            mList;
    private PopupOpenHelper mOpenHelper;
    private int             REQUEST_CODE_GET_FACE = 10001;

    public PersonsShowAdapter(Context context, int layoutResId, List<ReportPersonListBean.ListBean> list) {
        super(layoutResId, list);
        mContext = context;
        mList    = list;
    }

    public void setPopHelper(PopupOpenHelper openHelper) {
        mOpenHelper = openHelper;
    }

    @Override
    protected void convert(BaseViewHolder helper, ReportPersonListBean.ListBean item) {
        helper.setText(R.id.tv_xuHao, "" + (helper.getPosition() + 1));
        helper.setText(R.id.tv_person, item.getFname());
        helper.setText(R.id.tv_sTime, item.getStarttime());
        helper.setText(R.id.tv_eTime, item.getEndtime());

        View tv_open = helper.getView(R.id.tv_open);
        View tv_del  = helper.getView(R.id.tv_del);
        tv_open.setOnClickListener(v -> {
            //新增汇报人员
            addPersonForOpenMachine(helper);
        });
        tv_del.setOnClickListener(v -> {
            //移除人员
            delPerson(helper, item);
        });
    }

    /***移除人员
     * @param helper
     * @param item*/
    private void delPerson(BaseViewHolder helper, ReportPersonListBean.ListBean item) {
        RequestParamsFM params = new RequestParamsFM();
        params.put("workid", MyApplication.workID);
        params.put("userno", item.getId());
        params.put("starttime", item.getStarttime());
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.DELHBRY, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误");
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
                if (!"1".equals(commonBean.getCode())) {
                    return;
                }
                mList.remove(helper.getPosition());
                notifyDataSetChanged();
            }
        });
    }

    /****增加开机人员
     * @param helper*/
    private void addPersonForOpenMachine(BaseViewHolder helper) {
        //跳转人脸识别界面
        Intent intent = new Intent(mContext, CameraPhotoActivity.class);
        intent.putExtra("ftype", 2);
        intent.putExtra("itemIndex", helper.getPosition());
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE_GET_FACE);
        mOpenHelper.dismiss();
    }
}
