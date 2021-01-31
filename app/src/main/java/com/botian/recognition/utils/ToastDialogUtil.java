package com.botian.recognition.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.botian.recognition.R;


public class ToastDialogUtil {
    private static ToastDialogUtil     dialogUtil;
    private        MyAlertDialogHelper dialogHelper;
    public static  int                 NORMOL_STYLE           = 0;
    private        View                inflateView;
    private        Context             context;

    private ToastDialogUtil() {

    }

    public static ToastDialogUtil getInstance() {
        if (dialogUtil == null) {
            synchronized (ToastDialogUtil.class) {
                if (dialogUtil == null)
                    dialogUtil = new ToastDialogUtil();
            }
        }
        return dialogUtil;
    }

    private void init() {
        dialogHelper = new MyAlertDialogHelper();
    }

    public ToastDialogUtil setContext(Context context) {
        this.context = context;
        init();
        return dialogUtil;
    }

    /**
     * 设置显示弹窗的风格
     */
    public ToastDialogUtil useStyleType(int type) {
        if (type == NORMOL_STYLE) {
            dialogUtil.setInflateView(R.layout.toast_dialog_normol_view);
        }
        return dialogUtil;
    }

    /**
     * 设置自定义显示的view,有默认格式
     */
    private ToastDialogUtil setInflateView(int layout) {
        if (context != null) {
            inflateView = View.inflate(context, layout, null);
        }
        dialogHelper.setDIYView(context, inflateView);
        return dialogUtil;
    }

    /**
     * 获取显示的整个view
     */
    public View getInflateView() {
        return inflateView;
    }

    /**
     * 是否显示取消按钮，以及显示内容
     */
    public ToastDialogUtil showCancelView(boolean needShow, String cont, ViewOnclickListener viewOnclickListener) {
        TextView tv_cancel = getInflateView().findViewById(R.id.tv_cancel);
        if (!needShow) {
            tv_cancel.setVisibility(View.GONE);
        } else {
            tv_cancel.setVisibility(View.VISIBLE);
            if (null != cont || !"".equals(cont)) {
                setTextViewCont(tv_cancel, cont);
            }
            tv_cancel.setOnClickListener(v -> {
                //取消点击事件
                viewOnclickListener.onclick(dialogUtil, tv_cancel);
            });
        }
        return dialogUtil;
    }

    /**
     * 是否显示确认按钮，以及显示内容
     */
    public ToastDialogUtil showSureView(boolean needShow, String cont, ViewOnclickListener viewOnclickListener) {
        TextView tv_sure = inflateView.findViewById(R.id.tv_sure);
        if (!needShow) {
            tv_sure.setVisibility(View.GONE);
        } else {
            tv_sure.setVisibility(View.VISIBLE);
            setTextViewCont(tv_sure, cont);
            tv_sure.setOnClickListener(v -> {
                viewOnclickListener.onclick(dialogUtil, tv_sure);
            });
        }
        return dialogUtil;
    }

    /**
     * 设置标题
     */
    public ToastDialogUtil setTitle(String title) {
        setTextViewCont(inflateView.findViewById(R.id.tv_title), title);
        return dialogUtil;
    }

    /**
     * 设置提示内容
     */
    public ToastDialogUtil setCont(String cont) {
        setTextViewCont(inflateView.findViewById(R.id.tv_cont), cont);
        return dialogUtil;
    }

    /**
     * 显示弹窗
     */
    public void show() {
        if (null != dialogHelper) {
            dialogHelper.show();
        }
    }

    /**
     * 关闭弹窗
     */
    public void dismiss() {
        if (null != dialogHelper) {
            dialogHelper.disMiss();
        }
    }

    /**
     * 通用tv设置内容
     */
    private void setTextViewCont(TextView tv, String cont) {
        if (null != cont) {
            tv.setText(cont);
        } else {
            tv.setText("");
        }
    }

    public interface ViewOnclickListener {
        void onclick(ToastDialogUtil dialogUtil, View tv);
    }
}
