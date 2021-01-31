package com.botian.recognition;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {
    private Unbinder unBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //flag为-1说明程序被杀掉
        if (MyApplication.flag == -1) {
            protectApp();
        }
        MyApplication.listActivity.add(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//保持竖屏
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);//取消键盘自动弹出
        setContentView(setLayout());
        //注册ButterKnife
        unBinder = ButterKnife.bind(this);
        initView(savedInstanceState);
        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除注解
        if (unBinder != null) {
            unBinder.unbind();
        }
        MyApplication.listActivity.remove(this);
    }

    protected void protectApp() {
        Intent intent = new Intent(this, MainActivity.class);
        //清空栈里firstActivity之上的所有activty
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        MyApplication.flag = 0;
    }

    public void setTextCont(int tvID, String cont) {
        if (null != findViewById(tvID)) ((TextView) findViewById(tvID)).setText(cont);
    }

    public void setTextCont(TextView tv, String cont) {
        if (null != tv) tv.setText(cont);
    }

    public Context getSelfContext() {
        return this;
    }

    protected abstract int setLayout();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void initData();

    protected abstract void initListener();
}
