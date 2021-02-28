package com.botian.recognition.sdksupport;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.activity.RegWithAndroidCameraActivity;
import com.botian.recognition.adapter.SpPersonNameAdapter;
import com.botian.recognition.bean.PersonListResultBean;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class ShowCheckFaceDialogView {
    private MyDialog                            dialog;
    private View                                view;
    private Context                             context;
    private String                              selectName;
    private String                              selectID;
    private Spinner                             spinnerPerson;
    private EditText                            et_no;
    private TextView                            tv_search;
    private TextView                            tv_name;
    private List<PersonListResultBean.ListBean> mPersonList;
    private SpPersonNameAdapter                 mSpAdapter;
    private boolean                             showStatus;

    //public static ShowCheckFaceDialogView getInstance() {
    //    if (null == faceDialogView) {
    //        synchronized (ShowCheckFaceDialogView.class) {
    //            if (null == faceDialogView) {
    //                faceDialogView = new ShowCheckFaceDialogView();
    //            }
    //        }
    //    }
    //    return faceDialogView;
    //}

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
        initSpinner();
        dialog.setContentView(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initDialogView() {
        et_no     = view.findViewById(R.id.et_no);
        tv_search = view.findViewById(R.id.tv_search);
        tv_name   = view.findViewById(R.id.tv_name);
        et_no.setShowSoftInputOnFocus(false);
        setViewListener();
    }

    private void initSpinner() {
        spinnerPerson = view.findViewById(R.id.spinner_list);
        mPersonList   = new ArrayList<>();
        mSpAdapter    = new SpPersonNameAdapter(context, mPersonList);
        spinnerPerson.setAdapter(mSpAdapter);
        spinnerPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectID   = mPersonList.get(position).getId();
                selectName = mPersonList.get(position).getFname();
                ((RegWithAndroidCameraActivity) context).setSelectName(selectName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                    ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                    ((RegWithAndroidCameraActivity) context).setSelectButton(1);
                    dialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                ((RegWithAndroidCameraActivity) context).setSelectButton(2);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                ((RegWithAndroidCameraActivity) context).setSelectButton(0);
                dialog.dismiss();
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
                    tv_name.setText(selectName);
                } else {
                    ToastUtils.showToast("网络请求错误，人员姓名查询失败！");
                }
            }
        });
    }

    public void setViewCont(Bitmap face, List<PersonListResultBean.ListBean> personList) {
        ((ImageView) view.findViewById(R.id.img)).setImageBitmap(face);
        selectName = "";
        //if (null == mPersonList) {
        //    mPersonList = new ArrayList<>();
        //} else {
        //    mPersonList.clear();
        //}
        //mPersonList.addAll(personList);
        //mSpAdapter.notifyDataSetChanged();
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
}
