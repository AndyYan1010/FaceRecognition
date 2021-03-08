package com.botian.recognition.utils.fileUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.botian.recognition.utils.ToastUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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

    /***判断文件是否存在*/
    public static boolean existFolder(String filePath) {
        if (null == filePath || "".equals(filePath))
            return false;
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return true;
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


    /***根据uri获取文件本地路径*/
    public static String getFilePathFromUri(Context context, Uri uri) {
        //专属 Matisse  uri 路径解析
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String realPath = null;
        if (scheme == null)
            realPath = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            realPath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        realPath = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        if (TextUtils.isEmpty(realPath)) {
            if (uri != null) {
                String uriString = uri.toString();
                int index = uriString.lastIndexOf("/");
                String imageName = uriString.substring(index);
                File storageDir;

                storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File file = new File(storageDir, imageName);
                if (file.exists()) {
                    realPath = file.getAbsolutePath();
                } else {
                    storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File file1 = new File(storageDir, imageName);
                    realPath = file1.getAbsolutePath();
                }
            }
        }
        return realPath;
    }

    /**
     * 保存图片并发送广播通知数据库刷新，只有这样才能在相册里面看到这张照片
     *
     * @param bitmap
     */
    public static void saveBitmapFile(Activity activity, Bitmap bitmap, String filePath) {
        //将要保存图片的路径
        existOrCreateFolder(filePath);
        File file = new File(filePath + "temp" + System.currentTimeMillis() + ".jpg");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //发送广播给系统，刷新数据库
        Uri uri = Uri.fromFile(file);
        System.out.println("imageUri------>" + uri);
        //imageUri------>file:///storage/emulated/0/MyAndroidBase/mybase/1574394370534.jpg
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        activity.sendBroadcast(localIntent);
    }
}
