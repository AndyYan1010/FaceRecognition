package com.botian.recognition.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.cameraview.CameraView;
import com.tencent.cloud.ai.fr.camera.Frame;
import com.tencent.cloud.ai.fr.camera.FrameGroup;
import com.tencent.cloud.ai.fr.camera.ICameraManager;
import com.tencent.cloud.ai.fr.pipeline.AbsStep;

import java.io.File;
import java.util.Collection;

public class RetrieveWithAndroidCameraActivityOri extends AppCompatActivity {
    private static final String                       TAG = RetrieveWithAndroidCameraActivity.class.getSimpleName();
    private              ICameraManager               mCameraManager;
    private              AbsActivityViewControllerOri mViewController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        // 显示UI
        setContentView(mViewController.getRootView());
        // 设置UI按钮
        mViewController.addButton("切换相机", new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                ((AndroidCameraManager) mCameraManager).switchCamera();//切换相机
            }
        });

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
}