package com.botian.recognition.configure;

import android.os.Environment;

public class LocalSetting {
    //拍摄的图片
    public static String CAMERA_PHOTO_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRecognition/keepPic/";
    //存储的语音
    public static String AUDIO_PATH        = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRecognition/audio/";

}
