package com.botian.recognition.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.activity.WebUrlActivity;
import com.botian.recognition.bean.ClassLinesBean;
import com.botian.recognition.bean.CommonBean;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.mediaUtils.SoundUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;

public class LiensAdapter extends RecyclerView.Adapter<LiensAdapter.CommemtViewHolder> {
    private Context                       mContext;
    private List<ClassLinesBean.ListBean> mList;
    private String                        urlParamsStr;

    public LiensAdapter(Context context, List<ClassLinesBean.ListBean> list) {
        mContext = context;
        mList    = list;
    }

    public void setUrlParamsStr(String urlParamsStr) {
        this.urlParamsStr = urlParamsStr;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @NonNull
    @Override
    public CommemtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recy_lines, parent, false);
        // 实例化viewholder
        CommemtViewHolder viewHolder = new CommemtViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommemtViewHolder holder, int position) {
        holder.tv_line.setText(mList.get(position).getDepartment());
        holder.tv_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转h5页面
                step2Html();

                //先调取接口，根据接口跳转页面
                //openByService();
            }
        });
    }

    private void step2Html() {
        Intent intent = new Intent(mContext, WebUrlActivity.class);
        intent.putExtra("urlParams", urlParamsStr);
        mContext.startActivity(intent);
    }

    //是否开机
    private void openByService() {
        RequestParamsFM parames = new RequestParamsFM();
        //parames.put("id", "869066035238777");
        parames.put("id", MyApplication.devID);
        OkHttpUtils.getInstance().doGetWithParams(NetConfig.SERKJTYPE, parames, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ToastUtils.showToast("网络错误！");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    return;
                }
                Gson       gson       = new Gson();
                CommonBean commonBean = gson.fromJson(resbody, CommonBean.class);
                //点击开机后的判断
                if (!"1".equals(commonBean.getCode())) {
                    return;
                }
                if ("1".equals(commonBean.getType())) {
                    SoundUtil.getInstance().playSrcAudio(R.raw.openfirst);
                    return;
                }
                //跳转h5页面
                step2Html();
            }
        });
    }

    class CommemtViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_line;

        public CommemtViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_line = itemView.findViewById(R.id.tv_line);
        }
    }
}
