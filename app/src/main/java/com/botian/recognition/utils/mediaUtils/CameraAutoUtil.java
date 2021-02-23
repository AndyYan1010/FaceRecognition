package com.botian.recognition.utils.mediaUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.botian.recognition.MyApplication;
import com.botian.recognition.NetConfig;
import com.botian.recognition.R;
import com.botian.recognition.bean.FaceResultBean;
import com.botian.recognition.configure.LocalSetting;
import com.botian.recognition.utils.AudioTimeUtil;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.fileUtils.FileUtil;
import com.botian.recognition.utils.imageUtils.BitmapUtil;
import com.botian.recognition.utils.netUtils.OkHttpUtils;
import com.botian.recognition.utils.netUtils.RequestParamsFM;
import com.botian.recognition.utils.netUtils.ThreadUtils;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class CameraAutoUtil implements Camera.PreviewCallback {
    private static CameraAutoUtil                cameraUtilInstance;
    private        Activity                      mActivity;
    private        int                           ftype = -1;
    private        SurfaceView                   mSfview;
    private        ImageView                     img_fontBorder;
    private        Camera                        mCamera;
    private        byte[]                        mPicByte;//临时记录预览下的图片数据
    private        ContChangeListener            contChangeListener;
    private        FinishListener                finishListener;
    private        boolean                       isUpLoadGetFacePic;
    private        List<FaceResultBean.DataBean> mPersonList;//人脸认证结果

    public static CameraAutoUtil getInstance() {
        if (null == cameraUtilInstance) {
            synchronized (CameraAutoUtil.class) {
                if (null == cameraUtilInstance) {
                    cameraUtilInstance = new CameraAutoUtil();
                }
            }
        }
        return cameraUtilInstance;
    }

    public CameraAutoUtil initView(Activity activity, SurfaceView sfview, ImageView img_fontBorder) {
        mActivity           = activity;
        mSfview             = sfview;
        this.img_fontBorder = img_fontBorder;
        if (null == mPersonList) {
            mPersonList = new ArrayList();
        } else {
            mPersonList.clear();
        }
        return cameraUtilInstance;
    }

    /***设置认证类型*/
    public CameraAutoUtil setFtype(int ftype) {
        this.ftype = ftype;
        return cameraUtilInstance;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setSurFaceView() {
        //判断是否有前置摄像头
        if (mCamera == null) {
            if (CameraUtil.getInstance().isHasFrontCamera()) {
                mCamera = Camera.open(1);//前置
                //                rotation = 270;
            } else {
                if (MyApplication.flagScreen) {
                    mCamera = Camera.open(1);
                } else {
                    mCamera = Camera.open(0);//后置
                }
                //                rotation = 90;
            }
        }
        function();
    }

    private void function() {
        //旋转90度
        if (MyApplication.flagScreen) {
            mCamera.setDisplayOrientation(90);
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);//default默认为21，所有手机均支持NV21
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        parameters.setPreviewSize(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);//设置预览分辨率
        if (!MyApplication.flagScreen) {
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            parameters.setPictureSize(supportedPictureSizes.get(0).width, supportedPictureSizes.get(0).height);//设置图片分辨率
        }
        parameters.setPreviewFrameRate(25);

        //后置需要自动对焦，否则人脸采集照片模糊
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        if (!MyApplication.flagScreen) {
            mCamera.setParameters(parameters);
        }
        mCamera.startPreview();//开启预览
        mCamera.setPreviewCallback(this);//开启Camera预览回调，重写onPreviewFrame获取相机回调
        mCamera.cancelAutoFocus();//聚焦
        //已打开相机
        final SurfaceHolder mSurfaceHolder = mSfview.getHolder();//获取holder参数
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {//设置holder的回调
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                startPreview(mSurfaceHolder);
                //mCamera.startFaceDetection();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                stopPreview();
            }
        });
        //读取脸部信息
        //if (!MyApplication.flagScreen) {
        //    mCamera.startFaceDetection();
        //    mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
        //        @Override
        //        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        //            if (faces.length == 0) {
        //                img_fontBorder.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.pic_face_nm));
        //                //TimeUtil.getInstance().stopCountDown();
        //            } else if (faces.length == 1) {
        //                ToastUtils.showToast("检测到人脸");
        //                //countGetFace();
        //            } else {
        //                ToastUtils.showToast("检测到" + faces.length + "个人脸");
        //                //countGetFace();
        //            }
        //        }
        //    });
        //}
    }

    /***倒计时获取人脸*/
    public void countGetFace() {
        img_fontBorder.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.pic_face_on));
        if (AudioTimeUtil.getInstance().isCountDown())
            return;
        if (isUpLoadGetFacePic)
            return;
        //倒计时拍照
        if (ftype == 0) {
            mTitleCont = "上班";
        } else if (ftype == 1) {
            mTitleCont = "下班";
        } else if (ftype == 2) {
            mTitleCont = "开机";
        } else if (ftype == 3) {
            mTitleCont = "关机";
        } else if (ftype == 4) {
            mTitleCont = "汇报";
        } else if (ftype == 5) {
            mTitleCont = "认证";
        } else {
            ToastUtils.showToast("未识别类型，请重新选择");
            return;
        }
        getFaceWaitTwoSecond();
    }

    /***开始播放* */
    private void startPreview(SurfaceHolder mSurfaceHolder) {
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            finishListener.onWaiteStart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        //try {
        //    mActivity.finish();
        //} catch (Exception e) {
        //}
    }

    public void stopCamera() {
        //先取消倒计时任务
        if (AudioTimeUtil.getInstance().isCountDown()) {
            AudioTimeUtil.getInstance().cancelCountDown();
        }
        //释放相机资源
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        mPicByte = bytes;
    }

    String mTitleCont = "";

    /***
     * 倒计时两秒拍照
     * */
    private void getFaceWaitTwoSecond() {
        callContChangeListener(mTitleCont, "2");
        AudioTimeUtil.getInstance().initData(2).setOnTimeListener(new AudioTimeUtil.TimeListener() {
            @Override
            public void onStart(String cont) {
                callContChangeListener(mTitleCont, cont);
            }

            @Override
            public void onChange(String cont) {
                callContChangeListener(mTitleCont, cont);
            }

            @Override
            public void onCancel() {
                callContChangeListener("", "");
            }

            @Override
            public void onFinish() {
                callContChangeListener(mTitleCont, "");
                //拍照
                getFaceImg();
            }
        });
    }

    public CameraAutoUtil setContChangeListener(ContChangeListener contChangeListener) {
        this.contChangeListener = contChangeListener;
        return cameraUtilInstance;
    }

    public CameraAutoUtil setFinishListener(FinishListener finishListener) {
        this.finishListener = finishListener;
        return cameraUtilInstance;
    }

    /***
     * 获取人脸
     * */
    private void getFaceImg() {
        isUpLoadGetFacePic = true;
        CameraAutoUtil.getInstance().setGetCameraPicListener(new CameraAutoUtil.GetFaceImgListener() {
            @Override
            public void onSuccess(String imgBase64) {
                //ToastUtils.showToast("人脸获取成功");
                //上传人脸，比对
                uploadFaceBase64("data:image/jpg;base64," + imgBase64);
            }

            @Override
            public void onFailed() {
                isUpLoadGetFacePic = false;
                //ToastUtils.showToast("人脸获取失败，请重新选择");
                callContChangeListener("", "人脸获取失败，请退出重选！");
            }
        });
    }

    /***
     * 上传人脸数据
     * @param imgBase64*/
    private void uploadFaceBase64(String imgBase64) {
        callContChangeListener("", "正在认证人脸信息...");
        mPersonList.clear();
        RequestParamsFM params = new RequestParamsFM();
        params.put("Image", imgBase64);
        OkHttpUtils.getInstance().doPost(NetConfig.SEARCHFACE, params, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                isUpLoadGetFacePic = false;
                ToastUtils.showToast("网络错误！");
                callContChangeListener(mTitleCont, "网络错误!");
                re2Check();
            }

            @Override
            public void onSuccess(int code, String resbody) {
                isUpLoadGetFacePic = false;
                if (code != 200) {
                    ToastUtils.showToast("网络请求错误！");
                    callContChangeListener(mTitleCont, "网络请求错误!");
                    re2Check();
                    return;
                }
                Gson           gson           = new Gson();
                FaceResultBean faceResultBean = gson.fromJson(resbody, FaceResultBean.class);
                if (!faceResultBean.isOk()) {
                    ToastUtils.showToast("人脸认证失败，" + faceResultBean.getMessage());
                    callContChangeListener(mTitleCont, "人脸认证失败!");
                    SoundUtil.getInstance().playSrcAudio(R.raw.facefail);
                    //重新认证
                    re2Check();
                    return;
                }
                ToastUtils.showToast("人脸认证成功");
                mPersonList.addAll(faceResultBean.getData());
                //提交认证信息
                if (null != finishListener)
                    finishListener.onResult(ftype, resbody, mPersonList);
            }
        });
    }

    /****重新认证*/
    private void re2Check() {
        //暂停4秒
        ThreadUtils.sleepSec(4);
        countGetFace();
    }

    //调用内容变化监听器
    public void callContChangeListener(String title, String cont) {
        if (null != contChangeListener)
            contChangeListener.onChange(title, cont);
    }

    //调用完成监听器
    public void callFinishListener(String cont) {
        if (null != finishListener)
            finishListener.onFinish(cont);
    }

    private Bitmap mBmp;
    private String filePath;

    //获取摄像机图片
    public void setGetCameraPicListener(GetFaceImgListener getFaceImgListener) {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                //mCamera.stopPreview();
                //保存图片
                try {
                    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                    YuvImage    image       = new YuvImage(mPicByte, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                    if (image != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, stream);
                        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                        //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                        rotateMyBitmap(bmp);
                        stream.close();
                    }
                } catch (Exception ex) {
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            getFaceImgListener.onFailed();
                        }
                    });
                    return;
                }
                if (null != mBmp) {
                    //将bitmap保存，记录照片本地地址，留待之后上传
                    String resultBase64 = BitmapUtil.bitmapToBase64(mBmp);
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            getFaceImgListener.onSuccess(resultBase64);
                        }
                    });
                    //boolean b = saveBitmap(mBmp);
                    //if (b) {
                    //    ToastUtils.showToast(mActivity, "人脸保存成功");
                    //} else {
                    //    ToastUtils.showToast(mActivity, "人脸保存失败，请退出重新打卡");
                    //}
                } else {
                    ThreadUtils.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            getFaceImgListener.onFailed();
                        }
                    });
                }
            }
        });
    }

    public void rotateMyBitmap(Bitmap bmp) {
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        //Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        mBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * 保存方法
     */
    public boolean saveBitmap(Bitmap bm) {
        long longTime = System.currentTimeMillis();
        FileUtil.existOrCreateFolder(LocalSetting.CAMERA_PHOTO_PATH);
        filePath = LocalSetting.CAMERA_PHOTO_PATH + longTime + "face.png";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            //保存图片后发送广播通知更新数据库
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri    uri    = Uri.fromFile(file);
            intent.setData(uri);
            mActivity.sendBroadcast(intent);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface GetFaceImgListener {
        void onSuccess(String imgBase64);

        void onFailed();
    }

    public interface ContChangeListener {
        void onChange(String titleCont, String cont);
    }

    public interface FinishListener {
        void onWaiteStart();

        void onCancel();

        void onResult(int type, String resbody, List<FaceResultBean.DataBean> mPersonList);

        void onFinish(String result);
    }
}
