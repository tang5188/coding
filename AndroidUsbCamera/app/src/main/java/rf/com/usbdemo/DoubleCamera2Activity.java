package rf.com.usbdemo;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.widget.CameraViewInterface;

import butterknife.BindView;
import butterknife.ButterKnife;
import rf.com.usbdemo.helper.UVCDoubleCamera2Helper;

public class DoubleCamera2Activity extends Activity {

    private static final String TAG = "DoubleCamera2Activity";

    private boolean isRequestPermission;
    private boolean isPreview;

    private UVCDoubleCamera2Helper mCameraHelper;

    @BindView(R.id.s_camera_connect1)
    public Switch sCameraConnect1;
    @BindView(R.id.tvCamera1)
    public TextView tvCamera1;
    @BindView(R.id.camera_view1)
    public View mTextureView1;
    private CameraViewInterface mUVCCameraView1;

    @BindView(R.id.s_camera_connect2)
    public Switch sCameraConnect2;
    @BindView(R.id.tvCamera2)
    public TextView tvCamera2;
    @BindView(R.id.camera_view2)
    public View mTextureView2;
    private CameraViewInterface mUVCCameraView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_camera2);
        ButterKnife.bind(this);

        mUVCCameraView1 = (CameraViewInterface) mTextureView1;
        mUVCCameraView1.setCallback(new CameraViewCallback1());

        mUVCCameraView2 = (CameraViewInterface) mTextureView2;
        mUVCCameraView2.setCallback(new CameraViewCallback2());

        mCameraHelper = UVCDoubleCamera2Helper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCDoubleCamera2Helper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView1, listener1, mUVCCameraView2, listener2);

        sCameraConnect1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCameraHelper.startPreview(mUVCCameraView1);
                } else {
                    mCameraHelper.stopPreview();
                }
            }
        });
        sCameraConnect2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCameraHelper.startPreview2(mUVCCameraView2);
                } else {
                    mCameraHelper.stopPreview2();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int time;
    private Handler handler = new Handler();

    private UVCDoubleCamera2Helper.OnMyDevConnectListener listener1 = new UVCDoubleCamera2Helper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(final UsbDevice usbDevice) {
            Log.d(TAG, "onAttach: ==" + usbDevice.getProductName() + ", " + usbDevice.getDeviceName() + ", " + usbDevice.getDeviceClass() + ", " + usbDevice.getDeviceSubclass());
            if (usbDevice.getDeviceClass() == 239 && usbDevice.getDeviceSubclass() == 2) {//根据相机信息选择选需要打开的相机
                if (usbDevice.getProductName().contains(UVCDoubleCamera2Helper.CameraName1) ||
                        usbDevice.getProductName().contains(UVCDoubleCamera2Helper.CameraName2)) {

                    if (usbDevice.getProductName().contains(UVCDoubleCamera2Helper.CameraName1) && mCameraHelper.isCameraOpened())
                        return;
                    if (usbDevice.getProductName().contains(UVCDoubleCamera2Helper.CameraName2) && mCameraHelper.isCameraOpened2())
                        return;

                    //此处请求权限，需点击确定，有系统权限可忽略
                    //每个注册权限增加延时，如果4个摄像同时注册权限 可能权限弹窗只会显示一个，导致其他的相机权限未确定，
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: 发送 dev=" + usbDevice.getProductName() + ", " + usbDevice.getDeviceName());
                            mCameraHelper.requestPermission(usbDevice.getDeviceName());
                        }
                    }, time++ * 2000);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice usbDevice) {
            if (isRequestPermission) {
                isRequestPermission = false;
                mCameraHelper.closeCamera();
            }
            //更新画面
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sCameraConnect1.setChecked(false);
                    tvCamera1.setText("");
                }
            });
            Log.i(TAG, "onDettachDev1, " + usbDevice.getDeviceName() + " is out");
        }

        @Override
        public void onConnectDev(UsbDevice usbDevice, boolean isConnected) {
            if (!isConnected) {
                Log.i(TAG, "onConnectDev, fail to connect,please check resolution params1");
                isPreview = false;
            } else {
                isPreview = true;
                final String usbName = usbDevice.getDeviceName();
                Log.i(TAG, "onConnectDev, " + usbName + " connecting1");
                //更新画面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //sCameraConnect1.setChecked(true);
                        tvCamera1.setText(usbName);
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                            Log.i(TAG, "onConnectDev, set progress1");
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice usbDevice) {
            Log.i(TAG, "onDisConnectDev, disconnecting1");
        }
    };

    private class CameraViewCallback1 implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

        @Override
        public USBMonitor getUSBMonitor() {
            return mCameraHelper.getUSBMonitor();
        }

        @Override
        public void onDialogResult(boolean canceled) {
            Log.i(TAG, "CameraViewInterface.Callback.onDialogResult, cancel1");
        }

        @Override
        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView1);
                isPreview = true;
                Log.i(TAG, "CameraViewInterface.Callback.onSurfaceCreated, onSurfaceCreated1");
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
            if (isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.stopPreview();
                Log.i(TAG, "CameraViewInterface.Callback.onSurfaceDestroy1");
                isPreview = false;
            }
        }
    }

    private UVCDoubleCamera2Helper.OnMyDevConnectListener listener2 = new UVCDoubleCamera2Helper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice usbDevice) {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                Log.i(TAG, "onAttachDev, check no usb camera2");
                return;
            }
            Log.i(TAG, "onAttachDev1, " + usbDevice.getProductName());
            if (!isRequestPermission) {
                //isRequestPermission = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(usbDevice.getDeviceName());
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice usbDevice) {
            if (isRequestPermission) {
                isRequestPermission = false;
                mCameraHelper.closeCamera2();
            }
            //更新画面
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sCameraConnect2.setChecked(false);
                    tvCamera2.setText("");
                }
            });
            Log.i(TAG, "onDettachDev2, " + usbDevice.getDeviceName() + " is out");
        }

        @Override
        public void onConnectDev(UsbDevice usbDevice, boolean isConnected) {
            if (!isConnected) {
                Log.i(TAG, "onConnectDev, fail to connect,please check resolution params2");
                isPreview = false;
            } else {
                isPreview = true;
                final String usbName = usbDevice.getDeviceName();
                Log.i(TAG, "onConnectDev, " + usbName + " connecting2");
                //更新画面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //sCameraConnect2.setChecked(true);
                        tvCamera2.setText(usbName);
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if (mCameraHelper != null && mCameraHelper.isCameraOpened2()) {
                            Log.i(TAG, "onConnectDev, set progress2");
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice usbDevice) {
            Log.i(TAG, "onDisConnectDev, disconnecting2");
        }
    };

    private class CameraViewCallback2 implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

        @Override
        public USBMonitor getUSBMonitor() {
            return mCameraHelper.getUSBMonitor();
        }

        @Override
        public void onDialogResult(boolean canceled) {
            Log.i(TAG, "onDialogResult, cancel2");
        }

        @Override
        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
            if (!isPreview && mCameraHelper.isCameraOpened2()) {
                mCameraHelper.startPreview2(mUVCCameraView2);
                isPreview = true;
                Log.i(TAG, "onSurfaceCreated, onSurfaceCreated2");
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
            if (isPreview && mCameraHelper.isCameraOpened2()) {
                mCameraHelper.stopPreview();
                Log.i(TAG, "onSurfaceDestroy2");
                isPreview = false;
            }
        }
    }
}
