package com.botian.recognition.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.botian.recognition.BaseActivity;
import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.adapter.PersonListAdapter;
import com.botian.recognition.bean.FaceResultBean;
import com.botian.recognition.bean.UpResultBean;
import com.botian.recognition.configure.LocalSetting;
import com.botian.recognition.utils.ToastDialogUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.fileUtils.AudioUtil;
import com.botian.recognition.utils.fileUtils.FileUtil;
import com.botian.recognition.utils.mediaUtils.CameraAutoUtil;
import com.botian.recognition.utils.mediaUtils.SoundUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

import static com.botian.recognition.utils.ToastDialogUtil.NORMOL_STYLE;

public class CameraPhotoActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_title)
    TextView tv_title;
    //@BindView(R.id.sfview)
    SurfaceView sfview;
    @BindView(R.id.img_fontBorder)
    ImageView    img_fontBorder;
    @BindView(R.id.img_back)
    ImageView    img_back;
    @BindView(R.id.preview_parent)
    LinearLayout preview_parent;
    @BindView(R.id.rec_person)
    RecyclerView rec_person;
    @BindView(R.id.card_view)
    CardView     card_view;
    @BindView(R.id.tv_sure)
    TextView     tv_sure;

    private List<UpResultBean.ListBean> mRecPersonList;
    private PersonListAdapter           personListAdapter;
    private String[]                    mListPermission           = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int                         RESULT_CODE_OPEN_MACHINE  = 10012;
    private int                         RESULT_CODE_CLOSE_MACHINE = 10013;
    private int                         RESULT_CODE_FOR_WEB       = 10014;
    private String                      faceCheckResbody;

    @Override
    public int setLayout() {
        return R.layout.act_camera_photo;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        sfview = new SurfaceView(this);
        LinearLayout.LayoutParams lp = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        sfview.setLayoutParams(lp);
    }

    @Override
    public void initData() {
        //初始化人员列表
        initRecPerson();
    }

    @Override
    public void initListener() {
        img_back.setOnClickListener(this);
        tv_sure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_sure:
                //关闭弹框和act
                card_view.setVisibility(View.GONE);
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        preview_parent.addView(sfview);
        showCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preview_parent.removeView(sfview);
        CameraAutoUtil.getInstance().stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 显示人脸
     */
    @SuppressLint("CheckResult")
    private void showCamera() {
        new RxPermissions(this)
                .request(mListPermission)
                .subscribe(granted -> {
                    if (granted) {
                        //获取前置摄像头，显示在SurfaceView上
                        setSurFaceView();
                    } else {
                        //未开启定位权限或者被拒绝的操作
                        ToastDialogUtil.getInstance()
                                .setContext(this)
                                .useStyleType(NORMOL_STYLE)
                                .setTitle("无法获取拍照权限")
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

    /**
     * 显示人脸
     */
    private void setSurFaceView() {
        CameraAutoUtil.getInstance()
                .initView(this, sfview, img_fontBorder)
                .setFtype(getFtype())
                .setContChangeListener((titleCont, cont) -> {
                    setCont(tv_title, titleCont);
                    setCont(tv_time, cont);
                })
                .setFinishListener(new CameraAutoUtil.FinishListener() {
                    @Override
                    public void onWaiteStart() {
                        CameraAutoUtil.getInstance().countGetFace();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onResult(int ftype, String resbody, List<FaceResultBean.DataBean> personList) {
                        //提交人脸信息到对应接口
                        if (ftype == 0) {
                            //调用上班接口
                            go2Work(personList);
                        } else if (ftype == 1) {
                            //调用下班接口
                            offWork(personList);
                        } else if (ftype == 2) {
                            //开机、启动，添加人员
                            openMachineOrAddPerson(personList);
                        } else if (ftype == 3) {
                            //关机
                            closeMachine(personList);
                        } else if (ftype == 4) {
                            //汇报

                        } else if (ftype == 5) {
                            //返回web类型
                            faceCheckResbody = resbody;
                            Intent intent = new Intent();
                            intent.putExtra("ftype", getFtype());
                            intent.putExtra("resultResbody", faceCheckResbody);
                            setResult(RESULT_CODE_FOR_WEB, intent);
                            finish();
                        } else {
                            ToastUtils.showToast("未识别类型，请重新选择");
                        }
                    }

                    @Override
                    public void onFinish(String result) {
                        if (2 == getFtype()) {
                            Intent intent = new Intent();
                            intent.putExtra("ftype", getFtype());
                            intent.putExtra("addType", getIntent().getIntExtra("addType", -1));
                            intent.putExtra("itemIndex", getIntent().getIntExtra("itemIndex", -1));
                            setResult(RESULT_CODE_OPEN_MACHINE, intent);
                        }
                        if (3 == getFtype()) {
                            Intent intent = new Intent();
                            intent.putExtra("ftype", getFtype());
                            intent.putExtra("itemIndex", getIntent().getIntExtra("itemIndex", -1));
                            setResult(RESULT_CODE_CLOSE_MACHINE, intent);
                        }
                        finish();
                    }
                })
                .setSurFaceView();
    }

    public int getFtype() {
        return getIntent().getIntExtra("ftype", -1);
    }

    /****显示人员列表*/
    public void showPersonList(List<UpResultBean.ListBean> listBeans) {
        if (null == listBeans || listBeans.size() == 0) {
            ToastUtils.showToast("人脸认证信息为空！");
            return;
        }
        mRecPersonList.clear();
        mRecPersonList.addAll(listBeans);
        card_view.setVisibility(View.VISIBLE);
        personListAdapter.notifyDataSetChanged();
    }

    /***初始化人员认证列表*/
    private void initRecPerson() {
        mRecPersonList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rec_person.setLayoutManager(linearLayoutManager);
        personListAdapter = new PersonListAdapter(this, R.layout.recy_person_list, mRecPersonList);
        rec_person.setAdapter(personListAdapter);
    }

    private void setCont(TextView textView, String cont) {
        if (null != textView)
            textView.setText(cont);
    }

    /****关机*/
    private void closeMachine(List<FaceResultBean.DataBean> personList) {
        CameraAutoUtil.getInstance().callContChangeListener("", "正在提交关机信息...");
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < personList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", personList.get(i).getPersonName());
                jsonObject.put("userid", personList.get(i).getPersonId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("workid", MyApplication.workID);
        params.put("starttime", MyApplication.workStartTime);
        params.put("peoplelist", jsonArray);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.HBGUANJI, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误");
                SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                CameraAutoUtil.getInstance().callContChangeListener("", "网络错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    CameraAutoUtil.getInstance().callContChangeListener("", "网络请求错误！");
                    ToastUtils.showToast("网络请求错误！");
                    SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                    return;
                }
                CameraAutoUtil.getInstance().callContChangeListener("", "");
                Gson         gson         = new Gson();
                UpResultBean upResultBean = gson.fromJson(resbody, UpResultBean.class);
                ToastUtils.showToast(upResultBean.getMessage());
                //if (!"1".equals(upResultBean.getCode())) {
                //    SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                //    return;
                //}
                //显示人员列表
                showPersonList(upResultBean.getList());
                //播报语音
                playAudio(upResultBean.getAudio());
            }
        });
    }

    /****开机或添加人员*/
    private void openMachineOrAddPerson(List<FaceResultBean.DataBean> personList) {
        if (getIntent().getIntExtra("addType", -1) == -1) {
            ToastUtils.showToast("开机类别错误，请重新操作！");
            return;
        }
        CameraAutoUtil.getInstance().callContChangeListener("", "正在汇报开机人员...");
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < personList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", personList.get(i).getPersonName());
                jsonObject.put("userid", personList.get(i).getPersonId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("type", getIntent().getIntExtra("addType", -1));
        params.put("workid", MyApplication.workID);
        params.put("peoplelist", jsonArray);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.ADDRY2, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误");
                SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                CameraAutoUtil.getInstance().callContChangeListener("", "网络错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                    CameraAutoUtil.getInstance().callContChangeListener("", "网络请求错误！");
                    return;
                }
                CameraAutoUtil.getInstance().callContChangeListener("", "");
                Gson         gson         = new Gson();
                UpResultBean upResultBean = gson.fromJson(resbody, UpResultBean.class);
                ToastUtils.showToast(upResultBean.getMessage());
                //if (!"1".equals(upResultBean.getCode())) {
                //    SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                //    return;
                //}
                //显示人员列表
                showPersonList(upResultBean.getList());
                //播报语音
                playAudio(upResultBean.getAudio());
            }
        });
    }

    //下班
    private void offWork(List<FaceResultBean.DataBean> personList) {
        CameraAutoUtil.getInstance().callContChangeListener("", "正在提交下班信息...");
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < personList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", personList.get(i).getPersonName());
                jsonObject.put("userid", personList.get(i).getPersonId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", jsonArray);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.ENDWORK, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误");
                SoundUtil.getInstance().playSrcAudio(R.raw.nogowork);
                CameraAutoUtil.getInstance().callContChangeListener("", "网络错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    SoundUtil.getInstance().playSrcAudio(R.raw.nogowork);
                    CameraAutoUtil.getInstance().callContChangeListener("", "网络请求错误！");
                    return;
                }
                CameraAutoUtil.getInstance().callContChangeListener("", "");
                Gson         gson         = new Gson();
                UpResultBean upResultBean = gson.fromJson(resbody, UpResultBean.class);
                ToastUtils.showToast(upResultBean.getMessage());
                //if ("0".equals(upResultBean.getCode())) {
                //    SoundUtil.getInstance().playSrcAudio(R.raw.nogowork);
                //    return;
                //}
                //if ("1".equals(upResultBean.getCode())) {
                //    SoundUtil.getInstance().playSrcAudio(R.raw.noclose);
                //    return;
                //}
                //显示人员列表
                showPersonList(upResultBean.getList());
                //播报语音
                playAudio(upResultBean.getAudio());
            }
        });
    }

    //上班
    private void go2Work(List<FaceResultBean.DataBean> personList) {
        CameraAutoUtil.getInstance().callContChangeListener("", "正在提交上班信息...");
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < personList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", personList.get(i).getPersonName());
                jsonObject.put("userid", personList.get(i).getPersonId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", jsonArray);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.STARTWORK, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误");
                SoundUtil.getInstance().playSrcAudio(R.raw.noafterwork);
                CameraAutoUtil.getInstance().callContChangeListener("", "网络错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    SoundUtil.getInstance().playSrcAudio(R.raw.noafterwork);
                    CameraAutoUtil.getInstance().callContChangeListener("", "网络请求错误！");
                    return;
                }
                Gson         gson         = new Gson();
                UpResultBean upResultBean = gson.fromJson(resbody, UpResultBean.class);
                ToastUtils.showToast(upResultBean.getMessage());
                CameraAutoUtil.getInstance().callContChangeListener("", "");
                //if (!"1".equals(upResultBean.getCode())) {
                //    SoundUtil.getInstance().playSrcAudio(R.raw.noafterwork);
                //    //return;
                //}
                //显示人员列表
                showPersonList(upResultBean.getList());
                //播报语音
                playAudio(upResultBean.getAudio());
            }
        });
    }

    private String audioFilePath = "";

    /***播报语音
     * @param audio*/
    private void playAudio(String audio) {
        audioFilePath = LocalSetting.AUDIO_PATH + "bobao.wav";
        FileUtil.deleteFile(audioFilePath);
        AudioUtil.getInstance().setChangeListener(new AudioUtil.ChangeFileListener() {
            @Override
            public void onSuccess() {
                //播放音频
                SoundUtil.getInstance().playAudio(audioFilePath);
            }

            @Override
            public void onFailed() {
                ToastUtils.showToast("音频播放失败！");
            }
        }).decoderBase642File(audio, audioFilePath);
    }
}
