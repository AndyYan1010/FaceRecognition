package com.tencent.cloud.ai.fr;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.cloud.ai.fr.sdksupport.YTSDKManager;
import com.tencent.youtu.YTFaceQuality;
import com.tencent.youtu.YTFaceQualityPro;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class YTFaceQualityProTest {
    private final String TAG = "YTFaceQualityProTest:";
    Context appContext;
    YTFaceQualityPro ytFaceQualityPro;

    /**
     * 每个单元测试之前都会调用
     */
    @Before
    public void first_exe() {
        Log.d(TAG, "【开始测试】");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());

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
    public void after_exe() {
        Log.d(TAG, "【测试完毕】");
    }

    @Test
    public void useAppContext() {

    }

    @Test
    public void testQualityProEvaluate() {
        ytFaceQualityPro = new YTFaceQualityPro();
        String path = "match_data/v201/test.png";
        int ret = YTFaceQuality.globalInit(appContext.getAssets(), "models/face-quality-pro-v201", "config.ini");
        float[] xy5Points0 = {68.0309f, 66.0951f, 91.6758f, 70.3816f, 67.6161f, 87.2511f, 64.9889f, 100.94f, 86.0066f, 106.333f};
        Log.d(TAG, "YTFaceQualityPro.globalInit: " + ret);
        Log.d(TAG, "YTFaceQuality.version: " + YTFaceQuality.getVersion());
        try {
                InputStream ims = appContext.getAssets().open(path);
                Bitmap bitmap = BitmapFactory.decodeStream(ims);
                long start = System.currentTimeMillis();
                int startMemory = ToolUtils.getMemoryInfo(appContext);
                float[] score = ytFaceQualityPro.evaluate(xy5Points0, ToolUtils.bitmap2RGB(bitmap), bitmap.getWidth(), bitmap.getHeight());
                int stopMemory = ToolUtils.getMemoryInfo(appContext);
                Log.d(TAG, "evaluate memory consumption:" + (startMemory - stopMemory) + "M");

                long end = System.currentTimeMillis();
                Log.d(TAG, "evaluate test.png cost time = " + (end - start));
                for (int i = 0; i < score.length; i++) {
                    float facingScore = score[0];      // 角度：分数越低，角度越大。
                    Log.d(TAG, "【角度】:" + facingScore);
                    float visibilityScore = score[1];  // 遮挡：分数越低，遮挡程度越严重。
                    Log.d(TAG, "【遮挡】:" + visibilityScore);
                    float sharpScore = score[2];       // 模糊：分数越低，模糊程度越严重。
                    Log.d(TAG, "【模糊】:" + sharpScore);
                    float brightnessScore = score[3];  // 光线：分数越低，光线越暗，分数越高，光线越亮。
                    Log.d(TAG, "【亮度】:" + sharpScore);
                }
        } catch (IOException ex) {
            Log.e(TAG, "some error happened: " + ex);
        }
        ytFaceQualityPro.destroy();
    }
    @Test
    public void releaseModel(){
        int ret = YTFaceQuality.globalInit(appContext.getAssets(), "models/face-quality-pro-v201", "config.ini");
        YTFaceQuality.globalRelease();
    }
    @Test
    public void newQuailtyPro(){
        int startMemory = ToolUtils.getMemoryInfo(appContext);
        YTFaceQualityPro ytFaceQualityPro = new YTFaceQualityPro();
        int stopMemory = ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "evaluate memory consumption:" + (startMemory - stopMemory) + "M");
    }
}
