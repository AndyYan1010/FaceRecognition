package com.botian.recognition.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.botian.recognition.R;


/**
 * @创建者 AndyYan
 * @创建时间 2018/11/7 11:07
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class PopupOpenHelper {
    private PopupWindow popupWindow;
    private Context     mContext;
    private View        mView;
    private int         mLayout;
    private View        inflateView;

    public PopupOpenHelper(Context context, View v, int layout) {
        this.mContext = context;
        this.mView = v;
        this.mLayout = layout;
    }

    public void openPopupWindow(boolean isOutsideTouchable, int position) {//true,点击旁边消失
        //防止重复按按钮
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        //设置PopupWindow的View
        inflateView = LayoutInflater.from(mContext).inflate(mLayout, null);
        popupWindow = new PopupWindow(inflateView, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //设置背景,这个没什么效果，不添加会报错
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //设置点击弹窗外隐藏自身
        popupWindow.setFocusable(isOutsideTouchable);
        popupWindow.setOutsideTouchable(isOutsideTouchable);
        //设置动画
        popupWindow.setAnimationStyle(R.style.PopupWindow);
        //        setOnPopupViewClick(popupWindow, inflateView);
        //设置位置
        popupWindow.showAtLocation(mView, position, 0, 0);//CENTER-17//BOTTOM-80//TOP-48//RIGHT-5//LEFT-3

        //设置消失监听
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //设置背景色
                setBackgroundAlpha(1.0f);
            }
        });
        //设置背景色
        setBackgroundAlpha(0.5f);
    }

    public void openPopupWindowWithView(boolean isOutsideTouchable, int positionX, int positionY) {//true,点击旁边消失
        //防止重复按按钮
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        //设置PopupWindow的View
        inflateView = LayoutInflater.from(mContext).inflate(mLayout, null);
        popupWindow = new PopupWindow(inflateView, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //设置背景,这个没什么效果，不添加会报错
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //设置点击弹窗外隐藏自身
        popupWindow.setFocusable(isOutsideTouchable);
        popupWindow.setOutsideTouchable(isOutsideTouchable);
        //设置动画
        // popupWindow.setAnimationStyle(R.style.PopupWindow);
        // setOnPopupViewClick(popupWindow, inflateView);
        //设置位置
        popupWindow.showAsDropDown(mView, positionX, positionY);

        //设置消失监听
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //设置背景色
                setBackgroundAlpha(1.0f);
            }
        });
        //设置背景色
        setBackgroundAlpha(0.5f);
    }

    //设置屏幕背景透明效果
    private void setBackgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = alpha;
        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        ((Activity) mContext).getWindow().setAttributes(lp);
    }

    public void setOnPopupViewClick(ViewClickListener viewClickListener) {
        viewClickListener.onViewListener(popupWindow, inflateView);
    }

    public void setDisListener(PopDisListner viewDisListener) {
        viewDisListener.onDisMissListener(popupWindow, inflateView);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public interface ViewClickListener {
        void onViewListener(PopupWindow popupWindow, View inflateView);

    }

    public interface PopDisListner {
        void onDisMissListener(PopupWindow popupWindow, View inflateView);
    }
}
