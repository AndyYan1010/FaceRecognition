package com.botian.recognition.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.botian.recognition.NetConfig;
import com.botian.recognition.bean.CommonBean;
import com.botian.recognition.bean.PersonListResultBean;
import com.botian.recognition.sdksupport.AIThreadPool;
import com.botian.recognition.sdksupport.AbsActivityViewController;
import com.botian.recognition.sdksupport.AlignmentStep;
import com.botian.recognition.sdksupport.AndroidCameraManager;
import com.botian.recognition.sdksupport.AsyncJobBuilder;
import com.botian.recognition.sdksupport.ConfirmRegFaceStep;
import com.botian.recognition.sdksupport.ExtractFeatureStep;
import com.botian.recognition.sdksupport.FaceDrawView;
import com.botian.recognition.sdksupport.FaceForConfirm;
import com.botian.recognition.sdksupport.FaceForReg;
import com.botian.recognition.sdksupport.FilterStep;
import com.botian.recognition.sdksupport.PickBestStep;
import com.botian.recognition.sdksupport.PreprocessStep;
import com.botian.recognition.sdksupport.QualityProStep;
import com.botian.recognition.sdksupport.SaveFeaturesToFileStep;
import com.botian.recognition.sdksupport.ShowCheckFaceDialogView;
import com.botian.recognition.sdksupport.StuffBox;
import com.botian.recognition.sdksupport.TrackStep;
import com.botian.recognition.utils.CommonUtil;
import com.botian.recognition.utils.ProgressDialogUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.botian.recognition.utils.netUtils.ThreadUtils;
import com.google.android.cameraview.CameraView;
import com.google.gson.Gson;
import com.tencent.cloud.ai.fr.camera.Frame;
import com.tencent.cloud.ai.fr.camera.FrameGroup;
import com.tencent.cloud.ai.fr.camera.ICameraManager;
import com.tencent.cloud.ai.fr.pipeline.AbsStep;
import com.tencent.youtu.YTFaceTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.Request;

public class RegWithAndroidCameraActivity extends AppCompatActivity {
    private ICameraManager                      mCameraManager;
    private AbsActivityViewController           mViewController;
    private List<PersonListResultBean.ListBean> mPersonList;
    public  boolean                             isShowDialog = false;//
    public  int                                 selectButton = -1;//
    public  String                              selectName   = "";//
    public  String                              selectID     = "";//
    private boolean                             isUpDateFace = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AIThreadPool.instance().init(this);//重要!!
        // 初始化UI
        mViewController = new ViewController(this);
        // 设置UI按钮
        //mViewController.addButton("切换相机", new OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        ((AndroidCameraManager) mCameraManager).switchCamera();//切换相机
        //    }
        //});
        // 显示UI
        setContentView(mViewController.getRootView());

        //获取人员列表 personList
        //mPersonList = (List<PersonListResultBean.ListBean>) getIntent().getSerializableExtra("personList");

        // 初始化相机
        mCameraManager = new AndroidCameraManager(this);
        // 监听相机帧回调
        mCameraManager.setOnFrameArriveListener(new ICameraManager.OnFrameArriveListener() {//从相机获取帧
            @Override
            public void onFrameArrive(byte[] data, int width, int height, int exifOrientation) {
                //彩图
                Frame colorFrame = new Frame(Frame.Format.YUV_NV21, data, width, height, exifOrientation);
                //红外图
                Frame irFrame = null;
                //深度图
                Frame depthFrame = null;
                StuffBox stuffBox = new StuffBox()//创建流水线运行过程所需的物料箱
                        .store(PreprocessStep.IN_RAW_FRAME_GROUP, new FrameGroup(colorFrame, irFrame, depthFrame, true))
                        .setOnRecycleListener(new StuffBox.OnRecycleListener() {
                            @Override
                            public void onRecycle(StuffBox stuffBox) {
                                FrameGroup rawFrameGroup = stuffBox.find(PreprocessStep.IN_RAW_FRAME_GROUP);
                                mCameraManager.onRecycleFrame(rawFrameGroup.colorFrame.data);
                                if (rawFrameGroup.irFrame != null) {/*TODO 回收 stuff.irFrame.data*/}
                                if (rawFrameGroup.depthFrame != null) {/*TODO 回收 stuff.depthFrame.data*/}
                            }
                        });//添加回收监听器
                new AsyncJobBuilder(stuffBox, mRegFromCameraPipelineBuilder).synthesize(/*合成流水线任务*/).launch(/*执行任务*/);
            }
        });
    }

    public void setDialogStatue(boolean dialogStatue) {
        isShowDialog = dialogStatue;
    }

    public void setSelectButton(int type) {
        selectButton = type;
    }

    public void setSelectName(String name, String id) {
        selectName = name;
        selectID   = id;
    }

    /**
     * 相机注册人脸流水线
     */
    private AsyncJobBuilder.PipelineBuilder mRegFromCameraPipelineBuilder = new AsyncJobBuilder.PipelineBuilder()
            .onThread(AIThreadPool.instance().getHeavyThread())
            .addStep(new PreprocessStep(PreprocessStep.IN_RAW_FRAME_GROUP))//【必选】图像预处理
            .addStep(new TrackStep())//【必选】人脸检测跟踪, 是后续任何人脸算法的前提
            .addStep(new FilterStep(TrackStep.OUT_COLOR_FACE))//【可选】过滤掉不合格的人脸, 推荐使用, 保证入库质量
            .addStep(new PickBestStep(FilterStep.OUT_FILTER_OK_FACES))//【必选】选出最佳的1个人脸, 因为后续步骤设计不支持一次注册多个人脸 
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    mViewController.drawLightThreadStuff(stuffBox);//UI绘制人脸追踪框
                    return true;
                }
            })
            .submit()
            .onThread(AIThreadPool.instance().getHeavyThread())
            .addStep(new AlignmentStep(PickBestStep.OUT_PICK_OK_FACES))//【可选】人脸遮挡, 表情, 瞳间距判断, 推荐使用, 保证入库质量
            .addStep(new QualityProStep(AlignmentStep.OUT_ALIGNMENT_OK_FACES))//【可选】人脸质量判断: 正面, 遮挡, 模糊, 光照, 推荐使用, 保证入库质量
            .addStep(new ExtractFeatureStep(QualityProStep.OUT_QUALITY_PRO_OK))//【必选】提取人脸特征
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    mViewController.drawHeavyThreadStuff(stuffBox);//UI绘制人脸详细信息
                    return true;
                }
            })
            .addStep(new ConfirmRegFaceStep(new ConfirmRegFaceStep.InputProvider() {//【可选】UI 询问用户是否确认注册当前人脸
                @Override
                public Collection<YTFaceTracker.TrackedFace> onGetInput(StuffBox stuffBox) {//筛选出已成功提取特征的人脸
                    Map<YTFaceTracker.TrackedFace, float[]> faceMap = stuffBox.find(ExtractFeatureStep.OUT_FACE_FEATURES);
                    return faceMap.keySet();
                }
            }) {
                @Override
                protected boolean onConfirmFace(FaceForConfirm faceForConfirm) {
                    //等待用户操作 UI 
                    //ConfirmRegFaceViewController.ConfirmResult result = new ConfirmRegFaceViewController(mPersonList)
                    //        .waitConfirm(RegWithAndroidCameraActivity.this, faceForConfirm.faceBmp, faceForConfirm.name);
                    //if (result.code == ConfirmRegFaceViewController.ConfirmResult.RESULT_OK) {
                    //    faceForConfirm.name = result.name;
                    //    return true;// true: 确认注册此人脸
                    //}
                    //if (result.code == ConfirmRegFaceViewController.ConfirmResult.RESULT_CANCEL) {
                    //    RegWithAndroidCameraActivity.this.finish();
                    //}
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                            ShowCheckFaceDialogView faceDialogView = null;
                            if (!isShowDialog && selectButton != 0 && !isUpDateFace) {
                                faceDialogView = new ShowCheckFaceDialogView();
                                faceDialogView.initView(RegWithAndroidCameraActivity.this);
                                faceDialogView.setViewCont(faceForConfirm.faceBmp, mPersonList);
                                faceDialogView.showDialog();
                            }
                        }
                    });
                    if (selectButton == 1) {
                        faceForConfirm.name = selectID;
                        isUpDateFace        = true;
                        return true;// true: 确认注册此人脸
                    }
                    if (selectButton == 0) {
                        finish();
                        //RegWithAndroidCameraActivity.this.finish();
                    }
                    return false;
                }
            })
            .addStep(new SaveFeaturesToFileStep(new SaveFeaturesToFileStep.InputProvider() {//【必选】把人脸特征保存到磁盘, 用于下次程序启动时恢复人脸库
                @Override
                public Collection<FaceForReg> onGetInput(StuffBox stuffBox) {
                    Map<YTFaceTracker.TrackedFace, float[]> faceAndFeatures = stuffBox.find(ExtractFeatureStep.OUT_FACE_FEATURES);
                    Map<YTFaceTracker.TrackedFace, String>  faceAndNames    = stuffBox.find(ConfirmRegFaceStep.OUT_CONFIRMED_FACES);
                    Collection<FaceForReg>                  out             = new ArrayList<>();
                    for (Entry<YTFaceTracker.TrackedFace, String> faceAndName : faceAndNames.entrySet()) {
                        YTFaceTracker.TrackedFace face    = faceAndName.getKey();//人脸信息
                        String                    name    = faceAndName.getValue();//姓名
                        float[]                   feature = faceAndFeatures.get(face);//人脸特征
                        out.add(new FaceForReg(face, name, feature));
                    }
                    return out;
                }

                @Override
                public void onFileSavedResult(Collection<FaceForReg> faceForRegs) {
                    if (!faceForRegs.isEmpty()) {
                        upLoadFaceFature(faceForRegs);
                    }
                }
            }))
            .submit();


    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraManager != null) {
            mCameraManager.resumeCamera();
        }
    }

    @Override
    protected void onPause() {
        if (mCameraManager != null) {
            mCameraManager.pauseCamera();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCameraManager != null) {
            mCameraManager.destroyCamera();
        }
        super.onDestroy();
    }

    private class ViewController extends AbsActivityViewController {
        private FaceDrawView mTracedFaceDrawView;

        ViewController(Context context) {
            super(context);
            mTracedFaceDrawView = new FaceDrawView(context, true);
        }

        @Override
        protected View onCreateCameraPreview() {
            CameraView preview = (CameraView) mCameraManager.getPreview();
            preview.addOverlay(mTracedFaceDrawView);
            return preview;
        }

        @Override
        protected void onDrawColorFaces(Collection<FaceDrawView.DrawableFace> drawableFaces, int frameWidth, int frameHeight) {
            mTracedFaceDrawView.onDrawTracedFaces(drawableFaces, frameWidth, frameHeight);
        }

        @Override
        protected void onDrawIrFaces(Collection<FaceDrawView.DrawableFace> drawableFaces, int frameWidth, int frameHeight) {
            /* Android has no IR Camera API */
        }

        @Override
        protected void onDrawDepthFaces(Collection<FaceDrawView.DrawableFace> drawableFaces, int frameWidth, int frameHeight) {
            /* Android has no Depth Camera API */
        }
    }

    /***提交人脸特征值
     * @param faceForRegs*/
    private void upLoadFaceFature(Collection<FaceForReg> faceForRegs) {
        ProgressDialogUtil.startShow(this, "正在提交人脸特征值");
        JSONArray peoplelist = new JSONArray();
        for (FaceForReg faceForReg : faceForRegs) {
            try {
                JSONObject object = new JSONObject();
                object.put("userid", selectID);
                object.put("fnote", CommonUtil.getFloatStr(faceForReg.feature));
                peoplelist.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", peoplelist);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.UPDATEUSERNOTE, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("人脸特征值提交失败！");
                isUpDateFace = false;
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误，人脸特征值提交失败！");
                    isUpDateFace = false;
                    return;
                }
                Gson       gson       = new Gson();
                CommonBean commonBean = gson.fromJson(resbody, CommonBean.class);
                ToastUtils.showToast(commonBean.getMessage());
                if (!"1".equals(commonBean.getCode())) {
                    return;
                }
                finish();
            }
        });
    }
}
