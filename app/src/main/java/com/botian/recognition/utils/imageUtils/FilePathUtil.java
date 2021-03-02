package com.botian.recognition.utils.imageUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Andy on 3/2/21.
 * Modification time 3/2/21.
 * Describe 描述
 */
public class FilePathUtil {
    public static final int RESULT_ORIGN = 0;
    public static final int RESULT_KB    = 1;
    public static final int RESULT_MB    = 2;


    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 传入文件路径，可以得到文件的大小信息
     *
     * @param path 文件路径
     * @return
     */
    public static String getFileSize(String path) {
        String resourceSizeMb = null;
        try {
            // 指定路径即可
            File f = new File(path);
            long fileLength = f.length();
            DecimalFormat df = new DecimalFormat("#.##");
            if (((double) fileLength / 1024) > 1000) {
                resourceSizeMb = df.format(((double) fileLength / 1024 / 1024)) + "MB";//MB
            } else {
                resourceSizeMb = df.format(((double) fileLength / 1024)) + "KB";//kB
            }

        } catch (Exception e) {
            e.printStackTrace();
            resourceSizeMb = null;
        }
        return resourceSizeMb;
    }

    /**
     * 传入文件路径，可以得到文件的大小信息
     *
     * @param path 文件路径
     * @return
     */
    public static double getFileSize(String path, int type) {//type=0 返回kb,1返回Mb
        double resourceSizeMb;
        File f = new File(path);
        long fileLength = f.length();
        if (type == RESULT_KB) {
            resourceSizeMb = (fileLength / 1024);//kB
        } else if (type == RESULT_MB) {
            resourceSizeMb = (fileLength / 1024 / 1024);//MB
        } else {
            resourceSizeMb = fileLength;
        }
        return resourceSizeMb;
    }

}
