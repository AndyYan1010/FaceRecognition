package com.botian.recognition.sdksupport;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.activity.RegWithAndroidCameraActivity;
import com.botian.recognition.bean.PersonListResultBean;
import com.botian.recognition.utils.PopupOpenHelper;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Request;

public class ShowCheckFaceDialogView {
    private MyDialog         dialog;
    private View             view;
    private Context          context;
    private String           selectName;
    private String           selectID;
    private EditText         et_no;
    private TextView         tv_search;
    private TextView         mTv_name;
    private Bitmap           mFace;
    private TextView         mTv_sure;
    private Handler          handler;
    private int              time = 3;
    private OnSelectListener mOnSelectListener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initView(Context context) {
        this.context = context;
        ((RegWithAndroidCameraActivity) context).setDialogStatue(true);
        ((RegWithAndroidCameraActivity) context).setSelectButton(-1);
        dialog = new MyDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        view = LayoutInflater.from(context).inflate(R.layout.confirm_reg_face, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        initDialogView();
        dialog.setContentView(view);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (null != handler) {
                    handler.removeCallbacksAndMessages(null);
                    handler = null;
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                time--;
                if (time == 0) {
                    mTv_sure.setText("确认注册");
                    handler.removeMessages(0);
                    return;
                }
                mTv_sure.setText("确认注册（" + time + "）");
                handler.sendEmptyMessageDelayed(0, 1000);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initDialogView() {
        et_no     = view.findViewById(R.id.et_no);
        tv_search = view.findViewById(R.id.tv_search);
        mTv_name  = view.findViewById(R.id.tv_name);
        et_no.setShowSoftInputOnFocus(false);
        setViewListener();
    }

    private void setViewListener() {
        tv_search.setOnClickListener(v -> {
            String etNo = String.valueOf(et_no.getText());
            if (TextUtils.isEmpty(etNo.trim())) {
                ToastUtils.showToast("工号不能为空！");
                return;
            }
            //通过工号查询姓名
            searchNameByNo(etNo);
        });
        view.findViewById(R.id.reg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(selectName)) {
                    ToastUtils.showToast("姓名不能为空！");
                } else {
                    //先让员工确认是否是本人
                    showSurePopView();
                }
            }
        });
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                ((RegWithAndroidCameraActivity) context).setSelectButton(2);
                dialog.dismiss();
                if (null != mOnSelectListener)
                    mOnSelectListener.onSelected(2);
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                ((RegWithAndroidCameraActivity) context).setSelectButton(0);
                dialog.dismiss();
                if (null != mOnSelectListener)
                    mOnSelectListener.onSelected(0);
            }
        });
    }

    /****显示确认弹框（三秒）*/
    private void showSurePopView() {
        PopupOpenHelper popupOpenHelper = new PopupOpenHelper(context, view, R.layout.popup_sure_view);
        popupOpenHelper.openPopupWindow(true, Gravity.CENTER);
        popupOpenHelper.setOnPopupViewClick(new PopupOpenHelper.ViewClickListener() {
            @Override
            public void onViewListener(PopupWindow popupWindow, View inflateView) {
                ImageView img_face = inflateView.findViewById(R.id.img_face);
                TextView  tv_num   = inflateView.findViewById(R.id.tv_num);
                TextView  tv_name  = inflateView.findViewById(R.id.tv_name);
                mTv_sure = inflateView.findViewById(R.id.tv_sure);
                TextView tv_cancel = inflateView.findViewById(R.id.tv_cancel);
                img_face.setImageBitmap(mFace);
                tv_num.setText(et_no.getText());
                tv_name.setText(mTv_name.getText());
                //三秒倒计时
                time = 3;
                mTv_sure.setText("确认注册(3)");
                handler.sendEmptyMessageDelayed(0, 1000);
                mTv_sure.setOnClickListener(v -> {
                    if (time > 0) {
                        return;
                    }
                    popupOpenHelper.dismiss();
                    //确认注册
                    ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                    ((RegWithAndroidCameraActivity) context).setSelectButton(1);
                    dialog.dismiss();
                    if (null != mOnSelectListener)
                        mOnSelectListener.onSelected(1);
                });
                tv_cancel.setOnClickListener(v -> {
                    popupOpenHelper.dismiss();
                });
            }
        });
    }

    private void searchNameByNo(String etNo) {
        getPersonName(etNo);
    }

    /***获取人员姓名*/
    private void getPersonName(String etNo) {
        RequestParamsFM params = new RequestParamsFM();
        params.put("danhao", etNo);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.USERSLIST, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络连接错误，人员姓名查询失败！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误，人员姓名查询失败！");
                    return;
                }
                Gson                 gson       = new Gson();
                PersonListResultBean resultBean = gson.fromJson(resbody, PersonListResultBean.class);
                ToastUtils.showToast(resultBean.getMessage());
                if (!"1".equals(resultBean.getCode())) {
                    return;
                }
                if (null != resultBean.getList() && resultBean.getList().size() > 0) {
                    selectName = resultBean.getList().get(0).getFname();
                    selectID   = resultBean.getList().get(0).getId();
                    mTv_name.setText(selectName);
                    ((RegWithAndroidCameraActivity) context).setSelectName(selectName, selectID);
                } else {
                    ToastUtils.showToast("网络请求错误，人员姓名查询失败！");
                }
            }
        });
    }

    public void setViewCont(Bitmap face) {
        mFace = face;
        ((ImageView) view.findViewById(R.id.img)).setImageBitmap(face);
        selectName = "";
    }

    public void showDialog() {
        if (null != dialog)
            dialog.show();
    }

    public String getSelectName() {
        return selectName;
    }

    private static class MyDialog extends Dialog {
        MyDialog(@NonNull Context context) {
            super(context);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        public void show() {
            // 设置背景变暗程度, 构造方法时候设置无效
            getWindow().getAttributes().dimAmount = 0.8f;
            super.show();
        }
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }

    public interface OnSelectListener {
        void onSelected(int selectType);
    }
}
