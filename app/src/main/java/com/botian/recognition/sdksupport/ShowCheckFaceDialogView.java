package com.botian.recognition.sdksupport;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.botian.recognition.R;
import com.botian.recognition.activity.RegWithAndroidCameraActivity;
import com.botian.recognition.adapter.SpPersonNameAdapter;
import com.botian.recognition.bean.PersonListResultBean;

import java.util.ArrayList;
import java.util.List;

public class ShowCheckFaceDialogView {
    private MyDialog                            dialog;
    private View                                view;
    private Context                             context;
    private String                              selectName;
    private String                              selectID;
    private Spinner                             spinnerPerson;
    private List<PersonListResultBean.ListBean> mPersonList;
    private SpPersonNameAdapter                 mSpAdapter;
    private boolean                             showStatus;

    //public static ShowCheckFaceDialogView getInstance() {
    //    if (null == faceDialogView) {
    //        synchronized (ShowCheckFaceDialogView.class) {
    //            if (null == faceDialogView) {
    //                faceDialogView = new ShowCheckFaceDialogView();
    //            }
    //        }
    //    }
    //    return faceDialogView;
    //}

    public void initView(Context context) {
        this.context = context;
        ((RegWithAndroidCameraActivity) context).setDialogStatue(true);
        ((RegWithAndroidCameraActivity) context).setSelectButton(-1);
        dialog = new MyDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        view = LayoutInflater.from(context).inflate(R.layout.confirm_reg_face, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinnerPerson = view.findViewById(R.id.spinner_list);
        initDialogView();
        dialog.setContentView(view);
    }

    private void initDialogView() {
        mPersonList = new ArrayList<>();
        mSpAdapter  = new SpPersonNameAdapter(context, mPersonList);
        spinnerPerson.setAdapter(mSpAdapter);
        spinnerPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectID   = mPersonList.get(position).getId();
                selectName = mPersonList.get(position).getFname();
                ((RegWithAndroidCameraActivity) context).setSelectName(selectName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setViewListener();
    }

    private void setViewListener() {
        view.findViewById(R.id.reg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(selectName)) {
                    Toast.makeText(context, "姓名不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                    ((RegWithAndroidCameraActivity) context).setSelectButton(1);
                    dialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                ((RegWithAndroidCameraActivity) context).setSelectButton(2);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegWithAndroidCameraActivity) context).setDialogStatue(false);
                ((RegWithAndroidCameraActivity) context).setSelectButton(0);
                dialog.dismiss();
            }
        });
    }

    public void setViewCont(Bitmap face, List<PersonListResultBean.ListBean> personList) {
        ((ImageView) view.findViewById(R.id.img)).setImageBitmap(face);
        if (null == mPersonList) {
            mPersonList = new ArrayList<>();
        } else {
            mPersonList.clear();
        }
        mPersonList.addAll(personList);
        mSpAdapter.notifyDataSetChanged();
    }

    public void showDialog() {
        if (null != dialog)
            dialog.show();
    }

    public String getSelectName() {
        return selectName;
    }

    private static class MyDialog extends Dialog {
        MyDialog(@NonNull Context context) {
            super(context);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        public void show() {
            // 设置背景变暗程度, 构造方法时候设置无效
            getWindow().getAttributes().dimAmount = 0.8f;
            super.show();
        }
    }
}
