package com.tencent.cloud.ai.fr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.youtu.YTFaceLive3D;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


@RunWith(AndroidJUnit4.class)
public class YTFaceLive3DTest {
    private final String TAG = "YTFaceTrackerTest:";
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
        System.loadLibrary("YTFaceLive3D");
        System.loadLibrary("YTUtils");
        int ret = YTCommonInterface.initAuth(appContext, "yttestlic20200707", 0);
        if (ret != 0) {
            Log.d(TAG, "【授权失败】");
            return;
        }
        Log.d(TAG, "【授权成功】");
        // Mock 模拟数据，读取 assets 下的文件进行模拟
        int ret_models = YTFaceLive3D.globalInit(appContext.getAssets(), "models/face-live-3d-v300", "config.ini");
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
    public void testV300Detect() {
        Log.d(TAG, "testV300: YTFaceLive3D.version = " + YTFaceLive3D.getVersion());
        YTFaceLive3D ytFaceLive3D = new YTFaceLive3D();
        try {
            Rect rect = new Rect(322, 238, 455, 403);//针对71_rgb.jpg专门测量
            InputStream in = appContext.getAssets().open("match_data/deth3dv300/71_depth.png");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            int startMemory = ToolUtils.getMemoryInfo(appContext);
            System.loadLibrary("YTFaceFeature");
            boolean detect = ytFaceLive3D.detect(rect, data, bitmap.getWidth(), bitmap.getHeight());
            int stopMemory = ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, "ytFaceLive3D.detect  memory consumption:" + (startMemory - stopMemory) + "M");
        } catch (Exception e) {
            Log.e(TAG, "doDetection: ", e);
        }
        Log.d(TAG, "testV300: global release and destroy instance");
    }

    @Test
    public void setThresholdsTest() {
        float[] shild = new float[]{0.3f, 0.8f};
        int i = YTFaceLive3D.setThresholds(shild);
        Log.i(TAG, "setThresholds result:" + i);
    }

    @Test
    public void getFaceDistanceTest() {
        float[] xy5Points0 = {351.348f, 303.025f, 414.266f, 296.948f, 382.633f, 331.165f, 366.336f, 368.357f, 414.098f, 364.508f};
        try {
            InputStream in = appContext.getAssets().open("match_data/deth3dv300/71_depth.png");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            int startMemory = ToolUtils.getMemoryInfo(appContext);
            System.loadLibrary("YTFaceFeature");
            int faceDistance = YTFaceLive3D.getFaceDistance(xy5Points0, data, bitmap.getWidth(), bitmap.getHeight());
            int stopMemory = ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, "getFaceDistance  memory consumption:" + (startMemory - stopMemory) + "M");

            Log.i(TAG, "faceDistance = " + faceDistance);
        } catch (Exception e) {
            Log.e(TAG, "Execption: ", e);
        }
    }

    @Test
    public void getVsersionTest() {
        String version = YTFaceLive3D.getVersion();
        Log.i(TAG, "version = " + version);
    }
}
