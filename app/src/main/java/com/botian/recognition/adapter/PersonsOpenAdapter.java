package com.botian.recognition.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.botian.recognition.R;
import com.botian.recognition.activity.CameraPhotoActivity;
import com.botian.recognition.bean.OpenRecordBean;
import com.botian.recognition.utils.TimeUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class PersonsOpenAdapter extends BaseQuickAdapter<OpenRecordBean.ListBean, BaseViewHolder> {
    private Context mContext;
    private int     REQUEST_CODE_ADD_PERSON = 10002;

    public PersonsOpenAdapter(Context context, int layoutResId, List<OpenRecordBean.ListBean> list) {
        super(layoutResId, list);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, OpenRecordBean.ListBean item) {
        helper.setText(R.id.tv_line, (null == item.getWorkno() || "".equals(item.getWorkno())) ? "--" : item.getWorkno());
        helper.setText(R.id.tv_time, TimeUtil.subStrTime2Sec(item.getCreatetime()));
        helper.setText(R.id.tv_name, null == item.getUsers() ? "" : item.getUsers());

        View tv_addPerson = helper.getView(R.id.tv_addPerson);
        //View img_add = helper.getView(R.id.img_add);
        tv_addPerson.setOnClickListener(v -> {
            //增加开机人员
            addPersonForOpenMachine(helper.getPosition());
        });
    }

    /****增加开机人员
     * @param position*/
    private void addPersonForOpenMachine(int position) {
        //跳转人脸识别界面
        Intent intent = new Intent(mContext, CameraPhotoActivity.class);
        intent.putExtra("ftype", 2);
        intent.putExtra("itemIndex", position);
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE_ADD_PERSON);
    }
}
