package com.botian.recognition.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.botian.recognition.BaseActivity;
import com.botian.recognition.R;
import com.botian.recognition.utils.ToastUtils;

import butterknife.BindView;

public class DeviceActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.tv_open)
    TextView tv_open;
    @BindView(R.id.tv_close)
    TextView tv_close;


    @Override
    protected int setLayout() {
        return R.layout.act_check_device;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {
        //获取SensorManager对象  
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //获取精度  
                float acc = event.accuracy;
                //获取光线强度  
                float lux = event.values[0];
                System.out.println("光线强度" + lux);
                ToastUtils.showToast("光线强度" + lux);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sm.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void initListener() {
        tv_open.setOnClickListener(this);
        tv_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open:
                //打开灯
                openLight();
                break;
            case R.id.tv_close:
                closeLight();
                break;
        }
    }

    /****关灯*/
    private void closeLight() {
    }

    /***打开灯*/
    private void openLight() {
    }
}
