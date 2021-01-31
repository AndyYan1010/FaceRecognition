package com.botian.recognition.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

/**
 * @创建者 AndyYan
 * @创建时间 2018/11/9 13:34
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class MyAlertDialogHelper {
    private AlertDialog         alertDialog;
    private AlertDialog.Builder builder;

    public void setDataNoView(Context context, String title, String message) {
        builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(title);
        builder.setMessage(message);
    }

    public void setDIYView(Context context, View view) {
        builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        builder.setView(view);
    }

    public void setDialogClicker(String positive, final DialogClickListener dialogClickListener) {
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClickListener.onPositive();
                dialog.cancel();
            }
        });
    }

    public void setDialogClicker(String positive, String Negative, final DialogClickListener dialogClickListener) {
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClickListener.onPositive();
                dialog.cancel();
            }
        }).setNegativeButton(Negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClickListener.onNegative();
                dialog.cancel();
            }
        });
    }

    public void show() {
        if (null == alertDialog) {
            alertDialog = builder.create();
        }
        alertDialog.show();
    }

    public void hide() {
        if (null != alertDialog)
            alertDialog.hide();
    }

    public void disMiss() {
        if (null != alertDialog)
            alertDialog.dismiss();
    }

    public interface DialogClickListener {
        void onPositive();

        void onNegative();
    }
}
