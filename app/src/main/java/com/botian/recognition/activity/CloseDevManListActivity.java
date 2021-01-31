package com.botian.recognition.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.botian.recognition.BaseActivity;
import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.adapter.PersonsCloseAdapter;
import com.botian.recognition.bean.CloseRecordBean;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

public class CloseDevManListActivity extends BaseActivity {
    @BindView(R.id.recy_person)
    RecyclerView       recy_person;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swiperefresh;

    private List<CloseRecordBean.ListBean> mPersonsList;
    private PersonsCloseAdapter            personsAdapter;
    private int                            pageNum                   = 1;
    private int                            REQUEST_CODE_GET_FACE     = 10001;
    private int                            RESULT_CODE_CLOSE_MACHINE = 10013;

    @Override
    protected int setLayout() {
        return R.layout.act_close_man_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {
        setTextCont(R.id.tv_DevID, MyApplication.devID);
        //初始化人员列表
        initPersonList();
        //刷新控件
        setSwipRefresh();
        //获取初始数据
        getFirstData();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_GET_FACE == requestCode) {
            //人脸认证成功，刷新数据
            getFirstData();
        }
    }

    private void getFirstData() {
        mPersonsList.clear();
        pageNum = 1;
        //获取开机记录列表
        getCloseHistoryList();
    }

    /****获取开机记录列表*/
    private void getCloseHistoryList() {
        swiperefresh.setRefreshing(true);
        RequestParamsFM params = new RequestParamsFM();
        //params.put("id", "869066035238777");
        params.put("id", MyApplication.devID);
        params.put("page_num", pageNum);
        params.put("number", 10);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.GJRECORDLIST, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                swiperefresh.setRefreshing(false);
                personsAdapter.loadMoreEnd();
                ToastUtils.showToast("网络错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                swiperefresh.setRefreshing(false);
                personsAdapter.loadMoreEnd();
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    return;
                }
                Gson            gson            = new Gson();
                CloseRecordBean closeRecordBean = gson.fromJson(resbody, CloseRecordBean.class);
                ToastUtils.showToast(closeRecordBean.getMessage());
                if (!"1".equals(closeRecordBean.getCode())) {
                    return;
                }
                mPersonsList.addAll(closeRecordBean.getList());
                personsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setSwipRefresh() {
        swiperefresh.setColorSchemeColors(getResources().getColor(R.color.blue_icon), getResources().getColor(R.color.yellow_40), getResources().getColor(R.color.red_160));
        swiperefresh.setOnRefreshListener(() -> {
            //获取初始数据
            getFirstData();
        });
    }

    /***初始化人员列表*/
    private void initPersonList() {
        mPersonsList = new ArrayList();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recy_person.setLayoutManager(linearLayoutManager);
        personsAdapter = new PersonsCloseAdapter(this, R.layout.recy_persons_close, mPersonsList);
        recy_person.setAdapter(personsAdapter);
        personsAdapter.setOnLoadMoreListener(() -> {
            pageNum++;
            getCloseHistoryList();
        });
    }
}
