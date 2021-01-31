package com.tencent.cloud.ai.fr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.cloud.ai.fr.sdksupport.YTSDKManager;
import com.tencent.youtu.YTFaceTracker;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.tencent.cloud.ai.fr.ToolUtils.bitmap2RGB;

@RunWith(AndroidJUnit4.class)
public class YTFaceTrackerTest {
    private final String TAG = "YTFaceTrackerTest:";
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

    @Test
    public void colorTrackTest(){
        try {


            Frame frameColor = new Frame();
            frameColor.id = "3_color.jpg";
            Bitmap bitmap = BitmapFactory.decodeStream(mgr.open("frames/3_color.jpg"));
            frameColor.data = bitmap2RGB(bitmap);
            frameColor.width = bitmap.getWidth();
            frameColor.height = bitmap.getHeight();


            int startMemory = ToolUtils.getMemoryInfo(appContext);
            long start = System.currentTimeMillis();
            //track是从视频帧中获取数据
            YTFaceTracker.TrackedFace[] trackedFaces = manager.mYTFaceTracker.track(frameColor.data, frameColor.width, frameColor.height);
            long end = System.currentTimeMillis();
            int stopMemory =  ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, "track memory consumption:" +(startMemory-stopMemory)+"M");
            manager = new YTSDKManager(appContext.getAssets());
            int ret = YTCommonInterface.initAuth(appContext, "yttestlic20200707", 0);
            Assert.assertNotNull(trackedFaces);
            Log.d(TAG, "【track耗时】 " + (end - start));
            if (trackedFaces == null || trackedFaces.length == 0) {
                return;
            }
            Log.d(TAG, "【tracked face count: 】" + trackedFaces.length);
            Log.d(TAG, "【tracked face rect: 】" + trackedFaces[0].faceRect);
            Log.d(TAG, "【tracked face pitch: 】" + trackedFaces[0].pitch);
            Log.d(TAG, "【tracked face roll: 】" + trackedFaces[0].roll);
            Log.d(TAG, "【tracked face yaw: 】" + trackedFaces[0].yaw);
            Log.d(TAG, "【tracked face xy5Points: 】" + trackedFaces[0].xy5Points);
            Log.d(TAG, "【tracked face traceId: 】" + trackedFaces[0].traceId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void colorDetectTest(){
        YTFaceTracker.Options options = new YTFaceTracker.Options();
        options.minFaceSize = 50;//如果检测不到人脸, 可以尝试调小这个值
        options.maxFaceSize = 500;//最大脸也就图那么大
        options.biggerFaceMode = true;
        try {


            Frame frameColor = new Frame();
            frameColor.id = "3_color.jpg";
            Bitmap bitmap = BitmapFactory.decodeStream(mgr.open("frames/3_color.jpg"));
            frameColor.data = bitmap2RGB(bitmap);
            frameColor.width = bitmap.getWidth();
            frameColor.height = bitmap.getHeight();

            int startMemory =  ToolUtils.getMemoryInfo(appContext);
            long start = System.currentTimeMillis();
            //detect是从单帧中获取数据，不连续的图像中获取数据
            YTFaceTracker.TrackedFace[] trackedFaces = manager.mYTFaceTracker.detect(frameColor.data, frameColor.width, frameColor.height,options);
            long end = System.currentTimeMillis();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int stopMemory =  ToolUtils.getMemoryInfo(appContext);
            Log.d(TAG, " mYTFaceTracker detect memory consumption:" +(startMemory-stopMemory)+"M");
            Assert.assertNotNull(trackedFaces);
            Log.d(TAG, "【track耗时】 " + (end - start));
            if (trackedFaces == null || trackedFaces.length == 0) {
                return;
            }
            Log.d(TAG, "【detect face count: 】" + trackedFaces.length);
            Log.d(TAG, "【detect face rect: 】" + trackedFaces[0].faceRect);
            Log.d(TAG, "【detect face pitch: 】" + trackedFaces[0].pitch);
            Log.d(TAG, "【detect face roll: 】" + trackedFaces[0].roll);
            Log.d(TAG, "【detect face yaw: 】" + trackedFaces[0].yaw);
            Log.d(TAG, "【detect face xy5Points: 】" + trackedFaces[0].xy5Points);
            Log.d(TAG, "【detect face traceId: 】" + trackedFaces[0].traceId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static class Frame {
        String id;
        byte[] data;
        int height;
        int width;
    }
    @Test
    public void trackTest(){
        try {

            Frame frameColor = new Frame();
            frameColor.id = "1_color.jpg";
            Bitmap bitmap = BitmapFactory.decodeStream(mgr.open("frames/1_color.jpg"));
            frameColor.data = bitmap2RGB(bitmap);
            frameColor.width = bitmap.getWidth();
            frameColor.height = bitmap.getHeight();

            long start = System.currentTimeMillis();
            YTFaceTracker.TrackedFace[] trackedFaces = manager.mYTFaceTracker.track(frameColor.data, frameColor.width, frameColor.height);
            // 要对追踪的结果进行 null 判断
            if (trackedFaces == null || trackedFaces.length == 0) {
                return;
            }
            Log.d(TAG, "feedFrame [" + frameColor.id + "]: tracked face count: " + trackedFaces.length);
            long end = System.currentTimeMillis();
            Log.i(TAG, "feedFrame [" + frameColor.id + "]: single frame cost: " + (end - start));
        } catch (Exception e) {
            Log.e(TAG, "run: mock error", e);
        }
    }
    @Test
    public void newTrackerTest(){
        int startMemory =  ToolUtils.getMemoryInfo(appContext);
       YTFaceTracker tracker = new YTFaceTracker();
        int stopMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "newTracker memory consumption:" +(startMemory-stopMemory)+"M");
    }
}
