package rf.com.usbdemo.helper;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Environment;
import android.util.Log;

import com.jiangdg.libusbcamera.R;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.common.UVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.File;
import java.util.List;

/**
 * UVCCamera Helper class
 * <p>
 * Created by jiangdongguo on 2017/9/30.
 */

public class UVCDoubleCamera2Helper {
    public static final String CameraName1 = "USB2.0 Camera";
    public static final String CameraName2 = "XCX-S58M21";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator;
    public static final String SUFFIX_JPEG = ".jpg";
    public static final String SUFFIX_MP4 = ".mp4";
    private static final String TAG = "UVCDoubleCamera2Helper";
    private int previewWidth = 640;
    private int previewHeight = 480;
    // 高分辨率YUV格式帧率较低
    public static final int FRAME_FORMAT_YUYV = UVCCamera.FRAME_FORMAT_YUYV;
    // 默认使用MJPEG
    public static final int FRAME_FORMAT_MJPEG = UVCCamera.FRAME_FORMAT_MJPEG;
    public static final int MODE_BRIGHTNESS = UVCCamera.PU_BRIGHTNESS;
    public static final int MODE_CONTRAST = UVCCamera.PU_CONTRAST;
    private int mFrameFormat = FRAME_FORMAT_MJPEG;

    private static UVCDoubleCamera2Helper mCameraHelper;
    // USB Manager
    private USBMonitor mUSBMonitor;
    //private UsbManager mUSBManager;
    // Camera Handler
    private UVCCameraHandler mCameraHandler;
    private UVCCameraHandler mCameraHandler2;

    private Activity mActivity;
    private CameraViewInterface mCamView;
    private CameraViewInterface mCamView2;

    private USBMonitor.UsbControlBlock ctrlBlock;
    private USBMonitor.UsbControlBlock ctrlBlock2;

    private UVCDoubleCamera2Helper() {
    }

    public static UVCDoubleCamera2Helper getInstance() {
        if (mCameraHelper == null) {
            mCameraHelper = new UVCDoubleCamera2Helper();
        }
        return mCameraHelper;
    }

    public void closeCamera() {
        if (mCameraHandler != null) {
            mCameraHandler.close();
        }
    }

    public void closeCamera2() {
        if (mCameraHandler2 != null) {
            mCameraHandler2.close();
        }
    }

    public interface OnMyDevConnectListener {
        void onAttachDev(UsbDevice device);

        void onDettachDev(UsbDevice device);

        void onConnectDev(UsbDevice device, boolean isConnected);

        void onDisConnectDev(UsbDevice device);
    }

    public void initUSBMonitor(Activity activity, CameraViewInterface cameraView, final OnMyDevConnectListener listener,
                               CameraViewInterface cameraView2, final OnMyDevConnectListener listener2) {
        this.mActivity = activity;
        this.mCamView = cameraView;
        this.mCamView2 = cameraView2;
        mUSBMonitor = new USBMonitor(activity.getApplicationContext(), new USBMonitor.OnDeviceConnectListener() {

            // called by checking usb device
            // do request device permission
            @Override
            public void onAttach(UsbDevice device) {
                listener.onAttachDev(device);
                Log.i(TAG, "onAttach:" + device.getDeviceName());
            }

            // called by taking out usb device
            // do close camera
            @Override
            public void onDettach(UsbDevice device) {
                if (mCameraHandler.isEqual(device)) {
                    listener.onDettachDev(device);
                } else if (mCameraHandler2.isEqual(device)) {
                    listener2.onDettachDev(device);
                }
                Log.i(TAG, "onDettach:" + device.getDeviceName());
            }

            // called by connect to usb camera
            // do open camera,start previewing
            @Override
            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                Log.i(TAG, "onConnect:" + device.getDeviceName());
                new Thread(new myRunnable(device, ctrlBlock)).start();
            }

            // called by disconnect to usb camera
            // do nothing
            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                if (mCameraHandler != null && mCameraHandler.isEqual(device)) {
                    mCameraHandler.close();
                    listener.onDisConnectDev(device);
                    Log.i(TAG, "onDisconnect,mCameraHandler:" + device.getDeviceName());
                } else if (mCameraHandler2 != null && mCameraHandler2.isEqual(device)) {
                    mCameraHandler2.close();
                    listener2.onDisConnectDev(device);
                    Log.i(TAG, "onDisconnect,mCameraHandler2:" + device.getDeviceName());
                }
                Log.i(TAG, "onDisconnect:" + device.getDeviceName());
            }

            @Override
            public void onCancel(UsbDevice device) {
                Log.i(TAG, "onCancel, " + device.getDeviceName());
            }
        });

        createUVCCamera();
    }

    public void createUVCCamera() {
        if (mCamView == null)
            throw new NullPointerException("CameraViewInterface cannot be null!");

        // release resources for initializing camera handler
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mCameraHandler2 != null) {
            mCameraHandler2.release();
            mCameraHandler2 = null;
        }
        // initialize camera handler
        mCamView.setAspectRatio(previewWidth / (float) previewHeight);
        mCameraHandler = UVCCameraHandler.createHandler(mActivity, mCamView, 2, 320, 240, mFrameFormat, 0.65f);
        mCameraHandler.addCallback(new AbstractUVCCameraHandler.CameraCallback() {
            @Override
            public void onOpen() {
                Log.i(TAG, "onOpen1");
            }

            @Override
            public void onClose() {
                Log.i(TAG, "onClose1");
            }

            @Override
            public void onStartPreview() {
                Log.i(TAG, "onStartPreview1");
            }

            @Override
            public void onStopPreview() {
                Log.i(TAG, "onStopPreview1");
            }

            @Override
            public void onStartRecording() {
                Log.i(TAG, "onStartRecording1");
            }

            @Override
            public void onStopRecording() {
                Log.i(TAG, "onStopRecording1");
            }

            @Override
            public void onError(Exception e) {
                Log.i(TAG, "onError1");
            }
        });

        if (mCameraHandler2 != null) {
            mCameraHandler2.release();
            mCameraHandler2 = null;
        }
        mCamView2.setAspectRatio(previewWidth / (float) previewHeight);
        mCameraHandler2 = UVCCameraHandler.createHandler(mActivity, mCamView2, 2, previewWidth, previewHeight, mFrameFormat, 0.65f);
        mCameraHandler2.addCallback(new AbstractUVCCameraHandler.CameraCallback() {
            @Override
            public void onOpen() {
                Log.i(TAG, "onOpen2");
            }

            @Override
            public void onClose() {
                Log.i(TAG, "onClose2");
            }

            @Override
            public void onStartPreview() {
                Log.i(TAG, "onStartPreview2");
            }

            @Override
            public void onStopPreview() {
                Log.i(TAG, "onStopPreview2");
            }

            @Override
            public void onStartRecording() {
                Log.i(TAG, "onStartRecording2");
            }

            @Override
            public void onStopRecording() {
                Log.i(TAG, "onStopRecording2");
            }

            @Override
            public void onError(Exception e) {
                Log.i(TAG, "onError2");
            }
        });
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
                cameraHandler = mCameraHandler;
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
                st = mCamView.getSurfaceTexture();
            } else if (usbControlBlock.getProductName().contains(CameraName2)) {
                st = mCamView2.getSurfaceTexture();
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

    public void requestPermission(String deviceName) {
        List<UsbDevice> devList = getUsbDeviceList();
        Log.i(TAG, "requestPermission:" + deviceName + ", count:" + (devList == null ? 0 : devList.size()));
        if (devList == null || devList.size() == 0) {
            return;
        }

        for (int i = 0; i < devList.size(); i++) {
            Log.i(TAG, "requestPermission:---" + devList.get(i).getDeviceName());
            if (deviceName.equals(devList.get(i).getDeviceName())) {
                mUSBMonitor.requestPermission(getUsbDeviceList().get(i));
                break;
            }
        }
    }

    public int getUsbDeviceCount() {
        List<UsbDevice> devList = getUsbDeviceList();
        if (devList == null || devList.size() == 0) {
            return 0;
        }
        return devList.size();
    }

    public List<UsbDevice> getUsbDeviceList() {
        List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(mActivity.getApplicationContext(), R.xml.device_filter);
        if (mUSBMonitor == null || deviceFilters == null)
            return null;
        return mUSBMonitor.getDeviceList(deviceFilters.get(0));
    }

    public boolean isCameraOpened() {
        if (mCameraHandler != null) {
            return mCameraHandler.isOpened();
        }
        return false;
    }

    public boolean isCameraOpened2() {
        if (mCameraHandler2 != null) {
            return mCameraHandler2.isOpened();
        }
        return false;
    }

    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    public void stopPreview() {
        if (mCameraHandler != null) {
            mCameraHandler.stopPreview();
        }
    }

    public void stopPreview2() {
        if (mCameraHandler2 != null) {
            mCameraHandler2.stopPreview();
        }
    }

    public void setDefaultFrameFormat(int format) {
        if (mUSBMonitor != null) {
            throw new IllegalStateException("setDefaultFrameFormat should be call before initMonitor");
        }
        this.mFrameFormat = format;
    }

    public void startPreview(CameraViewInterface cameraView) {
        SurfaceTexture st = cameraView.getSurfaceTexture();
        if (mCameraHandler != null) {
            mCameraHandler.startPreview(st);
        }
    }

    public void startPreview2(CameraViewInterface cameraView) {
        SurfaceTexture st = cameraView.getSurfaceTexture();
        if (mCameraHandler2 != null) {
            mCameraHandler2.startPreview(st);
        }
    }
}
