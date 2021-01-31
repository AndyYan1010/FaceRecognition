package com.tencent.cloud.ai.fr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.cloud.ai.fr.sdksupport.YTSDKManager;
import com.tencent.youtu.YTFaceAlignment;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class YTFaceAlignmentTest {
    private final String TAG = "YTFaceAlignmentTest:";
    Context appContext;
    YTSDKManager manager;
    AssetManager mgr;
    /**
     * 每个单元测试之前都会调用
     */
    @Before
    public void first_exe(){
        Log.d(TAG, "【开始测试】");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mgr = appContext.getAssets();

        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        manager = new YTSDKManager(appContext.getAssets());

        int ret = YTCommonInterface.initAuth(appContext, "yttestlic20200707", 0);

        if (ret != 0) {
            Log.d(TAG, "【授权失败】");
            return;
        }
        Log.d(TAG, "【授权成功】");

    }


    /**
     * 每个单元测试之后都会调用
     */
    @After
    public void after_exe(){
        Log.d(TAG, "【测试完毕】");
    }

    @Test
    public void useAppContext() {

    }
    public static class Frame {
        String id;
        byte[] data;
        int height;
        int width;
    }
    @Test
    public void colorAlignTest(){
        try {
            Rect rect = new Rect(322, 238, 455, 403);//针对71_rgb.jpg专门测量
            InputStream in = appContext.getAssets().open("match_data/deth3dv300/71_depth.png");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            int startMemory = ToolUtils.getMemoryInfo(appContext);
            YTFaceAlignment.FaceShape shape = manager.mYTFaceAlignment.align(data, bitmap.getWidth(), bitmap.getHeight(), rect);
            int endMemory = ToolUtils.getMemoryInfo(appContext);
            Log.i(TAG,"align memory consumption:"+(startMemory-endMemory)+"M");

            float[] faceProfile = shape.faceProfile;
            float[] leftEyebrow = shape.leftEyebrow;
            float[] rightEyebrow= shape.rightEyebrow;
            float[] leftEye =leftEyebrow;
            float[] rightEye = shape.rightEye;
            float[] nose = shape.nose;
            float[] mouth = shape.mouth;
            float[] pupil = shape.leftEyebrow;
            float[] faceProfileVis = shape.faceProfileVis;
            float[] leftEyebrowVis = shape.leftEyebrowVis;
            float[] rightEyebrowVis = shape.rightEyebrowVis;
            float[] leftEyeVis = shape.leftEyeVis;
            float[] rightEyeVis = shape.rightEyeVis;
            float[] noseVis = shape.mouthVis;
            float[] mouthVis = shape.mouthVis;
            float[] pupilVis = shape.pupilVis;
            float   confidence = shape.confidence;
            float   pitch = shape.pitch;
            float   yaw = shape.yaw;
            float   roll = shape.roll;
            Rect    faceRect = shape.faceRect;



            int startMemory2 = ToolUtils.getMemoryInfo(appContext);
            YTFaceAlignment.FaceStatus status = manager.mYTFaceAlignment.getStatus(shape);
            int endMemory2 = ToolUtils.getMemoryInfo(appContext);
            Log.i(TAG,"getStatus memory consumption:"+(startMemory2-endMemory2)+"M");
            int pupilDist = status.pupilDist;
            float leftEyeOpen = status.leftEyeOpen;
            float rightEyeOpen = status.rightEyeOpen;
            float mouthOpen=status.mouthOpen;
            float leftEyebrowBlock=status.leftEyebrowBlock;
            float rightEyebrowBlock=status.rightEyebrowBlock;
            float leftEyeBlock=status.leftEyeBlock;
            float rightEyeBlock=status.rightEyeBlock;
            float noseBlock=status.noseBlock;
            float mouthBlock=status.mouthBlock;
            float leftProfileBlock=status.leftProfileBlock;
            float chinBlock=status.chinBlock;
            float rightProfileBlock=status.rightProfileBlock;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
