package com.botian.recognition.utils.imageUtils;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

import com.botian.recognition.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by Andy on 3/2/21.
 * Modification time 3/2/21.
 * Describe 描述
 */
public class GlideLoaderUtil {
    private static RequestOptions requestOptions;
    private static int default_hoder = R.drawable.logo;
    private static int default_error = R.drawable.logo;
    //圆形占位和错误图
    private static int round_default_hoder = R.drawable.logo;
    private static int round_default_error = R.drawable.logo;
    //头像占位和错误图
    private static int head_default_hoder = R.drawable.logo;
    private static int head_default_error = R.drawable.logo;

    /**
     * 一般图片加载
     *
     * @param context   上下文
     * @param url       图片链接
     * @param imageview 组件
     */
    public static void showImageView(Context context, Object url, ImageView imageview) {
        if (null == context) {
            return;
        }
        requestOptions = new RequestOptions()
                .placeholder(default_hoder)// 占位图
                .error(default_error);// 错误图
        Glide.with(context)
                .load(url)// 加载图片
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageview);
    }

    /**
     * 加载一般圆形图
     */
    public static void loadRoundImg(Context context, Object url, ImageView imgeview) {
        if (null != context) {
            requestOptions = new RequestOptions()
                    .placeholder(round_default_hoder)// 图片加载出来前，显示的图片
                    .error(round_default_error)// 设置错误图片
                    .centerCrop()
                    .circleCrop();
            Glide.with(context)
                    .load(url)// 加载图片
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgeview);
        }
    }

    public static void loadRoundImg(Context context, Object url, int beforeID, int errorID, ImageView imgeview) {
        if (null != context) {
            requestOptions = new RequestOptions()
                    .placeholder(beforeID)// 图片加载出来前，显示的图片
                    .error(errorID)// 设置错误图片
                    .circleCrop();
            Glide.with(context)
                    .load(url)// 加载图片
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgeview);
        }
    }

    /**
     * 加载圆形图,(默认头像填充)
     */
    public static void loadRoundHaedImg(Context context, Object url, ImageView imgeview) {
        if (null != context) {
            requestOptions = new RequestOptions()
                    .placeholder(head_default_hoder)// 图片加载出来前，显示的图片
                    .error(head_default_error)// 设置错误图片
                    .centerCrop()
                    .circleCrop();
            Glide.with(context)
                    .load(url)// 加载图片
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgeview);
        }
    }

    /**
     * 加载圆形图
     */
    public static void showRoundImgWithIcon(Context context, Object url, int beforeID, int errorID, ImageView imgeview) {
        if (null != context) {
            requestOptions = new RequestOptions()
                    .placeholder(beforeID)// 图片加载出来前，显示的图片
                    .error(errorID)// 设置错误图片
                    .centerCrop()
                    .circleCrop();
            Glide.with(context)
                    .load(url)// 加载图片
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgeview);
        }
    }

    /**
     * 加载手机本地图片
     */
    public static void loadIDImgNoCache(Context context, Object url, ImageView imageview) {
        if (null != context) {
            requestOptions = new RequestOptions()
                    .placeholder(default_hoder)// 图片加载出来前，显示的图片
                    .error(default_error)// 设置错误图片
                    .skipMemoryCache(true)// 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE);// 不使用磁盘缓存
            Glide.with(context)
                    .load(url)// 加载图片
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageview);
        }
    }

    /**
     * 获取Glide造成的缓存大小
     *
     * @return CacheSize
     */
    public static String getCacheSize(Context context) {
        try {
            return getFormatSize(getFolderSize(
                    new File(context.getCacheDir() + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     */
    private static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 清除图片磁盘缓存
     */
    public static void clearImageDiskCache(final Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(context).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片内存缓存
     */
    public static void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片所有缓存
     */
    public static void clearImageAllCache(Context context) {
        clearImageDiskCache(context);
        clearImageMemoryCache(context);
        String ImageExternalCatchDir = context.getExternalCacheDir() + ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR;
        deleteFolderFile(ImageExternalCatchDir, true);
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath       filePath
     * @param deleteThisPath deleteThisPath
     */
    private static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 格式化单位
     *
     * @param size size
     * @return size
     */
    private static String getFormatSize(double size) {

        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }
}
