package com.botian.recognition.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.botian.recognition.BaseActivity;
import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.adapter.ReportListAdapter;
import com.botian.recognition.bean.ReportListInfo;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.imageUtils.ShapeUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

public class ReportListActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.et_cont)
    EditText           et_cont;
    @BindView(R.id.tv_search)
    TextView           tv_search;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swiperefresh;
    @BindView(R.id.recy_report)
    RecyclerView       recy_report;

    private List<ReportListInfo.ListBean> mReportList;
    private ReportListAdapter             reportAdapter;
    private int                           pageNum               = 1;
    private int                           REQUEST_CODE_GET_FACE = 10001;

    @Override
    protected int setLayout() {
        return R.layout.act_report_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setTextCont(R.id.tv_DevID, MyApplication.devID);
        ShapeUtil.changeViewBackground(tv_search, "#1966FF");
    }

    @Override
    protected void initData() {
        //初始化人员列表
        initReportList();
        //刷新控件
        setSwipRefresh();
        //获取初始数据
        getFirstData();
    }

    @Override
    protected void initListener() {
        tv_search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search:
                //模糊搜索
                fuzzySearch();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_GET_FACE == requestCode) {
            getFirstData();
        }
    }

    /***模糊搜索*/
    private void fuzzySearch() {
        String searchCont = String.valueOf(et_cont.getText());
        mReportList.clear();
        reportAdapter.notifyDataSetChanged();
        getReportList(searchCont);
    }

    /***获取汇报单列表*/
    private void getReportList(String workNo) {
        swiperefresh.setRefreshing(true);
        RequestParamsFM params = new RequestParamsFM();
        params.put("number", 10);
        params.put("page_num", pageNum);
        params.put("fbiller", MyApplication.workUserName);//从bmlist里取
        params.put("gongxu", MyApplication.workGongXu);
        if (null != workNo && !"".equals(workNo)) {
            params.put("workno", workNo);
        }
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.SERHUIBAOWORK, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                swiperefresh.setRefreshing(false);
                reportAdapter.loadMoreEnd();
                ToastUtils.showToast("网络错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                swiperefresh.setRefreshing(false);
                reportAdapter.loadMoreEnd();
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    return;
                }
                Gson           gson           = new Gson();
                ReportListInfo reportListInfo = gson.fromJson(resbody, ReportListInfo.class);
                ToastUtils.showToast(reportListInfo.getMessage());
                if (!"1".equals(reportListInfo.getCode())) {
                    return;
                }
                mReportList.addAll(reportListInfo.getList());
                reportAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getFirstData() {
        mReportList.clear();
        reportAdapter.notifyDataSetChanged();
        pageNum = 1;
        //获取汇报单列表
        getReportList(null);
    }

    /***初始化汇报单列表*/
    private void initReportList() {
        mReportList = new ArrayList();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recy_report.setLayoutManager(linearLayoutManager);
        reportAdapter = new ReportListAdapter(this, R.layout.recy_report, mReportList);
        recy_report.setAdapter(reportAdapter);
        reportAdapter.setOnLoadMoreListener(() -> {
            pageNum++;
            getReportList(null);
        });
        reportAdapter.setOnItemClickListener((adapter, view, position) -> {
            //跳转web页面
            //Intent intent = new Intent(getSelfContext(), WebUrlActivity.class);
            //intent.putExtra("urlParams", "/pages/package/package?id=" + mReportList.get(position).getId());
            //startActivity(intent);
        });
    }

    private void setSwipRefresh() {
        swiperefresh.setColorSchemeColors(getResources().getColor(R.color.blue_icon), getResources().getColor(R.color.yellow_40), getResources().getColor(R.color.red_160));
        swiperefresh.setOnRefreshListener(() -> {
            //获取初始数据
            getFirstData();
        });
    }
}
