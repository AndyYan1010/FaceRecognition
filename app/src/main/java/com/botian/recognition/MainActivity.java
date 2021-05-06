package com.botian.recognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.botian.recognition.activity.CameraPhotoActivity;
import com.botian.recognition.activity.DeviceActivity;
import com.botian.recognition.activity.OpenDevManListActivity;
import com.botian.recognition.activity.ReportListActivity;
import com.botian.recognition.activity.WebUrlActivity;
import com.botian.recognition.adapter.LiensAdapter;
import com.botian.recognition.bean.ClassLinesBean;
import com.botian.recognition.bean.CommonBean;
import com.botian.recognition.utils.PhoneInfoUtil;
import com.botian.recognition.utils.PopupOpenHelper;
import com.botian.recognition.utils.ProgressDialogUtil;
import com.botian.recognition.utils.ToastDialogUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.imageUtils.ShapeUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Request;

import static com.botian.recognition.utils.ToastDialogUtil.NORMOL_STYLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.tv_toWork)
    TextView tv_toWork;
    @BindView(R.id.tv_DevID)
    TextView tv_DevID;
    @BindView(R.id.tv_offWork)
    TextView tv_offWork;
    @BindView(R.id.tv_openMachine)
    TextView tv_openMachine;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.tv_activity)
    TextView tv_activity;
    @BindView(R.id.tv_cont)
    TextView tv_cont;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_title)
    TextView tv_title;
    //@BindView(R.id.sfview)
    SurfaceView sfview;
    @BindView(R.id.img_fontBorder)
    ImageView    img_fontBorder;
    @BindView(R.id.preview_parent)
    LinearLayout preview_parent;
    @BindView(R.id.recy_lines)
    RecyclerView recy_lines;

    private Unbinder                      unBinder;
    private int                           REQUEST_CODE_GET_FACE = 10001;
    private List<ClassLinesBean.ListBean> mLinesList;
    private LiensAdapter                  liensAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.flag = 0;
        MyApplication.listActivity.add(this);
        initView();
        initData();
        initListener();
        startActivity(new Intent(this, DeviceActivity.class));
    }

    private void initView() {
        unBinder = ButterKnife.bind(this);
        ShapeUtil.changeViewBackground(tv_toWork, "#145CB9");
        ShapeUtil.changeViewBackground(tv_offWork, "#660099");
        ShapeUtil.changeViewBackground(tv_close, "#7E0023");
        ShapeUtil.changeViewBackground(tv_activity, "#F6EFA6");
        tv_activity.setTextColor(getResources().getColor(R.color.black));
    }

    public void initData() {
        //获取硬件信息
        getDevInfo();
        //初始化班线列表
        initLinesRecy();
    }

    public void initListener() {
        tv_toWork.setOnClickListener(this);
        tv_offWork.setOnClickListener(this);
        tv_openMachine.setOnClickListener(this);
        tv_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toWork:
                //倒计时拍照
                //getFaceWaitTwoSecond("上班");
                //跳转拍照界面
                step2CameraPhotoActivity(0);
                break;
            case R.id.tv_offWork:
                step2CameraPhotoActivity(1);
                break;
            case R.id.tv_openMachine:
                //跳转开机界面
                step2OpenMachineActivity();
                //调用接口判断，是否开机或新增人员
                //openByService();
                break;
            case R.id.tv_close:
                //Intent intent = new Intent(MainActivity.this, CloseDevManListActivity.class);
                //startActivity(intent);
                //step2CameraPhotoActivity(3);
                Intent intent = new Intent(this, WebUrlActivity.class);
                intent.putExtra("urlParams", "/hybrid/html/scan.html");
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除注解
        if (unBinder != null) {
            unBinder.unbind();
        }
        MyApplication.listActivity.remove(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_GET_FACE == requestCode) {

        }
    }

    //跳转到开机界面
    private void step2OpenMachineActivity() {
        Intent intent = new Intent(MainActivity.this, ReportListActivity.class);
        startActivity(intent);
    }

    //是否开机
    private void openByService() {
        if ("".equals(MyApplication.devID)) {
            ToastUtils.showToast("未获取到设备id");
            return;
        }
        ProgressDialogUtil.startShow(this,"正在认证设备，请稍等...");
        RequestParamsFM parames = new RequestParamsFM();
        //parames.put("id", "869066035238777");
        parames.put("id", MyApplication.devID);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.SERKJTYPE, parames, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("网络错误！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    return;
                }
                Gson       gson       = new Gson();
                CommonBean commonBean = gson.fromJson(resbody, CommonBean.class);
                //点击开机后的判断
                if (!"1".equals(commonBean.getCode())) {
                    ToastUtils.showToast(commonBean.getMessage());
                    return;
                }
                //弹框提示，开机还是新增人员
                showPop2Chose(commonBean.getType());
            }
        });
    }

    private void showPop2Chose(String type) {
        PopupOpenHelper openHelper = new PopupOpenHelper(this, tv_openMachine, R.layout.popup_open_button);
        openHelper.openPopupWindow(true, Gravity.CENTER);
        openHelper.setOnPopupViewClick((popupWindow, inflateView) -> {
            TextView tv_newMachine = inflateView.findViewById(R.id.tv_newMachine);
            TextView tv_addPerson  = inflateView.findViewById(R.id.tv_addPerson);
            ShapeUtil.changeViewBackground(tv_newMachine, "#FECD52");
            ShapeUtil.changeViewBackground(tv_addPerson, "#145CB9");
            tv_newMachine.setOnClickListener(v -> {
                step2CameraPhotoActivity(2);
                openHelper.dismiss();
            });
            tv_addPerson.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, OpenDevManListActivity.class);
                startActivity(intent);
                openHelper.dismiss();
            });
            if ("1".equals(type)) {
                tv_addPerson.setVisibility(View.GONE);
            } else if ("2".equals(type)) {
                tv_newMachine.setVisibility(View.GONE);
            }
        });
    }

    /****获取班线*/
    private void getClassLines() {
        RequestParamsFM params = new RequestParamsFM();
        //params.put("id", "869066035238777");
        params.put("id", MyApplication.devID);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.BMLIST, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    return;
                }
                Gson           gson           = new Gson();
                ClassLinesBean classLinesBean = gson.fromJson(resbody, ClassLinesBean.class);
                if (!"1".equals(classLinesBean.getCode())) {
                    ToastUtils.showToast(classLinesBean.getMessage());
                    return;
                }
                MyApplication.workUserName = classLinesBean.getUsername();
                MyApplication.workGongXu   = classLinesBean.getGongxu();
                mLinesList.addAll(classLinesBean.getList());
                liensAdapter.notifyDataSetChanged();
                liensAdapter.setUrlParamsStr("?id=" + MyApplication.devID + "&userno=" + classLinesBean.getUserno()
                        + "&username=" + classLinesBean.getUsername() + "&kdepartment="
                        + classLinesBean.getKdepartment() + "&gongxu=" + classLinesBean.getGongxu());
            }
        });
    }

    //初始化班线列表
    private void initLinesRecy() {
        mLinesList = new ArrayList();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        recy_lines.setLayoutManager(gridLayoutManager);
        liensAdapter = new LiensAdapter(this, mLinesList);
        recy_lines.setAdapter(liensAdapter);
    }

    /***
     * 跳转拍照界面
     * */
    private void step2CameraPhotoActivity(int ftype) {
        Intent intent = new Intent(MainActivity.this, CameraPhotoActivity.class);
        intent.putExtra("ftype", ftype);
        startActivityForResult(intent, REQUEST_CODE_GET_FACE);
    }

    //获取硬件信息
    @SuppressLint({"CheckResult", "MissingPermission"})
    private void getDevInfo() {
        new RxPermissions(this)
                .request(Manifest.permission.ACCESS_NETWORK_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        //MyApplication.devID = PhoneInfoUtil.getTelephonyManager().getDeviceId();
                        MyApplication.devID = PhoneInfoUtil.getTelMacAddress().replace(":", "");
                        //MyApplication.devID = "869066035238777";
                        tv_DevID.setText("设备ID：" + MyApplication.devID);
                        //获取路线
                        getClassLines();
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