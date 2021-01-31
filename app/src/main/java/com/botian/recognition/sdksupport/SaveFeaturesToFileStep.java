package com.botian.recognition.sdksupport;

import android.util.Log;

import com.tencent.cloud.ai.fr.pipeline.AbsStep;
import com.tencent.cloud.ai.fr.sdksupport.FloatsFileHelper;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class SaveFeaturesToFileStep extends AbsStep<StuffBox> {

    private static final String TAG = "SaveFeaturesToFileStep";

    public static final String FACE_LIB_PATH = "/sdcard/FaceLibrary/";

    private StuffId<Collection<FaceForReg>> mInFacesId;
    private InputProvider                   mInputProvider;

    public SaveFeaturesToFileStep(StuffId<Collection<FaceForReg>> inFacesId) {
        mInFacesId = inFacesId;
    }

    public SaveFeaturesToFileStep(InputProvider inputProvider) {
        mInputProvider = inputProvider;
    }

    @Override
    protected boolean onProcess(StuffBox stuffBox) {
        Collection<FaceForReg> faceForRegs = Collections.emptyList();
        if (mInFacesId != null) {
            faceForRegs = stuffBox.find(mInFacesId);
        } else if (mInputProvider != null) {
            faceForRegs = mInputProvider.onGetInput(stuffBox);
        }
        prepareDir(FACE_LIB_PATH);
        for (FaceForReg faceForReg : faceForRegs) {
            String filePath = new File(FACE_LIB_PATH + faceForReg.name + ".feature").getAbsolutePath();
            FloatsFileHelper.writeFloatsToFile(faceForReg.feature, filePath);
        }
        //保存人脸特征值后，返回信息，给进一步操作
        mInputProvider.onFileSavedResult(faceForRegs);
        return true;
    }

    private static void prepareDir(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            boolean succeeded = dirFile.mkdirs();
            if (!succeeded) {
                Log.w(TAG, "mkdirs failed: " + dir);
            }
        }
    }

    public interface InputProvider {
        Collection<FaceForReg> onGetInput(StuffBox stuffBox);

        void onFileSavedResult(Collection<FaceForReg> faceForRegs);
    }
}
