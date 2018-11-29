package rf.com.usbdemo;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.common.UVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DoubleUsbCameraActivity extends Activity {

    private static final String TAG = "DoubleUsbCameraActivity";

    private boolean isRequestPermission;
    private boolean isPreview;

    @BindView(R.id.camera_view1)
    public View mTextureView1;

    private UVCCameraHelper mCameraHelper1;
    private CameraViewInterface mUVCCameraView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_usb_camera);
        ButterKnife.bind(this);

        mUVCCameraView1 = (CameraViewInterface) mTextureView1;
        mUVCCameraView1.setCallback(new CameraViewCallback());
        mCameraHelper1 = UVCCameraHelper.getInstance();
        mCameraHelper1.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper1.initUSBMonitor(this, mUVCCameraView1, listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraHelper1 != null) {
            mCameraHelper1.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraHelper1 != null) {
            mCameraHelper1.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice usbDevice) {
            if (mCameraHelper1 == null || mCameraHelper1.getUsbDeviceCount() == 0) {
                Log.i(TAG, "check no usb camera");
                return;
            }
            if (!isRequestPermission) {
                isRequestPermission = true;
                if (mCameraHelper1 != null) {
                    mCameraHelper1.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice usbDevice) {
            if (isRequestPermission) {
                isRequestPermission = false;
                mCameraHelper1.closeCamera();
                Log.i(TAG, usbDevice.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice usbDevice, boolean isConnected) {
            if (!isConnected) {
                Log.i(TAG, "fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                Log.i(TAG, "connecting");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if (mCameraHelper1 != null && mCameraHelper1.isCameraOpened()) {
                            Log.i(TAG, "set progress");
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice usbDevice) {
            Log.i(TAG, "disconnecting");
        }
    };

    private class CameraViewCallback implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

        @Override
        public USBMonitor getUSBMonitor() {
            return mCameraHelper1.getUSBMonitor();
        }

        @Override
        public void onDialogResult(boolean canceled) {
            Log.i(TAG, "cancel");
        }

        @Override
        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
            if (!isPreview && mCameraHelper1.isCameraOpened()) {
                mCameraHelper1.startPreview(mUVCCameraView1);
                isPreview = true;
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
            if (isPreview && mCameraHelper1.isCameraOpened()) {
                mCameraHelper1.stopPreview();
                isPreview = false;
            }
        }
    }
}
