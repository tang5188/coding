package rf.com.usbdemo.helper;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.common.UVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.File;

/**
 * UVCCamera Helper class
 * <p>
 * Created by jiangdongguo on 2017/9/30.
 */

public class UVCDoubleCameraHelper {
    public static final String CameraName1 = "USB2.0 Camera";
    public static final String CameraName2 = "XCX-S58M21";

    private static final String TAG = "UVCDoubleCameraHelper";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    public static final String SUFFIX_JPEG = ".jpg";
    public static final String SUFFIX_MP4 = ".mp4";
    private int previewWidth = 640;
    private int previewHeight = 480;

    public static final int FRAME_FORMAT_YUYV = UVCCamera.FRAME_FORMAT_YUYV;        // 高分辨率YUV格式帧率较低
    public static final int FRAME_FORMAT_MJPEG = UVCCamera.FRAME_FORMAT_MJPEG;      // 默认使用MJPEG

    public static final int MODE_BRIGHTNESS = UVCCamera.PU_BRIGHTNESS;
    public static final int MODE_CONTRAST = UVCCamera.PU_CONTRAST;

    private int mFrameFormat = FRAME_FORMAT_YUYV;

    private static UVCDoubleCameraHelper mCameraHelper;

    // USB Manager
    private USBMonitor mUSBMonitor;

    private Activity mActivity;

    private UVCCameraHandler mCameraHandler1, mCameraHandler2;
    private CameraViewInterface mCamView1, mCamView2;
    private boolean isPreview1, isPreview2;

    private UVCDoubleCameraHelper() {
    }

    public static UVCDoubleCameraHelper getInstance() {
        if (mCameraHelper == null) {
            mCameraHelper = new UVCDoubleCameraHelper();
        }
        return mCameraHelper;
    }

    public interface OnMyDevConnectListener {
        void onAttachDev(UsbDevice device);

        void onDettachDev(UsbDevice device);

        void onConnectDev(UsbDevice device, boolean isConnected);

        void onDisConnectDev(UsbDevice device);
    }

    private int time;
    private Handler handler = new Handler();

    public void initUSBMonitor(Activity activity, CameraViewInterface cameraView1, CameraViewInterface cameraView2, final OnMyDevConnectListener listener) {
        this.mActivity = activity;
        this.mCamView1 = cameraView1;
        this.mCamView2 = cameraView2;
        mUSBMonitor = new USBMonitor(activity.getApplicationContext(), new USBMonitor.OnDeviceConnectListener() {

            @Override
            public void onAttach(final UsbDevice device) {
                Log.d(TAG, "onAttach: ==" + device.getProductName() + ", " + device.getDeviceName() + ", " + device.getDeviceClass() + ", " + device.getDeviceSubclass());
                if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {//根据相机信息选择选需要打开的相机
                    if (device.getProductName().contains(CameraName1) ||
                            device.getProductName().contains(CameraName2)) {

                        if (device.getProductName().contains(CameraName1) && isPreview1) return;
                        if (device.getProductName().contains(CameraName2) && isPreview2) return;

                        //此处请求权限，需点击确定，有系统权限可忽略
                        //每个注册权限增加延时，如果4个摄像同时注册权限 可能权限弹窗只会显示一个，导致其他的相机权限未确定，
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: 发送 dev=" + device.getProductName() + ", " + device.getDeviceName());
                                mUSBMonitor.requestPermission(device);
                            }
                        }, time++ * 500);
                        listener.onAttachDev(device);
                    }
                }
            }

            @Override
            public void onDettach(UsbDevice device) {
                listener.onDettachDev(device);
            }

            @Override
            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                if (device.getProductName().contains(CameraName1) ||
                        device.getProductName().contains(CameraName2)) {

                    if (device.getProductName().contains(CameraName1) && isPreview1) return;
                    if (device.getProductName().contains(CameraName2) && isPreview2) return;

                    new Thread(new myRunnable(device, ctrlBlock)).start();
                    listener.onConnectDev(device, true);
                }
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                Log.d(TAG, "onDisconnect," + device.getProductName() + ", " + device.getDeviceName());
                if (device.getProductName().contains(CameraName1) ||
                        device.getProductName().contains(CameraName2)) {
                    if (device.getProductName().contains(CameraName1)) {
                        isPreview1 = false;
                    } else if (device.getProductName().contains(CameraName2)) {
                        isPreview2 = false;
                    }
                    listener.onDisConnectDev(device);
                }
            }

            @Override
            public void onCancel(UsbDevice device) {
            }
        });

        createUVCCamera();
    }

    public void createUVCCamera() {
        // initialize camera handler
        mCamView1.setAspectRatio(previewWidth / (float) previewHeight);
        mCameraHandler1 = UVCCameraHandler.createHandler(mActivity, mCamView1, 2, previewWidth, previewHeight, mFrameFormat, 0.4f);

        mCamView2.setAspectRatio(previewWidth / (float) previewHeight);
        mCameraHandler2 = UVCCameraHandler.createHandler(mActivity, mCamView2, 2, previewWidth, previewHeight, mFrameFormat, 0.4f);
    }

    class myRunnable implements Runnable {
        private UsbDevice device;
        private USBMonitor.UsbControlBlock usbControlBlock;
        private UVCCameraHandler cameraHandler;

        public myRunnable(UsbDevice device, USBMonitor.UsbControlBlock usbControlBlock) {
            this.device = device;
            this.usbControlBlock = usbControlBlock;
        }

        @Override
        public void run() {
            if (usbControlBlock.getProductName().contains(CameraName1)) {
                cameraHandler = mCameraHandler1;
            } else if (usbControlBlock.getProductName().contains(CameraName2)) {
                cameraHandler = mCameraHandler2;
            }

            try {
                cameraHandler.open(usbControlBlock);
            } catch (Exception e) {
                Log.d(TAG, "开启相机错误！！！！" + device.getProductName() + ", " + device.getDeviceName());
                return;
            }

            SurfaceTexture st = null;
            //根据不同相机接入name  或者根据pid vid 指定相机在那个view显示
            if (usbControlBlock.getProductName().contains(CameraName1)) {
                st = mCamView1.getSurfaceTexture();
                isPreview1 = true;
            } else if (usbControlBlock.getProductName().contains(CameraName2)) {
                st = mCamView2.getSurfaceTexture();
                isPreview2 = true;
            }
            cameraHandler.startPreview(st);
        }
    }

    public void registerUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    public void unregisterUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
    }

    public void setDefaultFrameFormat(int format) {
        if (mUSBMonitor != null) {
            throw new IllegalStateException("setDefaultFrameFormat should be call before initMonitor");
        }
        this.mFrameFormat = format;
    }

    public void startPreview1(CameraViewInterface cameraView) {
        SurfaceTexture st = cameraView.getSurfaceTexture();
        if (mCameraHandler1 != null) {
            mCameraHandler1.startPreview(st);
        }
    }

    public void startPreview2(CameraViewInterface cameraView) {
        SurfaceTexture st = cameraView.getSurfaceTexture();
        if (mCameraHandler2 != null) {
            mCameraHandler2.startPreview(st);
        }
    }

    public void stopPreview1() {
        if (mCameraHandler1 != null) {
            mCameraHandler1.stopPreview();
        }
    }

    public void stopPreview2() {
        if (mCameraHandler2 != null) {
            mCameraHandler2.stopPreview();
        }
    }
}
