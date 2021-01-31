package com.botian.recognition.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.botian.recognition.BaseActivity;
import com.botian.recognition.R;
import com.botian.recognition.utils.ToastUtils;

import butterknife.BindView;

public class WebUrlActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.web_url)
    WebView   web_url;
    @BindView(R.id.img_logo)
    ImageView img_logo;

    private String urlParamsStr;
    private String askUrl;
    private int    REQUEST_CODE_GET_FACE = 10001;
    private int    RESULT_CODE_FOR_WEB   = 10014;
    private int    webType               = -1;

    @Override
    protected int setLayout() {
        return R.layout.act_web_url;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {
        urlParamsStr = getIntent().getStringExtra("urlParams");
        if (null == urlParamsStr || "".equals(urlParamsStr)) {
            ToastUtils.showToast("跳转链接参数获取失败！");
            return;
        }
        askUrl = "http://81.68.102.112:8088/h5" + urlParamsStr;
        //设置webview
        initWebView();
    }

    @Override
    protected void initListener() {
        img_logo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_logo:
                //transfer2web(getIntent().getStringExtra("resultResbody"));
                ////调js方法
                //web_url.evaluateJavascript("sum(1,2)", new ValueCallback<String>() {
                //    @Override
                //    public void onReceiveValue(String value) {
                //        Toast.makeText(getApplicationContext(),
                //                "相加结果：" + value, Toast.LENGTH_SHORT).show();
                //    }
                //});
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_FACE && resultCode == RESULT_CODE_FOR_WEB && null != data) {
            //传给web数据
            transfer2web(data.getStringExtra("resultResbody"));
        }
    }

    @SuppressLint("JavascriptInterface")
    private void initWebView() {
        //启用支持javascript
        WebSettings settings = web_url.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);//打开本地缓存提供JS调用,至关重要
        settings.setAllowFileAccess(true);
        // 设置允许JS弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 设置可以支持缩放
        settings.setSupportZoom(true);
        //设置出现缩放工具
        settings.setBuiltInZoomControls(true);
        web_url.setInitialScale(70);//100代表不缩放
        

        //设置webview
        web_url.loadUrl(askUrl);
        web_url.canGoBack();
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        web_url.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                // 如果url的协议 = 预先约定的 js 协议
                // 就解析往下解析参数
                if (uri.getScheme().equals("js") || uri.getAuthority().equals("")) {
                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("webview")) {
                        // 执行JS所需要调用的逻辑
                        // 可以在协议上带有参数并传递到Android上

                    }
                    return true;
                }
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        //自适应屏幕
        web_url.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        web_url.getSettings().setLoadWithOverviewMode(true);

        web_url.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    //if (needClear) {
                    //    web_url.clearHistory();
                    //    needClear = false;
                    //}
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return true;
            }
        });

        jsObject = new JavaScriptObject(this);
        //这里是我们要执行js方法的对象
        web_url.addJavascriptInterface(jsObject, "shangban");

        /*
         * 为webView 添加一个js 接口
         * 参数一： 是一个java对象
         * 参数二：是一个随意的字符串
         * 该方法的功能是在网页中创建一个js 对象，对象名就是参数二字符串。js对象中的功能，由参数一提供。
         */
        web_url.addJavascriptInterface(new Object() {
            /**
             * 声明一个在js 中可以调用的方法，
             * 注意：4.4以上这里要加注解。
             * 类名：shangyukeji和方法名paizhao跟html保持一致
             */
            @JavascriptInterface
            public void paizhao(int type) {
                webType = type;
                //跳转人脸认证
                step2CheckFace();
            }
        }, "shangyukeji");
    }

    private JavaScriptObject jsObject;

    //传输给web端认证数据
    private void transfer2web(String resultResbody) {
        if (webType == 0) {
            web_url.loadUrl("javascript:kaiji('" + resultResbody + "')");
        } else if (webType == 1) {
            web_url.loadUrl("javascript:guanji('" + resultResbody + "')");
        } else if (webType == 2) {
            web_url.loadUrl("javascript:add('" + resultResbody + "')");
        }
        //jsObject.onRecordFinished(web_url, resultResbody);

        //web_url.evaluateJavascript("javascript:shangban(\"" + resultResbody + "\")", new ValueCallback<String>() {
        //    @Override
        //    public void onReceiveValue(String value) {
        //        ToastUtils.showToast("js返回：" + value);
        //    }
        //});

        //web_url.loadUrl("javascript:shangban(\"" + resultResbody + "\")");
    }

    /***跳转人脸认证*/
    private void step2CheckFace() {
        Intent intent = new Intent(this, CameraPhotoActivity.class);
        intent.putExtra("ftype", 5);
        startActivityForResult(intent, REQUEST_CODE_GET_FACE);
    }

    /**
     * android和webview交互接口
     */
    class JavaScriptObject {
        private Context context;

        public JavaScriptObject(Context context) {
            this.context = context;
        }

        /**
         * 拍照或者选择照片结束
         *
         * @param webview
         * @param base64String
         */
        public void onCaptureFinished(WebView webview, String base64String) {
            webview.loadUrl("javascript:onCaptureFinished('" + base64String + "')");
        }

        /**
         * 录屏结束
         *
         * @param webView
         */
        public void onRecordFinished(WebView webView, String resultResbody) {
            webView.loadUrl("javascript:shangban('" + resultResbody + "')");
        }
    }
}
