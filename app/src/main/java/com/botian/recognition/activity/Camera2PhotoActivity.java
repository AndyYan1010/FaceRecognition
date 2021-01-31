//package com.botian.recognition.activity;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.ImageFormat;
//import android.graphics.Matrix;
//import android.graphics.Rect;
//import android.graphics.SurfaceTexture;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.CaptureResult;
//import android.hardware.camera2.TotalCaptureResult;
//import android.hardware.camera2.params.Face;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.Image;
//import android.media.ImageReader;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.provider.Settings;
//import android.util.Log;
//import android.util.Size;
//import android.util.SparseIntArray;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//
//import com.botian.recognition.BaseActivity;
//import com.botian.recognition.R;
//import com.botian.recognition.utils.ToastDialogUtil;
//import com.botian.recognition.utils.ToastUtils;
//import com.tbruyelle.rxpermissions2.RxPermissions;
//
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//
//import butterknife.BindView;
//
//import static com.botian.recognition.utils.ToastDialogUtil.NORMOL_STYLE;
//
//public class Camera2PhotoActivity extends BaseActivity implements View.OnClickListener {
//    @BindView(R.id.tv_time)
//    TextView  tv_time;
//    @BindView(R.id.tv_title)
//    TextView  tv_title;
//    @BindView(R.id.img_fontBorder)
//    ImageView img_fontBorder;
//    @BindView(R.id.img_back)
//    ImageView img_back;
//
//    @BindView(R.id.camera_preview)
//    TextureView camera_preview;
//    private String cID;
//    private Size   captureSize;
//
//    private String[] mListPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
//
//    @Override
//    public int setLayout() {
//        return R.layout.act_camera2_photo;
//    }
//
//
//    @Override
//    public void initView(Bundle savedInstanceState) {
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void initData() {
//        //请求拍照权限，然后显示拍照
//        showCamera();
//    }
//
//    @Override
//    public void initListener() {
//        img_back.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.img_back:
//                finish();
//                break;
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//    }
//
//    /**
//     * 显示人脸
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @SuppressLint("CheckResult")
//    private void showCamera() {
//        new RxPermissions(this)
//                .request(mListPermission)
//                .subscribe(granted -> {
//                    if (granted) {
//                        //获取前置摄像头，显示在SurfaceView上
//                        //setSurFaceView();
//                        //打开摄像头
//                        openCamera(true);
//                    } else {
//                        //未开启定位权限或者被拒绝的操作
//                        ToastDialogUtil.getInstance()
//                                .setContext(this)
//                                .useStyleType(NORMOL_STYLE)
//                                .setTitle("无法获取拍照权限")
//                                .setCont("您好，设备需使用相关权限，才能保证软件的正常运行。")
//                                .showCancelView(true, "取消", (dialogUtil, view) -> dialogUtil.dismiss())
//                                .showSureView(true, "去设置", (dialogUtil, view) -> {
//                                    //跳转设置界面
//                                    Intent intent = new Intent();
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
//                                    startActivity(intent);
//                                    finish();
//                                })
//                                .show();
//                    }
//                });
//    }
//
//    private Size  cPixelSize;//相机成像尺寸
//    private int   cOrientation;
//    private int[] faceDetectModes;
//
//    /***打开摄像头*/
//    @SuppressLint("MissingPermission")
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void openCamera(boolean isFront) {
//        this.isFront = isFront;
//        closeCamera();
//        if (isFront) {
//            cID = CameraCharacteristics.LENS_FACING_BACK + "";
//        } else {
//            cID = CameraCharacteristics.LENS_FACING_FRONT + "";
//        }
//        CameraManager         cameraManager         = (CameraManager) getSystemService(CAMERA_SERVICE);
//        CameraCharacteristics cameraCharacteristics = null;
//        try {
//            cameraCharacteristics = cameraManager.getCameraCharacteristics(cID);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        StreamConfigurationMap map          = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//        Size[]                 previewSizes = map.getOutputSizes(SurfaceTexture.class);//获取预览尺寸
//        Size[]                 captureSizes = map.getOutputSizes(ImageFormat.JPEG);//获取拍照尺寸
//        cOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);//获取相机角度
//        Rect cRect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);//获取成像区域
//        cPixelSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);//获取成像尺寸，同上
//        //可用于判断是否支持人脸检测，以及支持到哪种程度
//        faceDetectModes = cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);//支持的人脸检测模式
//        int maxFaceCount = cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);//支持的最大检测人脸数量
//        
//        //此处写死640*480，实际从预览尺寸列表选择
//        //Size sSize = new Size(640, 480);//previewSizes[0];
//        Size sSize = previewSizes[0];//previewSizes[0];
//        //设置预览尺寸（避免控件尺寸与预览画面尺寸不一致时画面变形）
//        SurfaceTexture surfaceTexture = camera_preview.getSurfaceTexture();
//        surfaceTexture.setDefaultBufferSize(sSize.getWidth(), sSize.getHeight());
//        //根据摄像头ID，开启摄像头
//        try {
//            cameraManager.openCamera(cID, getCDeviceOpenCallback(), getCHandler());
//        } catch (CameraAccessException e) {
//        }
//    }
//
//    CameraDevice.StateCallback cDeviceOpenCallback = null;//相机开启回调
//    CameraDevice               cDevice;
//    CameraCaptureSession       cSession;
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CameraDevice.StateCallback getCDeviceOpenCallback() {
//        if (cDeviceOpenCallback == null) {
//            cDeviceOpenCallback = new CameraDevice.StateCallback() {
//                @Override
//                public void onOpened(@NonNull CameraDevice camera) {
//                    cDevice = camera;
//                    try {
//                        //创建Session，需先完成画面呈现目标（此处为预览和拍照Surface）的初始化
//                        camera.createCaptureSession(Arrays.asList(getPreviewSurface(), getCaptureSurface()), new CameraCaptureSession.StateCallback() {
//                            @Override
//                            public void onConfigured(@NonNull CameraCaptureSession session) {
//                                cSession = session;
//                                //构建预览请求，并发起请求
//                                //Log("[发出预览请求]");
//                                try {
//                                    session.setRepeatingRequest(getPreviewRequest(), getPreviewCallback(), getCHandler());
//                                } catch (CameraAccessException e) {
//                                    //Log(Log.getStackTraceString(e));
//                                }
//                            }
//
//                            @Override
//                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
//                                session.close();
//                            }
//                        }, getCHandler());
//                    } catch (CameraAccessException e) {
//                        //Log(Log.getStackTraceString(e));
//                    }
//                }
//
//                @Override
//                public void onDisconnected(@NonNull CameraDevice camera) {
//                    camera.close();
//                }
//
//                @Override
//                public void onError(@NonNull CameraDevice camera, int error) {
//                    camera.close();
//                }
//            };
//        }
//        return cDeviceOpenCallback;
//    }
//
//    /*---------------------------------预览相关---------------------------------*/
//
//    /**
//     * 初始化并获取预览回调对象
//     *
//     * @return
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CameraCaptureSession.CaptureCallback getPreviewCallback() {
//        if (previewCallback == null) {
//            previewCallback = new CameraCaptureSession.CaptureCallback() {
//                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
//                    Camera2PhotoActivity.this.onCameraImagePreviewed(result);
//                }
//            };
//        }
//        return previewCallback;
//    }
//
//    CaptureRequest.Builder               previewRequestBuilder;//预览请求构建
//    CaptureRequest                       previewRequest;//预览请求
//    CameraCaptureSession.CaptureCallback previewCallback;//预览回调
//
//    /**
//     * 生成并获取预览请求
//     *
//     * @return
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CaptureRequest getPreviewRequest() {
//        previewRequest = getPreviewRequestBuilder().build();
//        return previewRequest;
//    }
//
//    /**
//     * 初始化并获取预览请求构建对象，进行通用配置，并每次获取时进行人脸检测级别配置
//     *
//     * @return
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CaptureRequest.Builder getPreviewRequestBuilder() {
//        if (previewRequestBuilder == null) {
//            try {
//                previewRequestBuilder = cSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                previewRequestBuilder.addTarget(getPreviewSurface());
//                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);//自动曝光、白平衡、对焦
//            } catch (CameraAccessException e) {
//                //Log(Log.getStackTraceString(e));
//            }
//        }
//        previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, getFaceDetectMode());//设置人脸检测级别
//        return previewRequestBuilder;
//    }
//
//    /**
//     * 获取支持的最高人脸检测级别
//     *
//     * @return
//     */
//    private int getFaceDetectMode() {
//        if (faceDetectModes == null) {
//            return CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL;
//        } else {
//            return faceDetectModes[faceDetectModes.length - 1];
//        }
//    }
//
//    HandlerThread cHandlerThread;//相机处理线程
//    Handler       cHandler;//相机处理
//
//    /**
//     * 初始化并获取相机线程处理
//     *
//     * @return
//     */
//    private Handler getCHandler() {
//        if (cHandler == null) {
//            //单独开一个线程给相机使用
//            cHandlerThread = new HandlerThread("cHandlerThread");
//            cHandlerThread.start();
//            cHandler = new Handler(cHandlerThread.getLooper());
//        }
//        return cHandler;
//    }
//
//    private Surface previewSurface;//预览Surface
//
//    /**
//     * 获取预览Surface
//     *
//     * @return
//     */
//    private Surface getPreviewSurface() {
//        if (previewSurface == null) {
//            previewSurface = new Surface(camera_preview.getSurfaceTexture());
//        }
//        return previewSurface;
//    }
//
//
//    /*---------------------------------拍照相关---------------------------------*/
//    /**
//     * 初始化拍照相关
//     */
//    private ImageReader cImageReader;
//    private Surface     captureSurface;//拍照Surface
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private Surface getCaptureSurface() {
//        if (cImageReader == null) {
//            cImageReader = ImageReader.newInstance(getCaptureSize().getWidth(), getCaptureSize().getHeight(), ImageFormat.JPEG, 2);
//            cImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    onCaptureFinished(reader);
//                }
//            }, getCHandler());
//            captureSurface = cImageReader.getSurface();
//        }
//        return captureSurface;
//    }
//
//    Bitmap  bitmap;
//    boolean isFront;
//
//    /**
//     * 处理相机拍照完成的数据
//     *
//     * @param reader
//     */
//    private void onCaptureFinished(ImageReader reader) {
//        Image      image  = reader.acquireLatestImage();
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[]     data   = new byte[buffer.remaining()];
//        buffer.get(data);
//        image.close();
//        buffer.clear();
//
//        if (bitmap != null) {
//            bitmap.recycle();
//            bitmap = null;
//        }
//        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        data   = null;
//        if (bitmap != null) {
//            //TODO 2 前置摄像头翻转照片
//            if (isFront) {
//                Matrix matrix = new Matrix();
//                matrix.postScale(-1, 1);
//                Bitmap imgToShow = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//                bitmap.recycle();
//                showImage(imgToShow);
//            } else {
//                showImage(bitmap);
//            }
//        }
//        Runtime.getRuntime().gc();
//    }
//
//    private void showImage(final Bitmap image) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //imageView.setImageBitmap(image);
//            }
//        });
//    }
//
//    public void SetCaptureSize(Size captureSize) {
//        this.captureSize = captureSize;
//    }
//
//    /**
//     * 获取拍照尺寸
//     *
//     * @return
//     */
//    private Size getCaptureSize() {
//        if (captureSize != null) {
//            return captureSize;
//        } else {
//            return cPixelSize;
//        }
//    }
//
//    /**
//     * 执行拍照
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void executeCapture() {
//        try {
//            Log.i(this.getClass().getName(), "发出请求");
//            cSession.capture(getCaptureRequest(), getCaptureCallback(), getCHandler());
//        } catch (CameraAccessException e) {
//            //Log(Log.getStackTraceString(e));
//        }
//    }
//
//    CaptureRequest captureRequest;
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CaptureRequest getCaptureRequest() {
//        captureRequest = getCaptureRequestBuilder().build();
//        return captureRequest;
//    }
//
//    CaptureRequest.Builder captureRequestBuilder;
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CaptureRequest.Builder getCaptureRequestBuilder() {
//        if (captureRequestBuilder == null) {
//            try {
//                captureRequestBuilder = cSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//                captureRequestBuilder.addTarget(getCaptureSurface());
//                //TODO 拍照静音尝试
//                // AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                // audioManager.setStreamMute(/AudioManager.STREAM_SYSTE、AudioManager.STREAM_SYSTEM_ENFORCED/7, true);
//                // audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//
//                //TODO 1 照片旋转
//                int rotation   = getWindowManager().getDefaultDisplay().getRotation();
//                int rotationTo = getOrientation(rotation);
//                captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotationTo);
//            } catch (CameraAccessException e) {
//                //Log(Log.getStackTraceString(e));
//            }
//        }
//        return captureRequestBuilder;
//    }
//
//    //为了使照片竖直显示
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//    private int getOrientation(int rotation) {
//        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
//        // We have to take that into account and rotate JPEG properly.
//        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
//        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
//        return (ORIENTATIONS.get(rotation) + cOrientation + 270) % 360;
//    }
//
//    CameraCaptureSession.CaptureCallback captureCallback;
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private CameraCaptureSession.CaptureCallback getCaptureCallback() {
//        if (captureCallback == null) {
//            captureCallback = new CameraCaptureSession.CaptureCallback() {
//                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
//                    Camera2PhotoActivity.this.onCameraImagePreviewed(result);
//                }
//            };
//        }
//        return captureCallback;
//    }
//
//    /**
//     * 处理相机画面处理完成事件，获取检测到的人脸坐标，换算并绘制方框
//     *
//     * @param result
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void onCameraImagePreviewed(CaptureResult result) {
//        Face faces[] = result.get(CaptureResult.STATISTICS_FACES);
//        ToastUtils.showToast("人脸个数:[" + faces.length + "]");
//    }
//
//    private void closeCamera() {
//        if (cSession != null) {
//            cSession.close();
//            cSession = null;
//        }
//        if (cDevice != null) {
//            cDevice.close();
//            cDevice = null;
//        }
//        if (cImageReader != null) {
//            cImageReader.close();
//            cImageReader          = null;
//            captureRequestBuilder = null;
//        }
//        if (cHandlerThread != null) {
//            cHandlerThread.quitSafely();
//            try {
//                cHandlerThread.join();
//                cHandlerThread = null;
//                cHandler       = null;
//            } catch (InterruptedException e) {
//                //Log(Log.getStackTraceString(e));
//            }
//        }
//
//    }
//}
