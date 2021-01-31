package com.tencent.cloud.ai.fr;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.cloud.ai.fr.lib.R;
import com.tencent.cloud.ai.fr.sdksupport.YTSDKManager;
import com.tencent.cloud.ai.fr.utils.ImageConverter;
import com.tencent.youtu.YTFaceAlignment;
import com.tencent.youtu.YTFaceFeature;
import com.tencent.youtu.YTFaceLive3D;
import com.tencent.youtu.YTFaceLiveColor;
import com.tencent.youtu.YTFaceLiveIR;
import com.tencent.youtu.YTFaceQuality;
import com.tencent.youtu.YTFaceQualityPro;
import com.tencent.youtu.YTFaceTracker;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.ContentValues.TAG;

/**
 * Instrumented test, which will execute on an Android device.
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class YTSDKManagerTest {
    Context appContext;
    AssetManager mgr;
    int result;
    @Before
    public void first_exe(){
        Log.d(TAG, "【Test Start】");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mgr = appContext.getAssets();
    }
    @After
    public void after_exe(){
        Log.d(TAG, "【Test End】");
    }

    @Test
    public void useAppContext() {
    }

    /**
     * YTSDKManager一个YTSDKManager对应一个destory,重复destory容易导致空指针异常
     */
    @Test
    public void destroyTest() {
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        YTSDKManager manger = new YTSDKManager(appContext.getAssets());
        manger.destroy();
        manger.destroy();
        manger.destroy();
    }

    /**
     * 创建YTSDKManager耗时测试
     */
    @Test
    public void createYTSDKManagerTest(){
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        long start = System.currentTimeMillis();
        YTSDKManager manger = new YTSDKManager(appContext.getAssets());
        long end = System.currentTimeMillis();
        Log.d(TAG, "new YTSDKManager memory consumption:" + (end - start));
    }
    /**
     * 提取特征测试
     */
    @Test
    public void extractFaceFeature() {
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap faceBitmap = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.sample,options);
        //转换图片为 RGB888 格式
        byte[] faceRgb = ImageConverter.bitmap2RGB(faceBitmap);
        YTSDKManager manger = new YTSDKManager(appContext.getAssets());

        float[] feature = manger.extractFaceFeature(faceRgb, faceBitmap.getWidth(), faceBitmap.getHeight());
        Assert.assertNull(feature);
    }

    @Test
    public void loadLibs() {
        //加载jar包，jar包内容不多，耗费内存几乎可以忽略，统计一次即可
        int startMemory1 =  ToolUtils.getMemoryInfo(appContext);
        System.loadLibrary("YTUtils");
        System.loadLibrary("YTFaceFeature");
        System.loadLibrary("YTFaceRetrieval");
        System.loadLibrary("YTFaceLive3D");
        System.loadLibrary("YTFaceLiveColor");
        System.loadLibrary("YTFaceLiveIR");
        System.loadLibrary("YTFaceQuality");
        System.loadLibrary("YTFaceQualityPro");
        System.loadLibrary("YTFaceTracker");
        System.loadLibrary("YTFaceAlignment");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int stopMemory1 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "loadLibs memory consumption:" +(startMemory1-stopMemory1)+"M");


    }


    /**
     * 加载so测试
     */
    @Test
    public void testLoadLibs() {
        long start = System.currentTimeMillis();
        YTSDKManager.loadLibs();
        long end = System.currentTimeMillis();
        Log.d(TAG, "loadLibs cost time" + (end - start));
    }

    /**
     * 加载模型测试
     */
    @Test
    public void loadModels() throws InterruptedException {
        YTSDKManager.loadLibs();
        Thread.sleep(5000);//测试前，先休眠5秒，防止前期内存陡增影响后面的结果

        int startMemory1 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceAlignment.globalInit(mgr, "models/face-align-v6.3.0", "config.ini");
        Thread.sleep(2000);
        int stopMemory1 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "YTFaceAlignment memory consumption::" +(startMemory1-stopMemory1)+"M");

        int startMemory2 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceFeature.globalInit(mgr, "models/face-feature-v704", "config.ini");
        Thread.sleep(2000);
        int stopMemory2 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "YTFaceFeature memory consumption:" +(startMemory2-stopMemory2)+"M");

        int startMemory3 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceLive3D.globalInit(mgr, "models/face-live-3d-v300", "config.ini");
        Thread.sleep(2000);
        int stopMemory3 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "face-live-3d-v300 memory consumption:" +(startMemory3-stopMemory3)+"M");

        int startMemory4 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceLiveColor.globalInit(mgr, "models/face-live-color-v123", "config.ini");
        Thread.sleep(2000);
        int stopMemory4 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "face-live-color memory consumption:" +(startMemory4-stopMemory4)+"M");

        int startMemory5 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceLiveIR.globalInit(mgr, "models/face-live-ir-v201", "config.ini");
        Thread.sleep(2000);
        int stopMemory5 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "face-live-ir memory consumption::" +(startMemory5-stopMemory5)+"M");

        int startMemory6 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceQuality.globalInit(mgr, "models/face-quality-v111", "config.ini");
        Thread.sleep(2000);
        int stopMemory6 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "face-quality-v111 memory consumption:" +(startMemory6-stopMemory6)+"M");

        int startMemory7 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceQualityPro.globalInit(mgr, "models/face-quality-pro-v201", "config.ini");
        Thread.sleep(2000);
        int stopMemory7 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "face-quality-pro-v201 memory consumption:" +(startMemory7-stopMemory7)+"M");

        int startMemory8 =  ToolUtils.getMemoryInfo(appContext);
        result = YTFaceTracker.globalInit(mgr, "models/face-tracker-v5.3.5+v4.1.0", "config.ini");
        Thread.sleep(2000);
        int stopMemory8 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "face-tracker memory consumption::" +(startMemory8-stopMemory8)+"M");

    }
    @Test
    public void testLoadModelAll(){
        long start = System.currentTimeMillis();
        int startMemory1 =  ToolUtils.getMemoryInfo(appContext);
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        long end = System.currentTimeMillis();
        int  stopMemory1=  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "LoadModelAll memory consumption:" +(startMemory1-stopMemory1)+"M");
        Log.d(TAG, "LoadModelAll cost time " + (end - start));
    }

    /**
     * 测试创建YTSDKManager耗时
     */
    @Test
    public void ManagerNewTest(){
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int startMemory1 =  ToolUtils.getMemoryInfo(appContext);
        YTSDKManager manger = new YTSDKManager(appContext.getAssets());
        int  stopMemory1=  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "ManagerNew memory consumption:" +(startMemory1-stopMemory1)+"M");

    }

    @Test
    public void releaseModelTest(){
        YTSDKManager.loadLibs();
        YTSDKManager.loadModels(appContext.getAssets());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int startMemory1 =  ToolUtils.getMemoryInfo(appContext);
        YTFaceFeature.globalRelease();
        YTFaceLiveColor.globalRelease();
        YTFaceLive3D.globalRelease();
        YTFaceLiveIR.globalRelease();
        YTFaceQuality.globalRelease();
        YTFaceQualityPro.globalRelease();
        YTFaceTracker.globalRelease();
        YTFaceAlignment.globalRelease();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int stopMemory1 =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "releaseModel memory consumption:" +(stopMemory1-startMemory1)+"M");
    }
}
