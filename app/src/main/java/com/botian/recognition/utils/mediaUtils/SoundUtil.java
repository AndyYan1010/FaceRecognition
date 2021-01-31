package com.botian.recognition.utils.mediaUtils;

import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;

import com.botian.recognition.MyApplication;

public class SoundUtil {
    private        String    TAG = "SoundUtil";
    private static SoundUtil soundUtilInstance;

    public static SoundUtil getInstance() {
        if (null == soundUtilInstance) {
            synchronized (SoundUtil.class) {
                if (null == soundUtilInstance)
                    soundUtilInstance = new SoundUtil();
            }
        }
        return soundUtilInstance;
    }

    /**
     * 播放音频
     */
    public void playAudio(String audioPath) {
        AsyncPlayer asyncPlayer = new AsyncPlayer(TAG);
        asyncPlayer.play(MyApplication.applicationContext, Uri.parse(audioPath), false, AudioManager.STREAM_MUSIC);
    }

    public void playSrcAudio(int fileID) {
        AsyncPlayer asyncPlayer = new AsyncPlayer(TAG);
        String      uri         = "android.resource://" + MyApplication.applicationContext.getPackageName() + "/" + fileID;
        asyncPlayer.play(MyApplication.applicationContext, Uri.parse(uri), false, AudioManager.STREAM_MUSIC);
    }
}
