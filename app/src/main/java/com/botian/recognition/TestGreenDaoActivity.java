package com.botian.recognition;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.botian.recognition.adapter.LiensAdapter;
import com.botian.recognition.bean.CheckFaceHistory;
import com.botian.recognition.bean.ClassLinesBean;
import com.btface.greendaodemo.DaoSession;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TestGreenDaoActivity extends AppCompatActivity {
    @BindView(R.id.rec_person)
    RecyclerView rec_person;
    @BindView(R.id.tv_write)
    TextView     tv_write;
    @BindView(R.id.tv_read)
    TextView     tv_read;
    @BindView(R.id.tv_clear)
    TextView     tv_clear;

    private List<ClassLinesBean.ListBean> mLinesList;
    private LiensAdapter                  liensAdapter;
    private Unbinder                      unBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list);
        MyApplication.flag = 0;
        MyApplication.listActivity.add(this);
        unBinder = ButterKnife.bind(this);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mLinesList = new ArrayList<>();
        rec_person.setLayoutManager(new LinearLayoutManager(this));
        liensAdapter = new LiensAdapter(this, mLinesList);
        rec_person.setAdapter(liensAdapter);
    }

    protected void initData() {

    }

    protected void initListener() {
        tv_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DaoSession       daoSession       = MyApplication.getDaoSession();
                CheckFaceHistory checkFaceHistory = new CheckFaceHistory();
                checkFaceHistory.setUserID("张三" + System.currentTimeMillis());
                daoSession.insert(checkFaceHistory);
            }
        });
        tv_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CheckFaceHistory> checkFaceHistories = MyApplication.getDaoSession().loadAll(CheckFaceHistory.class);
                if (checkFaceHistories == null || checkFaceHistories.size() == 0)
                    return;
                for (CheckFaceHistory checkFaceHistory : checkFaceHistories) {
                    ClassLinesBean.ListBean bean = new ClassLinesBean.ListBean();
                    bean.setDepartment(checkFaceHistory.getUserID());
                    mLinesList.add(bean);
                }
                liensAdapter.notifyDataSetChanged();
            }
        });
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinesList.clear();
                liensAdapter.notifyDataSetChanged();
                MyApplication.getDaoSession().deleteAll(CheckFaceHistory.class);
            }
        });
    }
}
