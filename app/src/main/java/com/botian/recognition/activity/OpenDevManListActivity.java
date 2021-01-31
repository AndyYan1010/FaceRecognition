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
import com.botian.recognition.adapter.PersonsOpenAdapter;
import com.botian.recognition.bean.OpenRecordBean;
import com.botian.recognition.bean.UpResultBean;
import com.botian.recognition.configure.LocalSetting;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.fileUtils.AudioUtil;
import com.botian.recognition.utils.fileUtils.FileUtil;
import com.botian.recognition.utils.mediaUtils.SoundUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

public class OpenDevManListActivity extends BaseActivity {
    @BindView(R.id.recy_person)
    RecyclerView       recy_person;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swiperefresh;

    private List<OpenRecordBean.ListBean> mPersonsList;
    private PersonsOpenAdapter            personsAdapter;
    private int                           pageNum                  = 1;
    private int                           REQUEST_CODE_ADD_PERSON  = 10002;
    private int                           RESULT_CODE_OPEN_MACHINE = 10012;

    @Override
    protected int setLayout() {
        return R.layout.act_open_man_list;
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
        if (REQUEST_CODE_ADD_PERSON == requestCode && RESULT_CODE_OPEN_MACHINE == resultCode) {
            //人脸认证成功，添加人员信息到列表
            if (null != data) {
                int itemIndex = data.getIntExtra("itemIndex", -1);
                if (-1 == itemIndex) {
                    ToastUtils.showToast("获取workid失败");
                    return;
                }
                addUser(itemIndex);
            }
        }
    }

    /****添加人员到线路列表*/
    private void addUser(int itemIndex) {
        RequestParamsFM params = new RequestParamsFM();
        params.put("id", mPersonsList.get(itemIndex).getId());
        params.put("workid", mPersonsList.get(itemIndex).getWorkid());
        params.put("userid", MyApplication.tempUserID);
        params.put("username", MyApplication.tempUserName);
        params.setUseJsonStreamer(true);
        OkHttpUtils.getInstance().doPost(NetConfig.ADDUSER, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误。");
                SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                    return;
                }
                Gson         gson         = new Gson();
                UpResultBean upResultBean = gson.fromJson(resbody, UpResultBean.class);
                ToastUtils.showToast(upResultBean.getMessage());
                if (!"1".equals(upResultBean.getCode())) {
                    SoundUtil.getInstance().playSrcAudio(R.raw.repeatclock);
                    return;
                }
                //播报语音
                playAudio(upResultBean.getAudio());
                //刷新页面
                getFirstData();
            }
        });
    }

    private void getFirstData() {
        mPersonsList.clear();
        pageNum = 1;
        //获取开机记录列表
        getOpenHistoryList();
    }

    /****获取开机记录列表*/
    private void getOpenHistoryList() {
        swiperefresh.setRefreshing(true);
        RequestParamsFM params = new RequestParamsFM();
        //params.put("id", "869066035238777");
        params.put("id", MyApplication.devID);
        params.put("page_num", pageNum);
        params.put("number", 5);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.KJRECORDLIST, params, new OkHttpUtils.HttpCallBack() {
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
                Gson           gson           = new Gson();
                OpenRecordBean openRecordBean = gson.fromJson(resbody, OpenRecordBean.class);
                ToastUtils.showToast(openRecordBean.getMessage());
                if (!"1".equals(openRecordBean.getCode())) {
                    return;
                }
                mPersonsList.addAll(openRecordBean.getList());
                personsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setSwipRefresh() {
        swiperefresh.setColorSchemeColors(getResources().getColor(R.color.blue_icon), getResources().getColor(R.color.yellow_40), getResources().getColor(R.color.red_160));
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //获取初始数据
                getFirstData();
            }
        });
    }

    /***初始化人员列表*/
    private void initPersonList() {
        mPersonsList = new ArrayList();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recy_person.setLayoutManager(linearLayoutManager);
        personsAdapter = new PersonsOpenAdapter(this, R.layout.recy_persons, mPersonsList);
        recy_person.setAdapter(personsAdapter);
        personsAdapter.setOnLoadMoreListener(() -> {
            pageNum++;
            getOpenHistoryList();
        });
    }

    private String audioFilePath = "";

    /***播报语音
     * @param audio*/
    private void playAudio(String audio) {
        audioFilePath = LocalSetting.AUDIO_PATH + "bobao.wav";
        FileUtil.deleteFile(audioFilePath);
        AudioUtil.getInstance().setChangeListener(new AudioUtil.ChangeFileListener() {
            @Override
            public void onSuccess() {
                //播放音频
                SoundUtil.getInstance().playAudio(audioFilePath);
            }

            @Override
            public void onFailed() {
                ToastUtils.showToast("音频播放失败！");
            }
        }).decoderBase642File(audio, audioFilePath);
    }
}
