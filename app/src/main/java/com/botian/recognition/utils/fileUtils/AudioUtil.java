package com.botian.recognition.utils.fileUtils;

import com.botian.recognition.utils.netUtils.ThreadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

import static com.botian.recognition.configure.LocalSetting.AUDIO_PATH;

public class AudioUtil {
    private static AudioUtil          audioUtilInstance;
    private        ChangeFileListener changeListener;

    public static AudioUtil getInstance() {
        if (null == audioUtilInstance) {
            synchronized (AudioUtil.class) {
                if (null == audioUtilInstance)
                    audioUtilInstance = new AudioUtil();
            }
        }
        return audioUtilInstance;
    }

    public AudioUtil setChangeListener(ChangeFileListener changeListener) {
        this.changeListener = changeListener;
        return audioUtilInstance;
    }

    /**
     * 将文件转成base64
     */
    public String encodeFile2Base64(String path) {
        File   file   = new File(path);
        String encode = null;
        try {
            FileInputStream inputFile = new FileInputStream(file);
            byte[]          buffer    = new byte[(int) file.length()];
            inputFile.read(buffer);
            inputFile.close();
            encode = new BASE64Encoder().encode(buffer);
        } catch (Exception e) {
        }
        return encode;
    }

    /**
     * 将base64字符解码保存文件
     *
     * @param base64Code
     * @param targetFilePath
     * @throws Exception
     */
    public void decoderBase642File(String base64Code, String targetFilePath) {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                if (null == base64Code || "".equals(base64Code)) {
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            changeListener.onFailed();
                        }
                    });
                }
                try {
                    FileUtil.existOrCreateFolder(AUDIO_PATH);
                    byte[]           buffer = new BASE64Decoder().decodeBuffer(base64Code);
                    FileOutputStream out    = new FileOutputStream(targetFilePath);
                    out.write(buffer);
                    out.close();
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            changeListener.onSuccess();
                        }
                    });
                } catch (Exception e) {
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            changeListener.onFailed();
                        }
                    });
                }
            }
        });
    }

    public interface ChangeFileListener {
        void onSuccess();

        void onFailed();
    }
}
