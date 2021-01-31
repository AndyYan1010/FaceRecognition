package com.tencent.cloud.ai.fr;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.youtu.YTFaceQuality;
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
public class YTFaceQualityTest {
    private final String TAG = "YTFaceQualityTest:";
    Context appContext;
    YTFaceQuality ytFaceQuality;

    /**
     * 每个单元测试之前都会调用
     */
    @Before
    public void first_exe() {
        Log.d(TAG, "【开始测试】");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        System.loadLibrary("YTCommon");
        System.loadLibrary("YTUtils");
        System.loadLibrary("YTFaceQuality");
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
    public void testYTFaceQualityTest() {

        int ret = YTFaceQuality.globalInit(appContext.getAssets(), "models/face-quality-v111", "config.ini");
        String path = "match_data/quality/v312/test.png";
        Log.d(TAG, "YTFaceQuality.globalInit: " + ret);
        Log.d(TAG, "YTFaceQuality.version: " + YTFaceQuality.getVersion());
        float[] xy5Points0 = {68.0309f, 66.0951f, 91.6758f, 70.3816f, 67.6161f, 87.2511f, 64.9889f, 100.94f, 86.0066f, 106.333f};
        YTFaceQuality ytFaceQuality = new YTFaceQuality();
        try {

                InputStream ims = appContext.getAssets().open(path);
                Bitmap bitmap = BitmapFactory.decodeStream(ims);

                long start = System.currentTimeMillis();

            int startMemory =  ToolUtils.getMemoryInfo(appContext);
            float[] scores = ytFaceQuality.evaluate(
                    xy5Points0,
                    ToolUtils.bitmap2RGB(bitmap),
                    bitmap.getWidth(),
                    bitmap.getHeight()
            );
            int endMemory =  ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, "YTFaceQuality memory consumption:" +(startMemory-endMemory)+"M");


                long end = System.currentTimeMillis();
                Log.d(TAG, "testv312: test.png cost time = " + (end - start));
                for (int i = 0; i <scores.length ; i++) {
                    Log.i(TAG, "score["+i+"]" + scores[i]);
                }
        } catch (IOException ex) {
            Log.e(TAG, "some error happened: " + ex);
        }
        ytFaceQuality.destroy();
        YTFaceQuality.globalRelease();
    }
    @Test
    public void releaseModel(){
        int ret = YTFaceQuality.globalInit(appContext.getAssets(), "models/face-quality-v111", "config.ini");
        YTFaceQuality.globalRelease();
    }

    @Test
    public  void testNewQuality(){
        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        YTFaceQuality ytFaceQuality = new YTFaceQuality();
        int endMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "new YTFaceQuality memory consumption:" +(startMemory-endMemory)+"M");
    }
}
