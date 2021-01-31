package com.botian.recognition.utils.imageUtils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitmapUtil {
    //3:4
    //

    /**
     * x 裁剪x点起始坐标（横向）
     * y 裁剪y点起始坐标（纵向）
     * width 裁剪后，新生成的bitmap的宽度
     * height 裁剪后，新生成的bitmap的高度
     */
    //public static Bitmap cutBitmap(Bitmap oriBitmap) {
    //    Bitmap finalBitmap = Bitmap.createBitmap(oriBitmap, x, y, width, height);
    //    return finalBitmap;
    //}

    public static String bitmapToBase64(Bitmap bitmap) {
        String                result = null;
        ByteArrayOutputStream baos   = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
