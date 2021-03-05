package com.botian.recognition.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.bean.CommonBean;
import com.botian.recognition.bean.PersonListResultBean;
import com.botian.recognition.sdksupport.AlignmentStep;
import com.botian.recognition.sdksupport.ExtractFeatureStep;
import com.botian.recognition.sdksupport.FaceForReg;
import com.botian.recognition.sdksupport.FileToFrameStep;
import com.botian.recognition.sdksupport.PreprocessStep;
import com.botian.recognition.sdksupport.QualityProStep;
import com.botian.recognition.sdksupport.RegStep;
import com.botian.recognition.sdksupport.SaveFeaturesToFileStep;
import com.botian.recognition.sdksupport.StuffBox;
import com.botian.recognition.sdksupport.SyncJobBuilder;
import com.botian.recognition.sdksupport.TrackStep;
import com.botian.recognition.utils.CommonUtil;
import com.botian.recognition.utils.ProgressDialogUtil;
import com.botian.recognition.utils.ToastDialogUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.fileUtils.FileUtil;
import com.botian.recognition.utils.imageUtils.GlideLoaderUtil;
import com.botian.recognition.utils.imageUtils.SelectImageEngine;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.cloud.ai.fr.pipeline.AbsJob;
import com.tencent.cloud.ai.fr.pipeline.AbsStep;
import com.tencent.cloud.ai.fr.sdksupport.YTSDKManager;
import com.tencent.youtu.YTFaceTracker.TrackedFace;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.Request;

import static com.botian.recognition.utils.ToastDialogUtil.NORMOL_STYLE;

public class RegWithFileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RegWithFileActivity.class.getSimpleName();

    //private FakeCameraActivityViewController mViewController;

    /**
     * 需要注册的人脸图片文件夹
     */
    private static final String FACE_IMG_DIR = "/sdcard/face_for_reg";

    private ImageView img_back;
    private ImageView img_face;
    private EditText  et_no;
    private TextView  tv_search;
    private TextView  tv_name;
    private TextView  tv_chose_face;
    private TextView  tv_regist;
    public  int       VIDEOSHOOT_REQUEST_CHOOSE = 1006;//第三方图片视频选择
    private int       maxPicSize                = 10;//单张图片大小
    private String[]  mListPermission           = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private String    personName, personID;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_reg_with_file);
//        AIThreadPool.instance().init(this);//重要!!

        // 初始化相机
        // 初始化UI
        //mViewController = new FakeCameraActivityViewController(this);
        // 显示UI
        //setContentView(mViewController.getRootView());

        initView();
    }

    private void initView() {
        img_back      = findViewById(R.id.img_back);
        img_face      = findViewById(R.id.img_face);
        et_no         = findViewById(R.id.et_no);
        tv_search     = findViewById(R.id.tv_search);
        tv_name       = findViewById(R.id.tv_name);
        tv_chose_face = findViewById(R.id.tv_chose_face);
        tv_regist     = findViewById(R.id.tv_regist);

        img_back.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        tv_chose_face.setOnClickListener(this);
        tv_regist.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_search:
                String etNo = String.valueOf(et_no.getText());
                if (TextUtils.isEmpty(etNo.trim())) {
                    ToastUtils.showToast("工号不能为空！");
                    return;
                }
                //根据工号搜索姓名
                getPersonName(etNo);
                break;
            case R.id.tv_chose_face:
                //获取手机读写和拍照权限
                getPhoneRight();
                break;
            case R.id.tv_regist:
                if (null == personName || "".equals(personName)) {
                    ToastUtils.showToast("请输入工号，然后搜索姓名。");
                    return;
                }
                if (null == filePath || "".equals(filePath)) {
                    ToastUtils.showToast("请选择正确的照片");
                    return;
                }
                //提取人脸特征值
                getFaceValue(filePath);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEOSHOOT_REQUEST_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> uris = Matisse.obtainResult(data);
            if (uris.size() == 0) {
                ToastUtils.showToast("未获取到图片");
                return;
            }
            GlideLoaderUtil.showImageView(this, uris.get(0), img_face);
            filePath = FileUtil.getFilePathFromUri(RegWithFileActivity.this, uris.get(0));
        }
    }

    /***获取人员姓名*/
    private void getPersonName(String etNo) {
        ProgressDialogUtil.startShow(this, "正在搜索人员姓名");
        RequestParamsFM params = new RequestParamsFM();
        params.put("danhao", etNo);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.USERSLIST, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("网络连接错误，人员姓名查询失败！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误，人员姓名查询失败！");
                    return;
                }
                Gson                 gson       = new Gson();
                PersonListResultBean resultBean = gson.fromJson(resbody, PersonListResultBean.class);
                if (!"1".equals(resultBean.getCode())) {
                    ToastUtils.showToast("姓名查询失败！");
                    return;
                }
                if (null != resultBean.getList() && resultBean.getList().size() > 0) {
                    ToastUtils.showToast(resultBean.getMessage());
                    personName = resultBean.getList().get(0).getFname();
                    personID   = resultBean.getList().get(0).getId();
                    tv_name.setText(personName);
                } else {
                    ToastUtils.showToast("网络请求错误，人员姓名查询失败！");
                }
            }
        });
    }

    /***提取人脸特征值*/
    private void getFaceValue(String filePath) {
//        ProgressDialogUtil.startShow(this,"正在提取人脸特征值");
        new Thread() {
            @Override
            public void run() {
                YTSDKManager sdkMgr = new YTSDKManager(getAssets());//每个线程都必须使用单独的算法SDK实例, 这里为当前线程生成一份实例
//                for (File file : getFaceImageFiles()) {
//                    //创建流水线运行过程所需的物料箱
//                    StuffBox stuffBox = new StuffBox()
//                            .store(FileToFrameStep.IN_FILE, file)
//                            .setSdkManagerForThread(sdkMgr, Thread.currentThread().getId());
//                    //创建任务
//                    new AbsJob<>(stuffBox, regPipeline).run(/*直接在当前线程执行任务*/);
//                }
                File file = new File(filePath);
                //创建流水线运行过程所需的物料箱
                StuffBox stuffBox = new StuffBox()
                        .store(FileToFrameStep.IN_FILE, file)
                        .setSdkManagerForThread(sdkMgr, Thread.currentThread().getId());
                //创建任务
                new AbsJob<>(stuffBox, regPipeline).run(/*直接在当前线程执行任务*/);
                sdkMgr.destroy();//销毁算法SDK实例
            }
        }.start();
    }

    /***打开图片选择器*/
    private void openPhonePics() {
        Matisse.from(this)
                .choose(MimeType.ofAll())
                //有序选择图片 123456...
                .countable(true)
                //最大选择数量为9
                .maxSelectable(1)
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                //选择方向
                //.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                //界面中缩略图的质量
                //.thumbnailScale(0.85f)
                //Glide加载方式
                .imageEngine(new SelectImageEngine())
                //是否提供拍照功能
                .capture(true)
                //存储到哪里
                .captureStrategy(new CaptureStrategy(true, "com.botian.recognition.fileprovider"))
//              .setOnSelectedListener(new OnSelectedListener() {
//                    @Override
//                    public void onSelected(@NonNull List<Uri> uriList, @NonNull List<String> pathList) {
//                        if (null != uriList && uriList.size() > 0) {
//                            double fileSize = FilePathUtil.getFileSize(FilePathUtil.getRealFilePath(RegWithFileActivity.this, uriList.get(0)), FilePathUtil.RESULT_MB);
//                            if (fileSize >= maxPicSize) {
//                                ToastUtils.showToast("图片大小不能超过" + maxPicSize + "M");
//                            }
//                        } else {
//                            ToastUtils.showToast("未获取到图片！");
//                        }
//                    }
//                })
                .theme(R.style.Matisse_Dracula)
                .forResult(VIDEOSHOOT_REQUEST_CHOOSE);
    }

    private final List<AbsStep<StuffBox>> regPipeline = new SyncJobBuilder()
            .addStep(new FileToFrameStep(FileToFrameStep.IN_FILE))
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    //mViewController.setPreviewImageColor(stuffBox.find(FileToFrameStep.OUT_BITMAP));//显示帧预览
                    return true;
                }
            })
            .addStep(new PreprocessStep(FileToFrameStep.OUT_FRAME_GROUP))//【必选】图像预处理
            .addStep(new TrackStep())//【必选】人脸检测跟踪, 是后续任何人脸算法的前提
            .addStep(new AbsStep<StuffBox>() {//检测上一个步骤的结果, 决定流水线是否能继续
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    //mViewController.drawLightThreadStuff(stuffBox);//显示人脸检测结果
                    Collection<TrackedFace> faces = stuffBox.find(TrackStep.OUT_COLOR_FACE);
                    int                     size  = faces.size();
                    if (size != 1) {
                        String msg = "图片中" + (size == 0 ? "检测不到" : "多于一张") + "人脸, 无法注册: " + stuffBox.find(FileToFrameStep.IN_FILE).getName();
                        Log.w(TAG, msg);
                        ToastUtils.showToast(msg);
                        //mViewController.appendLogText(msg);
                        return false;
                    }
                    return true;
                }
            })
            .addStep(new AlignmentStep(TrackStep.OUT_COLOR_FACE))//【必选】遮挡检查, 保证注册照质量, 才能保证以后人脸搜索的准确度
            .addStep(new AbsStep<StuffBox>() {//检测上一个步骤的结果, 决定流水线是否能继续
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    for (Entry<TrackedFace, String> entry : stuffBox.find(AlignmentStep.OUT_ALIGNMENT_FAILED_FACES).entrySet()) {
                        String frameName = stuffBox.find(PreprocessStep.IN_RAW_FRAME_GROUP).name;
                        String msg       = String.format("%s: Alignment 失败, msg:%s", frameName, entry.getValue());
                        Log.i(TAG, msg);
                        ToastUtils.showToast(msg);
                        //mViewController.appendLogText(msg);
                    }

                    return stuffBox.find(AlignmentStep.OUT_ALIGNMENT_OK_FACES).size() > 0;//符合条件的人脸大于0才继续
                }
            })
            .addStep(new QualityProStep(AlignmentStep.OUT_ALIGNMENT_OK_FACES))//【必选】质量检查, 保证注册照质量, 才能保证以后人脸搜索的准确度
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    for (Entry<TrackedFace, String> entry : stuffBox.find(QualityProStep.OUT_QUALITY_PRO_FAILED).entrySet()) {
                        String frameName = stuffBox.find(PreprocessStep.IN_RAW_FRAME_GROUP).name;
                        String msg       = String.format("%s: QualityPro 失败, msg:%s", frameName, entry.getValue());
                        Log.i(TAG, msg);
                        ToastUtils.showToast(msg);
                        //mViewController.appendLogText(msg);
                    }

                    return stuffBox.find(QualityProStep.OUT_QUALITY_PRO_OK).size() > 0;//符合条件的人脸大于0才继续
                }
            })
            .addStep(new ExtractFeatureStep(QualityProStep.OUT_QUALITY_PRO_OK))//【必选】提取人脸特征
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    Map<TrackedFace, float[]> features = stuffBox.find(ExtractFeatureStep.OUT_FACE_FEATURES);
                    if (features.size() > 1) {
                        //如果这里触发了, 说明前面的步骤检查失效, 请检查流水线改动
                        throw new IllegalArgumentException("图中多于一张人脸, 无法注册: " + stuffBox.find(FileToFrameStep.IN_FILE).getName());
                    } else if (features.size() == 0) {
                        String msg = "图片提取人脸特征失败, 无法注册: " + stuffBox.find(FileToFrameStep.IN_FILE).getName();
                        Log.w(TAG, msg);
                        ToastUtils.showToast(msg);
                        //mViewController.appendLogText(msg);
                        return false;
                    }
                    return true;// 此时 size == 1
                }
            })
            .addStep(new RegStep(new RegStep.InputProvider() {//【必选】人脸特征入库(RAM)
                @Override
                public Collection<FaceForReg> onGetInput(StuffBox stuffBox) {
                    Map<TrackedFace, float[]> features = stuffBox.find(ExtractFeatureStep.OUT_FACE_FEATURES);

                    Collection<FaceForReg> faces = new ArrayList<>(1);
                    for (Entry<TrackedFace, float[]> entry : features.entrySet()) {
                        TrackedFace face    = entry.getKey();
                        float[]     feature = entry.getValue();
//                        String name = stuffBox.find(PreprocessStep.IN_RAW_FRAME_GROUP).name;//输入帧的名字(文件名)作为人名
                        String name = personName;
                        faces.add(new FaceForReg(face, name, feature));
                        //mViewController.appendLogText("注册成功: " + name);
                        //将人脸特征值，提交给服务器
                        upLoadFaceFature(feature);
                    }
                    return faces;
                }
            }))
            .addStep(new SaveFeaturesToFileStep(RegStep.IN_FACES))//【必选】把人脸特征保存到磁盘, 用于下次程序启动时恢复人脸库
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    //mViewController.drawHeavyThreadStuff(stuffBox);//绘制人脸详细信息
                    return true;
                }
            })
            .build();

    /***提交人脸特征值到服务器
     * @param feature*/
    private void upLoadFaceFature(float[] feature) {
        ProgressDialogUtil.startShow(this, "正在提交人脸特征值");
        JSONArray peoplelist = new JSONArray();
        try {
            JSONObject object = new JSONObject();
            object.put("userid", personID);
            object.put("fnote", CommonUtil.getFloatStr(feature));
            peoplelist.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", peoplelist);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.UPDATEUSERNOTE, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("人脸特征值提交失败！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                System.out.println("提交返回信息：" + resbody);
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误，人脸特征值提交失败！");
                    return;
                }
                Gson       gson       = new Gson();
                CommonBean commonBean = gson.fromJson(resbody, CommonBean.class);
                ToastUtils.showToast(commonBean.getMessage());
                if (!"1".equals(commonBean.getCode())) {
                    ToastUtils.showToast("人脸特征值备份失败！");
                    return;
                }
                personName = "";
                personID   = "";
                tv_name.setText(personName);
            }
        });
    }

    /**
     * 获取手机读写权限
     */
    @SuppressLint("CheckResult")
    private void getPhoneRight() {
        new RxPermissions(this)
                .request(mListPermission)
                .subscribe(granted -> {
                    if (granted) {
                        filePath = "";
                        openPhonePics();
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


    private List<File> getFaceImageFiles() {
        List<File> outFiles = Collections.emptyList();
        File       dir      = new File(FACE_IMG_DIR);
        File[]     files    = dir.listFiles();
        if (files == null) {
            String msg = "目录里面没找到图片文件: " + dir.getAbsolutePath();
            Log.w(TAG, msg);
            //mViewController.appendLogText(msg);
        } else {
            outFiles = new ArrayList<>(Arrays.asList(files));
            Collections.sort(outFiles);
        }
        return outFiles;
    }

}
