package com.botian.recognition.utils.fileUtils;

import java.io.File;

public class FileUtil {

    /***
     * 创建文件夹
     * */
    public static void existOrCreateFolder(String filePath) {
        File folder = new File(filePath);
        if (!folder.exists() && !folder.isDirectory()) {
            //创建文件夹
            folder.mkdirs();
        }
        //文件夹已存在
    }

    /***
     * 删除文件
     * */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
