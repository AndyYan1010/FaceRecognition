package com.tencent.cloud.ai.fr.sdksupport;

import android.Manifest.permission;
import android.util.Log;
import androidx.test.rule.GrantPermissionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class FloatsFileHelperTest {

    private static final String TAG = "FloatsFileHelperTest";

    private static final String FILE_PATH = "/sdcard/test.floats";
    private static final int FLOATS_COUNT = 512;
    private float[] mFloats = new float[FLOATS_COUNT];

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp() throws Exception {
        Random random = new Random();
        for (int i = 0; i < FLOATS_COUNT; i++) {
            float f = random.nextFloat();
            mFloats[i] = f;
        }
    }

    @After
    public void tearDown() throws Exception {
        new File(FILE_PATH).delete();
    }

    @Test
    public void writeAndReadFloatsFile() {
        //写
        long start = System.nanoTime();
        FloatsFileHelper.writeFloatsToFile(mFloats, FILE_PATH);
        long cost = System.nanoTime() - start;
        Log.d(TAG, "write cost " + cost * 1f / 1000000 + "ms");

        //读
        start = System.nanoTime();
        float[] floats = FloatsFileHelper.readFloatsFromFile(FILE_PATH, FLOATS_COUNT);
        cost = System.nanoTime() - start;
        Log.d(TAG, "read cost " + cost * 1f / 1000000 + "ms");

        //校验
        for (int i = 0; i < FLOATS_COUNT; i++) {
            assertEquals(floats[i], mFloats[i], 0);
        }
    }
}
