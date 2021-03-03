package com.botian.recognition.utils;

public class CommonUtil {

    /***将float数组转字符串，以逗号隔开*/
    public static String getFloatStr(float[] floats) {
        String value = "";
        if (floats.length == 1) {
            value = String.valueOf(floats[0]);
        } else {
            for (int i = 0; i < floats.length; i++) {
                if (i < floats.length - 1) {
                    value = value + floats[i] + ",";
                } else {
                    value = value + floats[i];
                }
            }
        }
        return value;
    }

    /***将float数组字符串转成float数组*/
    public static float[] getFloatArray(String floatStr) {
        String[] split  = floatStr.split(",");
        float[]  floats = new float[split.length];
        try {
            for (int i = 0; i < split.length; i++) {
                floats[i] = Float.parseFloat(split[i]);
            }
        } catch (Exception e) {
            return new float[0];
        }
        return floats;
    }
}
