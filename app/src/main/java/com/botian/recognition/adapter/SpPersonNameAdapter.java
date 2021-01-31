package com.botian.recognition.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.botian.recognition.R;
import com.botian.recognition.bean.PersonListResultBean;

import java.util.List;


/**
 * @创建者 AndyYan
 * @创建时间 2018/8/27 10:07
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class SpPersonNameAdapter extends BaseAdapter {
    private Context                             mContext;
    private List<PersonListResultBean.ListBean> mList;

    public SpPersonNameAdapter(Context context, List<PersonListResultBean.ListBean> list) {
        this.mContext = context;
        this.mList    = list;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        MyViewholder viewholder;
        if (null == view) {
            viewholder         = new MyViewholder();
            view               = View.inflate(mContext, R.layout.adpter_sp_person, null);
            viewholder.tv_name = view.findViewById(R.id.tv_name);
            view.setTag(viewholder);
        } else {
            viewholder = (MyViewholder) view.getTag();
        }
        viewholder.tv_name.setText(mList.get(i).getFname());
        return view;
    }

    private class MyViewholder {
        TextView tv_name;
    }
}
