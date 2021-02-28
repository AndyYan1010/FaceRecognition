package com.botian.recognition.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.botian.recognition.BaseActivity;
import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.bean.CheckFaceHistory;
import com.botian.recognition.bean.PersonListResultBean;
import com.botian.recognition.bean.UpCheckResultBean;
import com.botian.recognition.sdksupport.AIThreadPool;
import com.botian.recognition.utils.NetUtil;
import com.botian.recognition.utils.ProgressDialogUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;
import com.tencent.cloud.ai.fr.sdksupport.Auth;
import com.tencent.cloud.ai.fr.utils.PermissionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Request;

import static com.tencent.cloud.ai.fr.sdksupport.Auth.authWithDeviceSn;

public class TXLiveFaceCheckActivity extends BaseActivity implements View.OnClickListener {
    private TextView                            tv_regist;
    private TextView                            tv_upload;
    //private Spinner                             mPersonSpinner;
    //private SpPersonNameAdapter                 mSpAdapter;
    private List<PersonListResultBean.ListBean> mPersonList;

    @Override
    protected int setLayout() {
        return R.layout.act_tx_face_check;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        //mPersonSpinner = findViewById(R.id.spinner_list);
        tv_regist = findViewById(R.id.tv_regist);
        tv_upload = findViewById(R.id.tv_upload);
    }

    @Override
    protected void initData() {
        try {
            mPermissionHandler.start();// 先申请系统权限
        } catch (PermissionHandler.GetPermissionsException e) {
            e.printStackTrace();
            Toast.makeText(this, "GetPermissionsException: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        mPersonList = new ArrayList<>();
        //初始化人员列表spinner
        //initSpinnerView();
        //获取人员列表
        //getPersonList();
    }

    @Override
    protected void initListener() {
        tv_regist.setOnClickListener(this);
        tv_upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_regist:
                //if (mPersonList.size() == 0) {
                //    ToastUtils.showToast("未获取到人员信息，请重新进入页面！");
                //    return;
                //}
                Intent intent = new Intent(TXLiveFaceCheckActivity.this, RegWithAndroidCameraActivity.class);
                intent.putExtra("personList", (Serializable) mPersonList);
                startActivity(intent);
                break;
            case R.id.tv_upload:
                //提交本地打卡信息
                if (!NetUtil.checkInternetStatus()) {
                    ToastUtils.showToast("请检查网络状态！");
                    return;
                }
                upLoadLocalCheckInfo();
                break;
        }
    }

    /***提交本地打卡记录到服务器*/
    private void upLoadLocalCheckInfo() {
        //读取本地数据库的打卡信息
        ProgressDialogUtil.startShow(this,"正在提交数据...");
        List<CheckFaceHistory> checkFaceHistories = readLocalCheckHistory();
        if (checkFaceHistories.size() == 0) {
            ProgressDialogUtil.hideDialog();
            ToastUtils.showToast("未找到本地打卡记录！");
            return;
        }
        JSONArray peoplelist = new JSONArray();
        for (CheckFaceHistory checkFaceHistory : checkFaceHistories) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userid", checkFaceHistory.getUserID());
                jsonObject.put("username", checkFaceHistory.getUserName());
                jsonObject.put("type", checkFaceHistory.getCheckType());
                jsonObject.put("ftime", checkFaceHistory.getFtime());
                peoplelist.put(jsonObject);
            } catch (Exception e) {
            }
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", peoplelist);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.UPDATEWORK, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("网络连接错误，打卡记录提交失败！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误，打卡记录提交失败！");
                    return;
                }
                Gson              gson       = new Gson();
                UpCheckResultBean resultBean = gson.fromJson(resbody, UpCheckResultBean.class);
                ToastUtils.showToast(resultBean.getMessage());
                if ("1".equals(resultBean.getCode())) {
                    //清除本地记录
                    clearLocalCheckHistory();
                }
            }
        });
    }

    /****清除本地打卡记录*/
    private void clearLocalCheckHistory() {
        MyApplication.getDaoSession().deleteAll(CheckFaceHistory.class);
    }

    /****读取本地数据库打卡信息*/
    private List<CheckFaceHistory> readLocalCheckHistory() {
        //查询全部
        return MyApplication.getDaoSession().loadAll(CheckFaceHistory.class);
    }

    /***获取人员列表*/
    private void getPersonList() {
        OkHttpUtils.getInstance().doGet(NetConfig.USERSLIST, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络连接错误，人员列表获取失败！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误，人员列表获取失败！");
                    return;
                }
                Gson                 gson       = new Gson();
                PersonListResultBean resultBean = gson.fromJson(resbody, PersonListResultBean.class);
                ToastUtils.showToast(resultBean.getMessage());
                if (!"1".equals(resultBean.getCode())) {
                    return;
                }
                mPersonList.clear();
                mPersonList.addAll(resultBean.getList());
                //mSpAdapter.notifyDataSetChanged();
                //TODO 将人员信息写入本地数据库

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);// 必须有这个调用, mPermissionHandler 才能正常工作
    }

    private final PermissionHandler mPermissionHandler = new PermissionHandler(this) {
        @Override
        protected boolean shouldIgnore(String permission) {
            return false;
            // return permission.equals(Manifest.permission.WRITE_SETTINGS) //API 23 或以上, 无法通过授权对话框获得授权, 忽略之
        }

        @Override
        protected void onPermissionsDecline(String[] permissions) {
            String msg = "没有获得系统权限: " + Arrays.toString(permissions);
            showMessage(msg);
        }

        @Override
        protected void onAllPermissionGranted() {
            // 请修改人脸识别 SDK 授权信息
            Auth.AuthResult authResult = auth(TXLiveFaceCheckActivity.this,
                    "whazsge55"/*修改APPID为实际的值*/,
                    "a2c0863f3ae943f3ac0603c7d4f2c28d"/*修改SECRET_KEY为实际的值*/);
            if (authResult.isSucceeded()) {//授权成功
                AIThreadPool.instance().init(TXLiveFaceCheckActivity.this);//提前全局初始化, 后续的 Activity 就不必再执行初始化了
                //addButton("注册人脸信息", RegWithAndroidCameraActivity.class);
                //addButton("搜索人脸信息", RetrieveWithAndroidCameraActivity.class);

                //addButton("1:1 比对(图片 vs Android相机)", CompareFaceActivity.class);
                //addButton("1:N 注册(Android相机)", RegWithAndroidCameraActivity.class);
                //addButton("1:N 注册(图片文件)", RegWithFileActivity.class);
                //addButton("1:N 搜索(Android相机)", RetrieveWithAndroidCameraActivity.class);
                //addButton("1:N 搜索(华捷相机彩色+红外+深度)", RetrieveWithImiCameraActivity.class);
                //addButton("1:N 搜索(图片文件)", null);
                //addButton("1:N 搜索(案例重放)", ReplayActivity.class);
            }
        }
    };

    private void addButton(String buttonText, final Class targetActivity) {
        Button button = new Button(this);
        button.setText(buttonText);
        button.setAllCaps(false);
        button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TXLiveFaceCheckActivity.this, targetActivity));
            }
        });
        ((ViewGroup) findViewById(R.id.button_container)).addView(button);
    }

    private Auth.AuthResult auth(Context context, String appId, String secretKey) {
        Auth.AuthResult authResult = authWithDeviceSn(context, appId, secretKey);
        String          msg        = String.format("授权%s, appId=%s, %s", authResult.isSucceeded() ? "成功" : "失败", appId, authResult.toString());
        showMessage(msg);
        return authResult;
    }

    private void showMessage(final String msg) {
        ((TextView) findViewById(R.id.tips)).setText(msg);
    }

    private void initSpinnerView() {
        //mPersonList = new ArrayList<>();
        //mSpAdapter  = new SpPersonNameAdapter(this, mPersonList);
        //mPersonSpinner.setAdapter(mSpAdapter);
        //mPersonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //    @Override
        //    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //        MyApplication.tempUserID   = mPersonList.get(position).getId();
        //        MyApplication.tempUserName = mPersonList.get(position).getFname();
        //        System.out.println(MyApplication.tempUserName);
        //    }
        //
        //    @Override
        //    public void onNothingSelected(AdapterView<?> parent) {
        //
        //    }
        //});
    }
}
