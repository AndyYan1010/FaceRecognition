package com.tencent.cloud.ai.fr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.youtu.YTFaceLiveIR;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;


@RunWith(AndroidJUnit4.class)
public class YTFaceLiveIRTest {
    private final String TAG = "YTFaceLiveIRTest:";
    Context appContext;

    AssetManager mgr;

    /**
     * 每个单元测试之前都会调用
     */
    @Before
    public void first_exe() {
        Log.d(TAG, "【开始测试】");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mgr = appContext.getAssets();

        System.loadLibrary("YTCommon");
        System.loadLibrary("YTUtils");
        System.loadLibrary("YTFaceLiveIR");
        System.loadLibrary("YTUtils");
        int ret = YTCommonInterface.initAuth(appContext, "yttestlic20200707", 0);
        if (ret != 0) {
            Log.d(TAG, "【授权失败】");
            return;
        }
        Log.d(TAG, "【授权成功】");
        // Mock 模拟数据，读取 assets 下的文件进行模拟
        int ret_models = YTFaceLiveIR.globalInit(mgr, "models/face-live-ir-v201", "config.ini");
        if (ret_models < 0) {
            Log.e(TAG, "testV300: load from assets: " + ret_models);
            return;
        }

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
    public void testFaceLiveIRdetect() {

        YTFaceLiveIR ytFaceLiveIR = new YTFaceLiveIR();
        float[] color5Points0 = {599.51862f, 387.88733f, 715.74231f, 377.93866f, 661.05371f, 442.20972f, 623.80688f, 501.4454f, 708.30164f, 497.15057f};
        float[] ir5Points0 = {632.64221f, 387.05206f, 766.02307f, 377.16833f, 704.17865f, 452.35312f, 658.81445f, 528.0799f, 757.45276f, 519.92639f};
        try {
                InputStream rgbims = appContext.getAssets().open( "match_datair/v1.3.0/pos_color.jpg");
                Bitmap colorBitmap = BitmapFactory.decodeStream(rgbims);
                InputStream irims = appContext.getAssets().open( "match_datair/v1.3.0/pos_depthIR.jpg");
                Bitmap irBitmap = BitmapFactory.decodeStream(irims);
            int startMemory = ToolUtils.getMemoryInfo(appContext);
            boolean isLive = ytFaceLiveIR.detect(color5Points0, ToolUtils.bitmap2RGB(colorBitmap), colorBitmap.getWidth(), colorBitmap.getHeight(),
                    ir5Points0, ToolUtils.bitmap2RGB(irBitmap), irBitmap.getWidth(), irBitmap.getHeight());
            int stopMemory = ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, "detect memory consumption:" + (startMemory - stopMemory) + "M");

                Log.i(TAG, "FaceLiveIR: "+isLive);
            float  score =  ytFaceLiveIR.getScore();
            Log.i(TAG, "FaceLiveIR: "+isLive);
            float[] scores = ytFaceLiveIR.getScores();
            for (int i=0;i<scores.length;i++){
                Log.e(TAG, "scores["+i+"]: "+scores[i]);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception: ", ex);
        }
    }
    @Test
    public void getVsersionTest(){
        String version = YTFaceLiveIR.getVersion();
        Log.i(TAG,"version = "+version);
    }

    @Test
    public void setThresholdsTest() {
        float[] shild = new float[]{0.3f, 0.8f};
        //前面的阈值必须要小于后面的阈值
        int i = YTFaceLiveIR.setThresholds(shild);
        Log.i(TAG, "setThresholds result:" + i);
    }

    @Test
    public void globalReleaseTest(){
        YTFaceLiveIR.globalRelease();
    }
}
