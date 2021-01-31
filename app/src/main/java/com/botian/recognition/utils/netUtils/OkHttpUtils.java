package com.botian.recognition.utils.netUtils;


import com.botian.recognition.MyApplication;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @创建者 AndyYan
 * @创建时间 2018/4/26 8:42
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class OkHttpUtils {
    // 网络请求超时时间值(s)
    private static final int             DEFAULT_TIMEOUT = 30;
    private static       OkHttpUtils     okhUtils;
    private static       OkHttpClient    client;
    private static       boolean         useDefaultHeadParam;
    private              RequestParamsFM defaultHeadbean;

    private OkHttpUtils() {
        client = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpUtils getInstance() {
        if (okhUtils == null) {
            synchronized (OkHttpUtils.class) {
                if (okhUtils == null)
                    okhUtils = new OkHttpUtils();
            }
        }
        useDefaultHeadParam = false;
        return okhUtils;
    }

    private OkHttpClient getOkClient() {
        return client;
    }

    /**
     * 根据Tag取消请求
     */
    public void cancelTag(Object tag) {
        if (null == tag || null == client) {
            return;
        }
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 取消所有请求请求
     */
    public void cancelAll() {
        if (null == client) {
            return;
        }
        for (Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    public OkHttpUtils useDefaultHeadParams(boolean isUsedDefaultHead) {
        useDefaultHeadParam = isUsedDefaultHead;
        if (useDefaultHeadParam) {
            defaultHeadbean = new RequestParamsFM();
            defaultHeadbean.put("token", MyApplication.tempUserID);
        }
        return okhUtils;
    }

    /**
     * get请求
     **/
    public void doGet(String url, HttpCallBack httpCallBack) {
        if (useDefaultHeadParam) {
            doGetWithParams(url, defaultHeadbean, null, httpCallBack);
        } else {
            doGetWithParams(url, null, null, httpCallBack);
        }
    }

    public void doGetWithOnlyHeader(String url, RequestParamsFM headbean, HttpCallBack httpCallBack) {
        doGetWithParams(url, headbean, null, httpCallBack);
    }

    public void doGetWithParams(String url, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        if (useDefaultHeadParam) {
            doGetWithParams(url, defaultHeadbean, paramBean, httpCallBack);
        } else {
            doGetWithParams(url, null, paramBean, httpCallBack);
        }
    }

    public void doGetWithParams(String url, RequestParamsFM headbean, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        Request.Builder builder = new Request.Builder();
        if (null != headbean) {
            Set<String> set1 = headbean.keySet();
            for (String key : set1) {
                if (null != headbean.get(key)) {
                    builder.addHeader(key, headbean.get(key).toString());
                }
            }
        }
        url = url + "?";
        if (null != paramBean) {
            Iterator iter = paramBean.entrySet().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (null != next) {
                    RequestParamsFM.Entry entry = (RequestParamsFM.Entry) next;
                    Object                key   = entry.getKey();
                    Object                value = entry.getValue();
                    url = url + key + "=" + value + "&";
                } else {
                    RequestParamsFM.Entry entry = (RequestParamsFM.Entry) next;
                    Object                key   = entry.getKey();
                    Object                value = entry.getValue();
                    url = url + key + "=" + value;
                }
            }
        }
        url = url.substring(0, url.length() - 1);
        Request request = builder.url(url).build();
        client.newCall(request).enqueue(new StringCallBack(request, httpCallBack));
    }

    /**
     * post请求
     **/
    public void doPost(String url, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        if (useDefaultHeadParam) {
            doPost(url, defaultHeadbean, paramBean, httpCallBack);
        } else {
            doPost(url, null, paramBean, httpCallBack);
        }
    }

    public void doPost(String url, RequestParamsFM headbean, RequestParamsFM bean, HttpCallBack httpCallBack) {
        Request.Builder builder1 = new Request.Builder();
        if (null != headbean) {
            Set<String> set1 = headbean.keySet();
            for (String key : set1) {
                if (null != headbean.get(key)) {
                    builder1.addHeader(key, headbean.get(key).toString());
                }
            }
        }
        RequestBody requestBody;
        if (null == bean) {
            FormBody.Builder builder = new FormBody.Builder();
            requestBody = builder.build();
        } else {
            if (bean.getIsUseJsonStreamer()) {
                String      json;
                JSONObject  jsonObject = new JSONObject();
                Set<String> set1       = bean.keySet();
                for (Iterator<String> it = set1.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        jsonObject.put(key, bean.get(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                json = jsonObject.toString();
                //MediaType  设置Content-Type 标头中包含的媒体类型值
//                requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            } else {
                FormBody.Builder builder = new FormBody.Builder();
                Set<String>      set2    = bean.keySet();
                for (String key : set2) {
                    if (null != bean.get(key)) {
                        String value = bean.get(key).toString();
                        builder.add(key, value);
                    }
                }
                requestBody = builder.build();
            }
        }
        Request request = builder1.url(url).post(requestBody).build();
        client.newCall(request).enqueue(new StringCallBack(request, httpCallBack));
    }

    /**
     * put请求
     **/
    public void doPut(String url, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        if (useDefaultHeadParam) {
            doPut(url, defaultHeadbean, paramBean, httpCallBack);
        } else {
            doPut(url, null, paramBean, httpCallBack);
        }
    }

    public void doPut(String url, RequestParamsFM headbean, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        Request.Builder builder1 = new Request.Builder();
        if (null != headbean) {
            Set<String> set1 = headbean.keySet();
            for (String key : set1) {
                if (null != headbean.get(key)) {
                    builder1.addHeader(key, headbean.get(key).toString());
                }
            }
        }
        RequestBody requestBody;
        if (null == paramBean) {
            FormBody.Builder builder = new FormBody.Builder();
            requestBody = builder.build();
        } else {
            if (paramBean.getIsUseJsonStreamer()) {
                Gson gson = new Gson();
                //使用Gson将对象转换为json字符串
                String json = gson.toJson(paramBean);
                //MediaType  设置Content-Type 标头中包含的媒体类型值
                requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            } else {
                FormBody.Builder builder = new FormBody.Builder();
                Set<String>      set     = paramBean.keySet();
                for (String key : set) {
                    if (null != paramBean.get(key)) {
                        String value = paramBean.get(key).toString();
                        builder.add(key, value);
                    }
                }
                requestBody = builder.build();
            }
        }
        Request request = builder1.url(url).put(requestBody).build();
        client.newCall(request).enqueue(new StringCallBack(request, httpCallBack));
    }

    /**
     * delete请求
     **/
    public void doDelete(String url, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        if (useDefaultHeadParam) {
            doDelete(url, defaultHeadbean, paramBean, httpCallBack);
        } else {
            doDelete(url, null, paramBean, httpCallBack);
        }
    }

    public void doDelete(String url, RequestParamsFM headbean, RequestParamsFM paramBean, HttpCallBack httpCallBack) {
        Request.Builder builder = new Request.Builder();
        if (null != headbean) {
            Set<String> set1 = headbean.keySet();
            for (String key : set1) {
                if (null != headbean.get(key)) {
                    builder.addHeader(key, headbean.get(key).toString());
                }
            }
        }
        url = url + "?";
        if (null != paramBean) {
            Iterator iter = paramBean.entrySet().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (null != next) {
                    RequestParamsFM.Entry entry = (RequestParamsFM.Entry) next;
                    Object                key   = entry.getKey();
                    Object                value = entry.getValue();
                    url = url + key + "=" + value + "&";
                } else {
                    RequestParamsFM.Entry entry = (RequestParamsFM.Entry) next;
                    Object                key   = entry.getKey();
                    Object                value = entry.getValue();
                    url = url + key + "=" + value;
                }
            }
        }
        url = url.substring(0, url.length() - 1);
        Request request = builder.url(url).delete().build();
        client.newCall(request).enqueue(new StringCallBack(request, httpCallBack));
    }

    /**
     * 提交文件
     */
    public void upDateFile(String url, RequestParamsFM paramBean, String fileKey, File file, HttpCallBack httpCallBack) {
        if (useDefaultHeadParam) {
            upDateFile(url, defaultHeadbean, paramBean, fileKey, file, httpCallBack);
        } else {
            upDateFile(url, null, paramBean, fileKey, file, httpCallBack);
        }
    }

    /**
     * url 地址
     * headbean 请求头
     * bean  参数
     * fileKey
     * file 文件
     */
    public void upDateFile(String url, RequestParamsFM headbean, RequestParamsFM paramBean, String fileKey, File file, HttpCallBack httpCallBack) {
        Request.Builder builder1 = new Request.Builder();
        if (null != headbean) {
            Set<String> set1 = headbean.keySet();
            for (String key : set1) {
                builder1.addHeader(key, headbean.get(key).toString());
            }
        }
        RequestBody           fileRequestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Builder builder         = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (null != paramBean) {
            Set<String> set = paramBean.keySet();
            for (String key : set) {
                String value = paramBean.get(key).toString();
                builder.addFormDataPart(key, value);
            }
        }
        builder.addFormDataPart(fileKey, file.getName(), fileRequestBody);
        RequestBody requestBody = builder.build();
        Request     request     = builder1.url(url).post(requestBody).build();
        client.newCall(request).enqueue(new StringCallBack(request, httpCallBack));
    }

    /**
     * 请求返回接口
     */
    private static class StringCallBack implements Callback {
        private HttpCallBack httpCallBack;
        private Request      request;

        public StringCallBack(Request request, HttpCallBack httpCallBack) {
            this.request = request;
            this.httpCallBack = httpCallBack;
        }

        @Override
        public void onFailure(okhttp3.Call call, final IOException e) {
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    httpCallBack.onError(request, e);
                }
            });
        }

        @Override
        public void onResponse(okhttp3.Call call, Response response) throws IOException {
            if (httpCallBack != null) {
                try {
                    final int    code   = response.code();
                    final String buffer = response.body().string();
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                httpCallBack.onSuccess(code, buffer);
                            } catch (Exception e) {
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 回调接口
     */
    public interface HttpCallBack {

        void onError(Request request, IOException e);

        void onSuccess(int code, String resbody);
    }
}
