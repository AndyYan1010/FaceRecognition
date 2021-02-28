package com.botian.recognition.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.bean.CheckFaceHistory;
import com.botian.recognition.bean.UpCheckResultBean;
import com.botian.recognition.configure.LocalSetting;
import com.botian.recognition.sdksupport.AIThreadPool;
import com.botian.recognition.sdksupport.AbsActivityViewController;
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
import com.botian.recognition.utils.fileUtils.AudioUtil;
import com.botian.recognition.utils.fileUtils.FileUtil;
import com.botian.recognition.utils.mediaUtils.SoundMediaPlayerUtil;
import com.botian.recognition.utils.mediaUtils.SoundUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.botian.recognition.utils.netUtils.ThreadUtils;
import com.btface.greendaodemo.DaoSession;
import com.google.android.cameraview.CameraView;
import com.google.gson.Gson;
import com.tencent.cloud.ai.fr.camera.Frame;
import com.tencent.cloud.ai.fr.camera.Frame.Format;
import com.tencent.cloud.ai.fr.camera.FrameGroup;
import com.tencent.cloud.ai.fr.camera.ICameraManager;
import com.tencent.cloud.ai.fr.camera.ICameraManager.OnFrameArriveListener;
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

public class RetrieveWithAndroidCameraActivity extends AppCompatActivity {
    private ICameraManager            mCameraManager;
    private AbsActivityViewController mViewController;
    private TextView                  tv_changeCont;
    private boolean                   isSubmitting = false;
    private List<String>              mOriCheckPersonNameList;//记录初始抓取到的人脸

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOriCheckPersonNameList = new ArrayList();
        AIThreadPool.instance().init(this);//重要!!
        // 恢复人脸库
        new AsyncJobBuilder(new StuffBox(), mRecoverFaceLibraryPipelineBuilder).synthesize(/*合成流水线任务*/).launch(/*执行任务*/);
        // 初始化相机
        mCameraManager = new AndroidCameraManager(this);
        // 监听相机帧回调
        mCameraManager.setOnFrameArriveListener(new OnFrameArriveListener() {//从相机获取帧
            @Override
            public void onFrameArrive(byte[] data, int width, int height, int exifOrientation) {
                //彩图
                Frame colorFrame = new Frame(Format.YUV_NV21, data, width, height, exifOrientation);
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
        mViewController.setGetFaceListener(new AbsActivityViewController.GetFaceListener() {
            @Override
            public void getFaceNum(StuffBox stuffBox, Collection<YTFaceTracker.TrackedFace> allFaces, int faceSize) {
                if (faceSize <= 0) {
                    AudioTimeUtil.getInstance().cancelCountDown();
                    mOriCheckPersonNameList.clear();
                    return;
                }
                if (!AudioTimeUtil.getInstance().isCountDown() && !isSubmitting && mOriCheckPersonNameList.size() > 0) {
                    //倒计时3秒后，抓拍人脸
                    checkFaceWaiteForThreeSecond();
                }
            }

            @Override
            public void addCheckName(String personName) {
                if (mOriCheckPersonNameList.size() == 0) {
                    mOriCheckPersonNameList.add(personName);
                } else {
                    for (int i = 0; i < mOriCheckPersonNameList.size(); i++) {
                        if (!mOriCheckPersonNameList.get(i).equals(personName)) {
                            mOriCheckPersonNameList.add(personName);
                        }
                    }
                }
            }
        });
        // 显示UI
        setContentView(mViewController.getRootView());
        mViewController.setOriPersonList(mOriCheckPersonNameList);

        ((TextView) mViewController.getRootView().findViewById(R.id.title))
                .setText("离线人脸识别" + (getIntent().getIntExtra("checkType", 1) == 1 ? "(上班)" : "(下班)"));
        ((TextView) mViewController.getRootView().findViewById(R.id.logTxt)).setText("请靠近凝视3秒确认打卡");
        tv_changeCont = mViewController.getRootView().findViewById(R.id.tv_changeCont);
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
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCameraManager != null) {
            mCameraManager.destroyCamera();
        }
        super.onDestroy();

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
                            Toast.makeText(RetrieveWithAndroidCameraActivity.this, msg, Toast.LENGTH_LONG).show();
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
                    //if (canSubData && !isSubmitting) {
                    //    //保存打卡记录
                    //    //keepCheckHistory(faceForConfirm.name);
                    //    //提交打卡信息
                    //    
                    //}
                    mStuffBox = stuffBox;
                    return true;
                }
            })
            .submit();

    private StuffBox mStuffBox;

    /****提交人脸信息*/
    private void submitCheckFaceInfo() {
        isSubmitting = true;
        ProgressDialogUtil.startShow(this, "正在提交数据...");
        JSONArray                             peoplelist = new JSONArray();
        Collection<YTFaceTracker.TrackedFace> allFaces   = mStuffBox.find(TrackStep.OUT_COLOR_FACE);
        for (YTFaceTracker.TrackedFace face : allFaces) {
            if (mStuffBox.find(RetrievalStep.OUT_RETRIEVE_RESULTS).containsKey(face)) {
                for (YTFaceRetrieval.RetrievedItem i : mStuffBox.find(RetrievalStep.OUT_RETRIEVE_RESULTS).get(face)) {
                    String userName = i.featureId.split("\\.")[0];
                    for (String name : mOriCheckPersonNameList) {
                        if (userName.equals(name)) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("userid", "1001");
                                jsonObject.put("username", userName);
                                jsonObject.put("type", getIntent().getIntExtra("checkType", 1));
                                jsonObject.put("ftime", TimeUtil.getNowDateAndTimeStr());
                                peoplelist.put(jsonObject);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }
                }
            }
        }
        if (peoplelist.length() == 0) {
            ProgressDialogUtil.hideDialog();
            ToastUtils.showToast("未检测到认证人脸,请先注册人脸信息！");
            isSubmitting = false;
            return;
        }
        RequestParamsFM params = new RequestParamsFM();
        params.put("peoplelist", peoplelist);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.UPDATEWORK, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast("网络连接错误，打卡记录提交失败！");
                isSubmitting = false;
                mOriCheckPersonNameList.clear();
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                mOriCheckPersonNameList.clear();
                if (code != 200) {
                    isSubmitting = false;
                    ToastUtils.showToast("网络请求错误，打卡记录提交失败！");
                    return;
                }
                Gson              gson       = new Gson();
                UpCheckResultBean resultBean = gson.fromJson(resbody, UpCheckResultBean.class);
                ToastUtils.showToast(resultBean.getMessage());
                tv_changeCont.setVisibility(View.VISIBLE);
                tv_changeCont.setText(resultBean.getMessage());
                if ("1".equals(resultBean.getCode())) {
                    //清除本地记录
                }
                //播报语音
                playAudio(resultBean.getAudio());
            }
        });
    }

    private String audioFilePath = "";

    /***播报语音
     * @param audio*/
    private void playAudio(String audio) {
        isSubmitting  = true;
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
                        SoundUtil.getInstance().playAudio(audioFilePath);
                        ThreadUtils.runOnSubThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(duration);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isSubmitting = false;
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed() {
                isSubmitting = false;
                ToastUtils.showToast("音频播放失败！");
            }
        }).decoderBase642File(audio, audioFilePath);
    }

    /***倒计时3秒，抓拍人脸*/
    private void checkFaceWaiteForThreeSecond() {
        AudioTimeUtil.getInstance().initData(3).setOnTimeListener(new AudioTimeUtil.TimeListener() {
            @Override
            public void onStart(String cont) {
                tv_changeCont.setVisibility(View.VISIBLE);
                tv_changeCont.setText(cont);
            }

            @Override
            public void onChange(String cont) {
                tv_changeCont.setVisibility(View.VISIBLE);
                tv_changeCont.setText(cont);
            }

            @Override
            public void onCancel() {
                tv_changeCont.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinish() {
                tv_changeCont.setVisibility(View.INVISIBLE);
                submitCheckFaceInfo();
            }
        });
    }

    /***确认打卡记录
     * @param name*/
    private void keepCheckHistory(String name) {
        //获取GreenDao数据库操作对象
        DaoSession       daoSession       = MyApplication.getDaoSession();
        CheckFaceHistory checkFaceHistory = new CheckFaceHistory();
        checkFaceHistory.setUserName(name);
        checkFaceHistory.setUserID("1001");
        checkFaceHistory.setCheckType(String.valueOf(getIntent().getIntExtra("checkType", -1)));
        checkFaceHistory.setFtime(TimeUtil.getNowDateAndTimeStr());
        daoSession.insert(checkFaceHistory);
        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                tv_changeCont.setVisibility(View.VISIBLE);
                tv_changeCont.setText(name + "已成功打卡");
            }
        });
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
}
