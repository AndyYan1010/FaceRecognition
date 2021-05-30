package com.botian.recognition.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.bean.UpCheckResultBean;
import com.botian.recognition.configure.LocalSetting;
import com.botian.recognition.sdksupport.AIThreadPool;
import com.botian.recognition.sdksupport.AbsActivityViewControllerOri;
import com.botian.recognition.sdksupport.AndroidCameraManager;
import com.botian.recognition.sdksupport.AsyncJobBuilder;
import com.botian.recognition.sdksupport.ExtractFeatureStep;
import com.botian.recognition.sdksupport.FaceDrawView;
import com.botian.recognition.sdksupport.PickBestStep;
import com.botian.recognition.sdksupport.PrepareFaceLibraryFromFileStep;
import com.botian.recognition.sdksupport.PreprocessStep;
import com.botian.recognition.sdksupport.RegStep;
import com.botian.recognition.sdksupport.RetrievalStep;
import com.botian.recognition.sdksupport.SaveFeaturesToFileStep;
import com.botian.recognition.sdksupport.StuffBox;
import com.botian.recognition.sdksupport.TrackStep;
import com.botian.recognition.utils.AudioTimeUtil;
import com.botian.recognition.utils.ProgressDialogUtil;
import com.botian.recognition.utils.TimeUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.UpdateWorkInfoUtil;
import com.botian.recognition.utils.fileUtils.AudioUtil;
import com.botian.recognition.utils.fileUtils.FileUtil;
import com.botian.recognition.utils.mediaUtils.SoundMediaPlayerUtil;
import com.botian.recognition.utils.mediaUtils.SoundUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.botian.recognition.utils.netUtils.ThreadUtils;
import com.google.android.cameraview.CameraView;
import com.google.gson.Gson;
import com.tencent.cloud.ai.fr.camera.Frame;
import com.tencent.cloud.ai.fr.camera.FrameGroup;
import com.tencent.cloud.ai.fr.camera.ICameraManager;
import com.tencent.cloud.ai.fr.pipeline.AbsStep;
import com.tencent.youtu.YTFaceRetrieval;
import com.tencent.youtu.YTFaceTracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.Request;

public class RetrieveWithAndroidCameraActivityOri extends AppCompatActivity {
    private ICameraManager               mCameraManager;
    private AbsActivityViewControllerOri mViewController;
    private List<String>                 mPersonDataList;
    private List<String>                 mLastPersonDataList;
    private boolean                      canGetFace   = true;
    private boolean                      isSubmitting = false;
    private boolean                      isAddPerson  = false;
    private Handler                      mHandler;
    private TextView                     tv_changeCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPersonDataList     = new ArrayList();
        mLastPersonDataList = new ArrayList();
        mHandler            = new Handler();
        canGetFace          = true;
        isSubmitting        = false;
        AudioTimeUtil.getInstance().clearOldData();

        AIThreadPool.instance().init(this);//重要!!
        // 恢复人脸库
        new AsyncJobBuilder(new StuffBox(), mRecoverFaceLibraryPipelineBuilder).synthesize(/*合成流水线任务*/).launch(/*执行任务*/);
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

                new AsyncJobBuilder(stuffBox, mRetrievePipelineBuilder).synthesize(/*合成流水线任务*/).launch(/*执行任务*/);
            }
        });

        // 初始化UI
        mViewController = new ViewController(this);
        mViewController.setGetFaceListener(this, new AbsActivityViewControllerOri.OnGetFaceListener() {
            @Override
            public void onGetFaceNum(int faceSize) {
                if (faceSize <= 0) {
                    AudioTimeUtil.getInstance().cancelCountDown();
                    mPersonDataList.clear();
                    canGetFace = true;
                    return;
                }
                if (!AudioTimeUtil.getInstance().isCountDown() && !isSubmitting && mPersonDataList.size() > 0) {
                    //倒计时3秒后，抓拍人脸
                    checkFaceWaiteForThreeSecond();
                }
            }

            @Override
            public void onGetFace(Collection<YTFaceTracker.TrackedFace> colorFaces, StuffBox stuffBox) {
                if (isSubmitting) {
                    return;
                }
                mLastPersonDataList.clear();
                for (YTFaceTracker.TrackedFace face : colorFaces) {
                    if (stuffBox.find(RetrievalStep.OUT_RETRIEVE_RESULTS).containsKey(face)) {
                        for (YTFaceRetrieval.RetrievedItem i : stuffBox.find(RetrievalStep.OUT_RETRIEVE_RESULTS).get(face)) {
                            String userName = i.featureId.split("\\.")[0];
                            mLastPersonDataList.add(userName);
                        }
                    }
                }
            }

            @Override
            public void onAddFaceName(String name) {
                if (!canGetFace) {
                    return;
                }
                if (isAddPerson) {
                    return;
                }
                isAddPerson = true;
                if (mPersonDataList.size() == 0) {
                    mPersonDataList.add(name);
                } else {
                    for (int m = 0; m < mPersonDataList.size(); m++) {
                        if (!mPersonDataList.contains(name)) {
                            mPersonDataList.add(name);
                        }
                    }
                }
                isAddPerson = false;
            }
        });
        // 显示UI
        setContentView(mViewController.getRootView());
        TextView tv_title = mViewController.getRootView().findViewById(R.id.title);
        tv_title.setText("离线人脸识别" + (getIntent().getIntExtra("checkType", 1) == 1 ? "(上班)" : "(下班)"));
        ((TextView) mViewController.getRootView().findViewById(R.id.logTxt)).setText("请靠近凝视3秒确认打卡");
        tv_changeCont = mViewController.getRootView().findViewById(R.id.tv_changeCont);
        tv_title.setOnClickListener(v -> {
            //跳转人脸特征值获取
            step2GetFaceValue();
        });
    }

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
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        AudioTimeUtil.getInstance().cancelCountDown();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCameraManager != null) {
            mCameraManager.destroyCamera();
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        AudioTimeUtil.getInstance().stop();
        super.onDestroy();
    }

    private void step2GetFaceValue() {
        Intent intent = new Intent(this, TXLiveFaceCheckActivity.class);
        startActivity(intent);
    }

    public boolean isCanGetFace() {
        return canGetFace;
    }

    /**
     * 恢复人脸库流水线. (运行时人脸库是在内存里的, 每次进程启动时需要恢复内存)
     */
    private final AsyncJobBuilder.PipelineBuilder mRecoverFaceLibraryPipelineBuilder = new AsyncJobBuilder.PipelineBuilder()
            .onThread(AIThreadPool.instance().getHeavyThread())
            .addStep(new PrepareFaceLibraryFromFileStep() {
                @Override
                protected File[] onGetFaceFeatureFiles() {//获得人脸特征文件
                    return new File(SaveFeaturesToFileStep.FACE_LIB_PATH).listFiles();
                }
            })
            .addStep(new RegStep(PrepareFaceLibraryFromFileStep.OUT_FACES_FOR_REG) {//注册入库, 达到恢复人脸库的效果
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    boolean      shouldContinue = super.onProcess(stuffBox);
                    int          size           = stuffBox.find(PrepareFaceLibraryFromFileStep.OUT_FACES_FOR_REG).size();
                    final String msg            = "恢复人脸库: " + size + "个人脸";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RetrieveWithAndroidCameraActivityOri.this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
                    return shouldContinue;
                }
            })
            .submit();

    /**
     * 人脸搜索流水线
     */
    private final AsyncJobBuilder.PipelineBuilder mRetrievePipelineBuilder = new AsyncJobBuilder.PipelineBuilder()
            .onThread(AIThreadPool.instance().getLightThread())
            .addStep(new PreprocessStep(PreprocessStep.IN_RAW_FRAME_GROUP))
            .addStep(new TrackStep())
            .addStep(new PickBestStep(TrackStep.OUT_COLOR_FACE))
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    mViewController.drawLightThreadStuff(stuffBox);//UI绘制
                    return true;
                }
            })
            .submit()
            .onThread(AIThreadPool.instance().getHeavyThread())
            .addStep(new ExtractFeatureStep(TrackStep.OUT_COLOR_FACE))//提取人脸特征, TrackStep.OUT_COLOR_FACE 提取全部人脸特征, PickBestStep.OUT_PICK_OK_FACES: 提取最佳脸特征
            .addStep(new RetrievalStep(ExtractFeatureStep.OUT_FACE_FEATURES))//搜索人脸特征
            .addStep(new AbsStep<StuffBox>() {
                @Override
                protected boolean onProcess(StuffBox stuffBox) {
                    mViewController.drawHeavyThreadStuff(stuffBox);//UI绘制
                    return true;
                }
            })
            .submit();

    /***倒计时3秒，抓拍人脸*/
    private void checkFaceWaiteForThreeSecond() {
        AudioTimeUtil.getInstance().initData(5).setOnTimeListener(new AudioTimeUtil.TimeListener() {
            @Override
            public void onStart(String cont) {
                if (null != tv_changeCont) {
                    tv_changeCont.setVisibility(View.VISIBLE);
                    tv_changeCont.setText(cont);
                }
            }

            @Override
            public void onChange(String cont) {
                if (null != tv_changeCont) {
                    tv_changeCont.setVisibility(View.VISIBLE);
                    tv_changeCont.setText(cont);
                }
            }

            @Override
            public void onCancel() {
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                canGetFace = true;
                if (null != tv_changeCont)
                    tv_changeCont.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinish() {
                if (null != tv_changeCont)
                    tv_changeCont.setVisibility(View.INVISIBLE);
                submitCheckFaceInfo();
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                canGetFace = false;
            }
        }, 1000);
    }

    /****提交人脸信息*/
    private void submitCheckFaceInfo() {
        isSubmitting = true;
        canGetFace   = false;
        ProgressDialogUtil.startShow(this, "正在提交数据...");
        JSONArray peoplelist = new JSONArray();
        for (String userID : mPersonDataList) {
            //剔除离开的人员
            if (mLastPersonDataList.contains(userID)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userid", userID);
                    //jsonObject.put("username", "");
                    jsonObject.put("type", getIntent().getIntExtra("checkType", 1));
                    jsonObject.put("ftime", TimeUtil.getNowDateAndTimeStr());
                    peoplelist.put(jsonObject);
                } catch (Exception e) {
                }
            }
        }
        if (peoplelist.length() == 0) {
            ProgressDialogUtil.hideDialog();
            ToastUtils.showToast("未检测到认证人脸,请先注册人脸信息！");
            isSubmitting = false;
            canGetFace   = true;
            return;
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", peoplelist);
        params.setUseJsonStreamer(true);
        ((TextView) mViewController.getRootView().findViewById(R.id.logTxt)).setText(peoplelist.toString());
        OkHttpUtils.getInstance().doPost(NetConfig.UPDATEWORK, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                //ToastUtils.showToast("网络连接错误，打卡记录提交失败！");
                //isSubmitting = false;
                //canGetFace   = true;
                mPersonDataList.clear();
                //保存打卡信息到本地
                keepWorkInfo(peoplelist);
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                mPersonDataList.clear();
                if (code != 200) {
                    //isSubmitting = false;
                    //canGetFace   = true;
                    //ToastUtils.showToast("网络请求错误，打卡记录提交失败！");
                    //保存打卡信息到本地
                    keepWorkInfo(peoplelist);
                    return;
                }
                Gson              gson       = new Gson();
                UpCheckResultBean resultBean = gson.fromJson(resbody, UpCheckResultBean.class);
                ToastUtils.showToast(resultBean.getMessage());
                tv_changeCont.setVisibility(View.VISIBLE);
                tv_changeCont.setText(resultBean.getMessage());
                if ("1".equals(resultBean.getCode())) {
                    //清除本地记录
                    //播报语音
                    playAudio(resultBean.getAudio());
                } else {
                    //保存打卡信息到本地
                    keepWorkInfo(peoplelist);
                }
            }
        });
    }

    /****保存打卡信息到本地*/
    private void keepWorkInfo(JSONArray peoplelist) {
        ProgressDialogUtil.startShow(this, "正在保存打卡信息");
        MyApplication.isKeepWorkInfo = true;
        UpdateWorkInfoUtil.getInstance().keepCheckHistory(peoplelist, new UpdateWorkInfoUtil.OnKeepWorkInfoListener() {
            @Override
            public void onKeepSuccess() {
                ProgressDialogUtil.hideDialog();
                isSubmitting = false;
                canGetFace   = true;
                MyApplication.isKeepWorkInfo = false;
                ToastUtils.showToast("打卡记录保存成功！");
                SoundUtil.getInstance().playSrcAudio(R.raw.success);
            }

            @Override
            public void onFail() {
                ProgressDialogUtil.hideDialog();
                isSubmitting = false;
                canGetFace   = true;
                MyApplication.isKeepWorkInfo = false;
                ToastUtils.showToast("网络错误，打卡记录保存失败！");
                SoundUtil.getInstance().playSrcAudio(R.raw.fail);
            }
        });
    }

    private String audioFilePath = "";

    /***播报语音
     * @param audio*/
    private void playAudio(String audio) {
        isSubmitting  = true;
        canGetFace    = false;
        audioFilePath = LocalSetting.AUDIO_PATH + "bobao.wav";
        FileUtil.deleteFile(audioFilePath);
        AudioUtil.getInstance().setChangeListener(new AudioUtil.ChangeFileListener() {
            @Override
            public void onSuccess() {
                //播放音频
                //SoundUtil.getInstance().playAudio(audioFilePath);
                SoundMediaPlayerUtil.getInstance().getAudioTime(audioFilePath, new SoundMediaPlayerUtil.OnGetDurationListener() {
                    @Override
                    public void outAudioTime(int duration) {
//                        SoundUtil.getInstance().playAudio(audioFilePath);
                        ThreadUtils.runOnSubThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(duration);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isSubmitting = false;
                                canGetFace   = true;
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed() {
                isSubmitting = false;
                canGetFace   = true;
                ToastUtils.showToast("音频播放失败！");
            }
        }).decoderBase642File(audio, audioFilePath);
    }

    private class ViewController extends AbsActivityViewControllerOri {

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
}
