package com.botian.recognition.utils.imageUtils;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class ShapeUtil {

    public static void changeViewBackground(View view, String backgroundColor) {
        GradientDrawable view_ground = (GradientDrawable) view.getBackground(); //获取控件的背景色
        view_ground.setColor(Color.parseColor(backgroundColor));//设置背景色
    }
}
