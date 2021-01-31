package com.tencent.cloud.ai.fr;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tencent.youtu.YTFaceRetrieval;
import com.tencent.ytcommon.util.YTCommonInterface;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


@RunWith(AndroidJUnit4.class)
public class YTFaceRetrieveTest {
    private final String TAG = "YTFaceRetrieveTest:";
    public  int FEAT_LENGTH = 512;
    Context appContext;
    AssetManager mgr;
    YTFaceRetrieval mYTFaceRetrieve;
    /**
     * 每个单元测试之前都会调用
     */
    @Before
    public void first_exe(){
        Log.d(TAG, "【开始测试】");
        System.loadLibrary("YTCommon");
        System.loadLibrary("YTUtils");
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        int ret = YTCommonInterface.initAuth(appContext, "yttestlic20200707", 0);

        if (ret != 0) {
            Log.d(TAG, "【授权失败】");
            return;
        }
        Log.d(TAG, "【授权成功】");

        mgr = appContext.getAssets();
        System.loadLibrary("YTFaceRetrieval");
        float[] cvtTable = YTFaceRetrieval.loadConvertTable(appContext.getAssets(), "models/face-feature-v704/cvt_table_1vN_704.txt");
        mYTFaceRetrieve = new YTFaceRetrieval(cvtTable, FEAT_LENGTH);

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
    public void testRetrieveLoad() {
        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        float[] cvtTable = YTFaceRetrieval.loadConvertTable(appContext.getAssets(), "models/face-feature-v704/cvt_table_1vN_704.txt");
        int stopMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "loadConvertTable memory consumption:" +(startMemory-stopMemory)+"M");
        YTFaceRetrieval faceRetrieve = new YTFaceRetrieval(cvtTable, FEAT_LENGTH);
        int thirdMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "new YTFaceRetrieval memory consumption:" +(stopMemory-thirdMemory)+"M");
    }

    /**
     * 测试批量插入的接口
     */
    @Test
    public  void testBatchInsertFeatures() {

        int TEST_BATCH_NUM = 1000;
        float[][] features = new float[TEST_BATCH_NUM][];
        String[] name = new String[1000];
        for (int i=0;i<TEST_BATCH_NUM;i++){
            float[] feature_temp = getFeature("data/feat" + i % 10 + ".txt");
            features[i] = feature_temp;
            name[i] = "test_name_"+i;
        }
        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        int ret = mYTFaceRetrieve.insertFeatures(features, name);
        Log.i(TAG, "testBatchInsertFeatures: insert result: " + ret);
        int endMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "BatchInsertFeatures  memory consumption:" +(startMemory-endMemory)+"M");

    }

    /**
     * 测试找出最相似的人脸
     */
    @Test
    public void testRetrieve() {

        float[] feature_name = getFeature("data/featsource.txt");
        float[][] features = new float[1][512];
        System.arraycopy(feature_name, 0, features[0], 0, feature_name.length);
        int ret = mYTFaceRetrieve.insertFeatures(features, new String[]{"test_id"});

        float[] feat = getFeature("data/featsource.txt");

        int topN = 1;
        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        YTFaceRetrieval.RetrievedItem[] retrievedItems = mYTFaceRetrieve.retrieve(feat, topN, 80.0f);
        int endMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "retrieve memory consumption:" +(startMemory-endMemory)+"M");
        Assert.assertEquals(1,retrievedItems.length);
        Log.i(TAG,""+retrievedItems.length);


    }

    /**
     * 测试特征接口
     */
    @Test
    public void deleteFeature(){
        float[] feature_name = getFeature("data/featsource.txt");
        float[][] features = new float[1][512];
        System.arraycopy(feature_name, 0, features[0], 0, feature_name.length);
        int ret = mYTFaceRetrieve.insertFeatures(features, new String[]{"test_id"});
        int i = mYTFaceRetrieve.deleteFeatures(new String[]{"test_id"});
        Assert.assertEquals(0,i);
        Log.i(TAG,"=aa="+i);
    }

    @Test
    public void queryFeatureTest(){
        float[] feature_name = getFeature("data/featsource.txt");
        float[][] features = new float[1][512];
        System.arraycopy(feature_name, 0, features[0], 0, feature_name.length);
        int ret = mYTFaceRetrieve.insertFeatures(features, new String[]{"test_id"});
        int feature_number = mYTFaceRetrieve.queryFeatureNum();



        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        float[]  feature_float = mYTFaceRetrieve.queryFeature("test_id");
        int endMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "queryFeature memory consumption:" +(startMemory-endMemory)+"M");




        Assert.assertTrue(feature_number>0);
        Assert.assertNotNull(feature_float);
    }

    @Test
    public void updateFeatureTest(){
        float[] feature_name = getFeature("data/featsource.txt");
        float[] feature_name_other = getFeature("data/feat1.txt");
        float[][] features = new float[1][512];
        System.arraycopy(feature_name, 0, features[0], 0, feature_name.length);
        int ret = mYTFaceRetrieve.insertFeatures(features, new String[]{"test_id"});
        Assert.assertEquals(0,ret);//插入数据是否正确


        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        int updateFeature_id = mYTFaceRetrieve.updateFeature(feature_name_other, "test_id");
        int endMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "updateFeature memory consumption:" +(startMemory-endMemory)+"M");



        Assert.assertEquals(0,updateFeature_id);//更新数据是否正确
        float[]  feature_float = mYTFaceRetrieve.queryFeature("test_id");
        Assert.assertNotNull(feature_float);//查询返回的特征数据是否为空

    }
    @Test
    public void compareTest(){
        float[] feature_1 = getFeature("data/feat9.txt");
        float[] feature_2 = getFeature("data/feat9.txt");

        int startMemory =  ToolUtils.getMemoryInfo(appContext);
        float compare = mYTFaceRetrieve.compare(feature_1, feature_2, false);
        try {
            //这里延迟主要是为了等待内存稳定
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int endMemory =  ToolUtils.getMemoryInfo(appContext);
        Log.d(TAG, "【compare内存消耗】:" +(startMemory-endMemory)+"M");


        Assert.assertTrue(compare>=0&&compare<=1);//没有测试成功
        float compare_score = mYTFaceRetrieve.compare(feature_1, feature_2, true);
        Assert.assertTrue(compare_score>=0&&compare_score<=100);
    }

    private float[] getFeature(String filename) {
        int count = 0;
        float[] feature = new float[FEAT_LENGTH];
        try {
            InputStream is = appContext.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String ch;
            while ((ch = reader.readLine()) != null) {
                float num = Float.parseFloat(ch);
                feature[count] = num;
                count++;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feature;
    }
}
