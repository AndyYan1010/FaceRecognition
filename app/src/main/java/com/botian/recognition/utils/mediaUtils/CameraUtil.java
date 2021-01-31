package com.botian.recognition.utils.mediaUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.botian.recognition.R;
import com.botian.recognition.utils.ToastUtils;
import com.botian.recognition.utils.imageUtils.BitmapUtil;
import com.botian.recognition.utils.netUtils.ThreadUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraUtil implements Camera.PreviewCallback {
    private static CameraUtil         cameraUtilInstance;
    private        Activity           mActivity;
    private        SurfaceView        mSfview;
    private        ImageView          img_fontBorder;
    private        Camera             mCamera;
    private        byte[]             mPicByte;//临时记录预览下的图片数据
    private        GetFaceImgListener getFaceImgListener;
    private        SurfaceHolder      mSurfaceHolder;

    public static CameraUtil getInstance() {
        if (null == cameraUtilInstance) {
            synchronized (CameraUtil.class) {
                if (null == cameraUtilInstance) {
                    cameraUtilInstance = new CameraUtil();
                }
            }
        }
        return cameraUtilInstance;
    }

    public CameraUtil initView(Activity activity, SurfaceView sfview, ImageView img_fontBorder) {
        mActivity           = activity;
        mSfview             = sfview;
        this.img_fontBorder = img_fontBorder;
        return cameraUtilInstance;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setSurFaceView() {
        //判断是否有前置摄像头
        if (mCamera == null) {
            if (isHasFrontCamera()) {
                mCamera = Camera.open(1);//前置
                //                rotation = 270;
            } else {
                mCamera = Camera.open(0);//后置
                //                rotation = 90;
            }
        }
        //旋转90度
        //mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);//default默认为21，所有手机均支持NV21
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        parameters.setPreviewSize(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);//设置预览分辨率
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        parameters.setPictureSize(supportedPictureSizes.get(0).width, supportedPictureSizes.get(0).height);//设置图片分辨率
        parameters.setPreviewFrameRate(25);
        //打印分辨率
        //if (mActivity instanceof MainActivity) {
        //    String resultCont = "";
        //    for (int i = 0; i < supportedPictureSizes.size(); i++) {
        //        resultCont =
        //                //"预览分辨率：" + supportedPreviewSizes.get(i).width + " * " + supportedPreviewSizes.get(i).height +
        //                "图片分辨率：" + supportedPictureSizes.get(i).width + " * " + supportedPictureSizes.get(i).height;
        //    }
        //    ((MainActivity) mActivity).printCamera(resultCont);
        //}
        //后置需要自动对焦，否则人脸采集照片模糊
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);
        mCamera.startPreview();//开启预览
        mCamera.setPreviewCallback(this);//开启Camera预览回调，重写onPreviewFrame获取相机回调
        mCamera.cancelAutoFocus();//聚焦
        ///已打开相机
        //设置回调
        setCallBackListener();
    }

    /**
     * 设置画面回调
     */
    private void setCallBackListener() {
        mSurfaceHolder = mSfview.getHolder();//获取holder参数
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {//设置holder的回调
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                startPreview(mSurfaceHolder);
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
        mCamera.startFaceDetection();
        mCamera.setFaceDetectionListener((faces, camera) -> {
            if (faces.length == 0) {
                img_fontBorder.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.pic_face_nm));
            } else if (faces.length == 1) {
                img_fontBorder.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.pic_face_on));
                ToastUtils.showToast("检测到人脸");
            } else {
                img_fontBorder.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.pic_face_on));
                ToastUtils.showToast("检测到" + faces.length + "个人脸");
            }
        });
    }

    public boolean isHasFrontCamera() {
        boolean hasFrontCarmera = false;
        int     numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                hasFrontCarmera = true;
            }
        }
        return hasFrontCarmera;
    }

    private void startPreview(SurfaceHolder mSurfaceHolder) {
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        try {
            mCamera.stopPreview();
            mCamera.release();
            mCamera        = null;
            mSurfaceHolder = null;
        } catch (Exception e) {
        }
    }

    public void rePlayCamera() {

    }

    public void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void destroyCamera() {
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

    //获取摄像机图片
    public void getCameraPic(GetFaceImgListener getFaceImgListener) {
        this.getFaceImgListener = getFaceImgListener;
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                mCamera.stopPreview();
                //保存图片
                Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                try {
                    YuvImage image = new YuvImage(mPicByte, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                    if (image != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, stream);
                        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                        //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                        rotateMyBitmap(bmp);
                        stream.close();
                    }
                } catch (Exception ex) {
                    Log.e("Sys", "Error:" + ex.getMessage());
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

    private Bitmap mBmp;

    public void rotateMyBitmap(Bitmap bmp) {
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(0);
        //Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        mBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    public interface GetFaceImgListener {
        void onSuccess(String imgBase64);

        void onFailed();
    }
}
