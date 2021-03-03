package com.botian.recognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.jws.JwsIntents;
import com.botian.recognition.activity.RetrieveWithAndroidCameraActivityOri;
import com.botian.recognition.activity.TXLiveFaceCheckActivity;
import com.botian.recognition.bean.FnoteListBean;
import com.botian.recognition.sdksupport.AIThreadPool;
import com.botian.recognition.utils.CommonUtil;
import com.botian.recognition.utils.PhoneInfoUtil;
import com.botian.recognition.utils.ProgressDialogUtil;
import com.botian.recognition.utils.ToastDialogUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.devUtils.MyInfraredReceiver;
import com.botian.recognition.utils.imageUtils.ShapeUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.ThreadUtils;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.cloud.ai.fr.sdksupport.Auth;
import com.tencent.cloud.ai.fr.sdksupport.FloatsFileHelper;
import com.tencent.cloud.ai.fr.utils.PermissionHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Request;

import static com.botian.recognition.sdksupport.SaveFeaturesToFileStep.FACE_LIB_PATH;
import static com.botian.recognition.utils.ToastDialogUtil.NORMOL_STYLE;
import static com.tencent.cloud.ai.fr.sdksupport.Auth.authWithDeviceSn;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.tv_title)
    TextView  tv_title;
    @BindView(R.id.tv_DevID)
    TextView  tv_DevID;
    @BindView(R.id.tv_toWork)
    TextView  tv_toWork;
    @BindView(R.id.tv_offWork)
    TextView  tv_offWork;
    @BindView(R.id.img_logo)
    ImageView img_logo;

    private Unbinder unBinder;
    private int      REQUEST_CODE_GET_FACE = 10001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        MyApplication.flag = 0;
        MyApplication.listActivity.add(this);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        unBinder = ButterKnife.bind(this);
        ShapeUtil.changeViewBackground(tv_toWork, "#145CB9");
        ShapeUtil.changeViewBackground(tv_offWork, "#660099");
    }

    public void initData() {
        //获取硬件信息
        getDevInfo();
        //设置红外感应，有人时打开led灯
        setInfra_redWithLED();
    }

    public void initListener() {
        tv_title.setOnClickListener(this);
        tv_toWork.setOnClickListener(this);
        tv_offWork.setOnClickListener(this);
        img_logo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_title:
                //同步特征值
                syncFaceValue();
                break;
            case R.id.img_logo:
                //跳转人脸特征值获取
                step2GetFaceValue();
                break;
            case R.id.tv_toWork:
                //上班
                //Intent intent = new Intent(MainActivity2.this, RetrieveWithAndroidCameraActivity.class);
                Intent intent = new Intent(MainActivity2.this, RetrieveWithAndroidCameraActivityOri.class);
                intent.putExtra("checkType", 1);
                startActivity(intent);
                break;
            case R.id.tv_offWork:
                //下班
                //Intent intent1 = new Intent(MainActivity2.this, RetrieveWithAndroidCameraActivity.class);
                Intent intent1 = new Intent(MainActivity2.this, RetrieveWithAndroidCameraActivityOri.class);
                intent1.putExtra("checkType", 2);
                startActivity(intent1);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MyApplication.getJwsManager().jwsCloseLED();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除注解
        if (unBinder != null) {
            unBinder.unbind();
        }
        closeInfraredAndLED();
        MyApplication.listActivity.remove(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_GET_FACE == requestCode) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);// 必须有这个调用, mPermissionHandler 才能正常工作
    }

    /***同步人脸特种值*/
    private void syncFaceValue() {
        ProgressDialogUtil.startShow(this, "正在同步人脸特征值信息...");
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
                Gson          gson       = new Gson();
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
    private void keepStoreFaceValue(List<FnoteListBean.ListBean> list) {
        if (null == list || list.size() == 0) {
            ProgressDialogUtil.hideDialog();
            return;
        }
        ProgressDialogUtil.startShow(this, "正在存储特征值");
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                for (FnoteListBean.ListBean bean : list) {
                    String  filePath    = new File(FACE_LIB_PATH + bean.getId() + ".feature").getAbsolutePath();
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

    private MyInfraredReceiver mMyInfraredReceiver;

    /***设置红外感应*/
    private void setInfra_redWithLED() {
        mMyInfraredReceiver = new MyInfraredReceiver();
        IntentFilter intentFilter = new IntentFilter(JwsIntents.REQUEST_RESPONSE_IR_STATE_ACTION);
        //MyApplication.getJwsManager().jwsRegisterIRListener();
        registerReceiver(mMyInfraredReceiver, intentFilter);
    }

    /***关闭红外*/
    public void closeInfraredAndLED() {
        if (mMyInfraredReceiver != null) {
            //MyApplication.getJwsManager().jwsUnregisterIRListener();
            unregisterReceiver(mMyInfraredReceiver);
            mMyInfraredReceiver = null;
        }
        MyApplication.getJwsManager().jwsCloseLED();
    }

    private void askForRight() {
        try {
            mPermissionHandler.start();// 先申请系统权限
        } catch (PermissionHandler.GetPermissionsException e) {
            e.printStackTrace();
            Toast.makeText(this, "GetPermissionsException: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void step2GetFaceValue() {
        Intent intent = new Intent(MainActivity2.this, TXLiveFaceCheckActivity.class);
        startActivity(intent);
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
            ToastUtils.showToast(msg);
        }

        @Override
        protected void onAllPermissionGranted() {
            //// 请修改人脸识别 SDK 授权信息
            Auth.AuthResult authResult = auth(MainActivity2.this,
                    "whazsge55"/*修改APPID为实际的值*/,
                    "a2c0863f3ae943f3ac0603c7d4f2c28d"/*修改SECRET_KEY为实际的值*/);
            if (authResult.isSucceeded()) {//授权成功
                AIThreadPool.instance().init(MainActivity2.this);//提前全局初始化, 后续的 Activity 就不必再执行初始化了
            }
        }
    };

    private Auth.AuthResult auth(Context context, String appId, String secretKey) {
        Auth.AuthResult authResult = authWithDeviceSn(context, appId, secretKey);
        String          msg        = String.format("授权%s, appId=%s, %s", authResult.isSucceeded() ? "成功" : "失败", appId, authResult.toString());
        ToastUtils.showToast(msg);
        return authResult;
    }

    //获取硬件信息
    @SuppressLint({"CheckResult", "MissingPermission"})
    private void getDevInfo() {
        new RxPermissions(this)
                .request(Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        MyApplication.devID = PhoneInfoUtil.getTelephonyManager().getDeviceId();
                        tv_DevID.setText("设备ID：" + MyApplication.devID);
                        //申请权限
                        askForRight();
                    } else {
                        //未开启定位权限或者被拒绝的操作
                        ToastDialogUtil.getInstance()
                                .setContext(this)
                                .useStyleType(NORMOL_STYLE)
                                .setTitle("无法获取设备读取权限")
                                .setCont("您好，设备需使用相关权限，才能保证软件的正常运行。")
                                .showCancelView(true, "取消", (dialogUtil, view) -> dialogUtil.dismiss())
                                .showSureView(true, "去设置", (dialogUtil, view) -> {
                                    //跳转设置界面
                                    Intent intent = new Intent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    }
                });
    }
}