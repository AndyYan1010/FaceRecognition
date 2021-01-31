package com.tencent.cloud.ai.fr;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public class ToolUtils {
    public static int getMemoryInfo(Context context) {
        //获取运行内存的信息
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(info);
        return (int) (info.availMem / (1024 * 1024));
    }
    public static byte[] bitmap2RGB(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);

        byte[] rgba = buffer.array();
        int count = rgba.length / 4;
        byte[] pixels = new byte[count * 3];

        for (int i = 0; i < count; i++) {
            pixels[i * 3] = rgba[i * 4];
            pixels[i * 3 + 1] = rgba[i * 4 + 1];
            pixels[i * 3 + 2] = rgba[i * 4 + 2];
        }

        return pixels;
    }
}
