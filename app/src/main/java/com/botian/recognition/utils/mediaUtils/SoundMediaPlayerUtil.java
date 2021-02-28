package com.botian.recognition.utils.mediaUtils;

import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.botian.recognition.MyApplication;

public class SoundMediaPlayerUtil implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private        String                TAG = "SoundUtil";
    private static SoundMediaPlayerUtil  soundUtilInstance;
    private        MediaPlayer           mediaPlayer;
    private        String                audioPath;
    private        OnCompletListener     completListener;
    private        int                   audioDuration;
    private        OnGetDurationListener getDurationListener;

    public static SoundMediaPlayerUtil getInstance() {
        if (null == soundUtilInstance) {
            synchronized (SoundMediaPlayerUtil.class) {
                if (null == soundUtilInstance)
                    soundUtilInstance = new SoundMediaPlayerUtil();
            }
        }
        return soundUtilInstance;
    }

    /**
     * 设置播放文件
     */
    public SoundMediaPlayerUtil setAudio(String audioPath) {
        //AsyncPlayer asyncPlayer = new AsyncPlayer(TAG);
        //asyncPlayer.play(MyApplication.applicationContext, Uri.parse(audioPath), false, AudioManager.STREAM_MUSIC);
        this.audioPath = audioPath;
        initMediaPlayer();
        return soundUtilInstance;
    }

    /***播放*/
    public void playAudio() {
        if (null != mediaPlayer && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void ss() {

    }

    public void pausePlay() {
        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopPlay() {
        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void initMediaPlayer() {
        if (null != audioPath) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioPath);//指定音频文件路径
                mediaPlayer.setLooping(false);//设置为循环播放
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.prepare();//初始化播放器MediaPlayer
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void playSrcAudio(int fileID) {
        AsyncPlayer asyncPlayer = new AsyncPlayer(TAG);
        String      uri         = "android.resource://" + MyApplication.applicationContext.getPackageName() + "/" + fileID;
        asyncPlayer.play(MyApplication.applicationContext, Uri.parse(uri), false, AudioManager.STREAM_MUSIC);
    }

    public void getAudioTime(String audioPath, OnGetDurationListener getDurationListener) {
        this.getDurationListener = getDurationListener;
        try {
            if (null == mediaPlayer) {
                mediaPlayer = new MediaPlayer();
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(audioPath);//指定音频文件路径
            //mediaPlayer.setLooping(false);//设置为循环播放
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();//初始化播放器MediaPlayer
        } catch (Exception e) {
        }
    }

    public SoundMediaPlayerUtil setCompleteListener(OnCompletListener completeListener) {
        this.completListener = completeListener;
        return soundUtilInstance;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (null != mp) {
            audioDuration = mp.getDuration();
            mp.start();
        }
        if (null != getDurationListener) {
            getDurationListener.outAudioTime(audioDuration);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != completListener)
            completListener.onCompletion(true);
    }

    public interface OnCompletListener {
        void onCompletion(boolean isSuccessed);
    }

    public interface OnGetDurationListener {
        void outAudioTime(int duration);
    }
}
