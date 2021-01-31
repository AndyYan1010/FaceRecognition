package com.tencent.cloud.ai.fr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.youtu.YTFaceFeature;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;


@RunWith(AndroidJUnit4.class)
public class YTFaceFeatureTest {
    private final String TAG = "YTFaceFeatureTest:";
    Context appContext;
    YTFaceFeature mYTFaceFeature;
    AssetManager mgr;

    /**
     * 每个单元测试之前都会调用
     */
    @Before
    public void first_exe() {
        Log.d(TAG, "【开始测试】");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mgr = appContext.getAssets();
        int startMemory = ToolUtils.getMemoryInfo(appContext);
        System.loadLibrary("YTFaceFeature");
        int result = YTFaceFeature.globalInit(appContext.getAssets(), "models/face-feature-v704", "config.ini");
        Assert.assertEquals(0, result);//断言是否是加载模型正确
        int stopMemory = ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "【加载YTFaceFeature so和数据模型内存消耗】:" + (startMemory - stopMemory) + "M");
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
    public void testInitFeature() {
        int initREt = YTFaceFeature.globalInit(appContext.getAssets(), "models/face-feature-v703", "config.ini");
        if (initREt < 0) {
            Log.e(TAG, "testV704: loadModels: " + initREt);
            return;
        }
        Log.d(TAG, "testV704: YTFaceFeature SDK version: " + YTFaceFeature.getVersion());
    }


    @Test
    public void FeatureUnit() {
        mYTFaceFeature = new YTFaceFeature();
        float[] xy5Points0 = {133.360f, 176.401f, 223.730f, 177.795f, 174.014f, 230.962f, 137.555f, 279.846f, 212.404f, 282.790f};
        String path = "match_data/v704/11010819631012501X.bmp";
        try {
            float[] feature = new float[YTFaceFeature.FEATURE_LENGTH];
            InputStream ims = appContext.getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            long start = System.currentTimeMillis();
            int startMemory = ToolUtils.getMemoryInfo(appContext);
            int extResult = mYTFaceFeature.extract(xy5Points0, ToolUtils.bitmap2RGB(bitmap), bitmap.getWidth(), bitmap.getHeight(), feature);
            int stopMemory = ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, "extract  memory consumption " + (startMemory - stopMemory) + "M");
            long end = System.currentTimeMillis();
            Log.d(TAG, "extract 11010819631012501X.bmp cost time = " + (end - start));
            Assert.assertEquals(0, extResult);
            if (extResult == 0) {
                Log.e(TAG, "extract success");
            }
        } catch (IOException ex) {
            Log.e(TAG, "testV704: " + ex);
        }
    }


    /**
     * 测试模型数据释放
     */
    @Test
    public void releaseModelTest() {
        int startMemory = ToolUtils.getMemoryInfo(appContext);
        YTFaceFeature.globalRelease();
        int stopMemory = ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "releaseModel memory consumption:" + (stopMemory - startMemory) + "M");
    }

    /**
     * 测试模型数据释放
     */
    @Test
    public void versionTest() {
        String version = YTFaceFeature.getVersion();
        Log.i(TAG, version);
    }

    @Test
    public void createObjectTest() {
        int startMemory = ToolUtils.getMemoryInfo(appContext);
        mYTFaceFeature = new YTFaceFeature();
        int stopMemory = ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "createFeature memory consumption:" + (startMemory - stopMemory) + "M");
    }


}
