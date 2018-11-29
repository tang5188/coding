package rf.com.usbdemo;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.widget.CameraViewInterface;

import butterknife.BindView;
import butterknife.ButterKnife;
import rf.com.usbdemo.helper.UVCCameraHelper1;
import rf.com.usbdemo.helper.UVCCameraHelper2;

public class DoubleUsbCameraActivity extends Activity {

    private static final String TAG = "DoubleUsbCameraActivity";

    private boolean isRequestPermission;
    private boolean isPreview;

    @BindView(R.id.btn_camera_connect1)
    public Button btnCameraConnect1;
    @BindView(R.id.tvCamera1)
    public TextView tvCamera1;
    @BindView(R.id.camera_view1)
    public View mTextureView1;
    private UVCCameraHelper1 mCameraHelper1;
    private CameraViewInterface mUVCCameraView1;

    @BindView(R.id.btn_camera_connect2)
    public Button btnCameraConnect2;
    @BindView(R.id.tvCamera2)
    public TextView tvCamera2;
    @BindView(R.id.camera_view2)
    public View mTextureView2;
    private UVCCameraHelper2 mCameraHelper2;
    private CameraViewInterface mUVCCameraView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_usb_camera);
        ButterKnife.bind(this);

        mUVCCameraView1 = (CameraViewInterface) mTextureView1;
        mUVCCameraView1.setCallback(new CameraViewCallback1());
        mCameraHelper1 = UVCCameraHelper1.getInstance();
        mCameraHelper1.setDefaultFrameFormat(UVCCameraHelper1.FRAME_FORMAT_YUYV);
        mCameraHelper1.initUSBMonitor(this, mUVCCameraView1, listener1);
        btnCameraConnect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //0未连接 1连接中 2已连接
                int tag2 = Integer.parseInt(btnCameraConnect2.getTag().toString());
                if (tag2 == 1) return;
                //mCameraHelper2.unregisterUSB();

                int tag1 = Integer.parseInt(btnCameraConnect1.getTag().toString());
                if (tag1 == 0) {
                    mCameraHelper1.registerUSB();
                    btnCameraConnect1.setText("断开1");
                    btnCameraConnect1.setTag(1);
                    tvCamera1.setText("");
                } else if (tag1 == 2) {
                    //mCameraHelper1.unregisterUSB();
                    btnCameraConnect1.setText("连接1");
                    btnCameraConnect1.setTag(0);
                }
            }
        });

        mUVCCameraView2 = (CameraViewInterface) mTextureView2;
        mUVCCameraView2.setCallback(new CameraViewCallback2());
        mCameraHelper2 = UVCCameraHelper2.getInstance();
        mCameraHelper2.setDefaultFrameFormat(UVCCameraHelper2.FRAME_FORMAT_YUYV);
        mCameraHelper2.initUSBMonitor(this, mUVCCameraView2, listener2);
        btnCameraConnect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //0未连接 1连接中 2已连接
                int tag1 = Integer.parseInt(btnCameraConnect1.getTag().toString());
                if (tag1 == 1) return;
                //mCameraHelper1.unregisterUSB();

                int tag2 = Integer.parseInt(btnCameraConnect2.getTag().toString());
                if (tag2 == 0) {
                    mCameraHelper2.registerUSB();
                    btnCameraConnect2.setText("断开2");
                    btnCameraConnect2.setTag(1);
                    tvCamera2.setText("");
                } else if (tag2 == 2) {
                    //mCameraHelper2.unregisterUSB();
                    btnCameraConnect2.setText("连接2");
                    btnCameraConnect2.setTag(0);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (mCameraHelper1 != null) {
//            mCameraHelper1.registerUSB();
//        }
//        if (mCameraHelper2 != null) {
//            mCameraHelper2.registerUSB();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraHelper1 != null) {
            mCameraHelper1.unregisterUSB();
        }
        if (mCameraHelper2 != null) {
            mCameraHelper2.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private UVCCameraHelper1.OnMyDevConnectListener listener1 = new UVCCameraHelper1.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice usbDevice) {
            if (mCameraHelper1 == null || mCameraHelper1.getUsbDeviceCount() == 0) {
                Log.i(TAG, "onAttachDev, check no usb camera1");
                return;
            }
            Log.i(TAG, "onAttachDev1, " + usbDevice.getDeviceName());
            if (!isRequestPermission) {
                isRequestPermission = true;
                if (mCameraHelper1 != null) {
                    //更新画面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnCameraConnect1.setText("连接中1");
                            btnCameraConnect1.setTag(1);
                        }
                    });
                    mCameraHelper1.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice usbDevice) {
            if (isRequestPermission) {
                isRequestPermission = false;
                mCameraHelper1.closeCamera();
                //更新画面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnCameraConnect1.setText("已断开1");
                        btnCameraConnect1.setTag(0);
                        tvCamera1.setText("");
                    }
                });
            }
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
                        btnCameraConnect1.setText("已连接1");
                        btnCameraConnect1.setTag(2);
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
                        if (mCameraHelper1 != null && mCameraHelper1.isCameraOpened()) {
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
            return mCameraHelper1.getUSBMonitor();
        }

        @Override
        public void onDialogResult(boolean canceled) {
            Log.i(TAG, "onDialogResult, cancel1");
        }

        @Override
        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
            if (!isPreview && mCameraHelper1.isCameraOpened()) {
                mCameraHelper1.startPreview(mUVCCameraView1);
                isPreview = true;
                Log.i(TAG, "onSurfaceCreated, onSurfaceCreated1");
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

    private UVCCameraHelper2.OnMyDevConnectListener listener2 = new UVCCameraHelper2.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice usbDevice) {
            if (mCameraHelper2 == null || mCameraHelper2.getUsbDeviceCount() == 0) {
                Log.i(TAG, "onAttachDev, check no usb camera2");
                return;
            }
            Log.i(TAG, "onAttachDev2, " + usbDevice.getDeviceName());
            if (!isRequestPermission) {
                isRequestPermission = true;
                if (mCameraHelper2 != null) {
                    //更新画面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnCameraConnect2.setText("连接中2");
                            btnCameraConnect2.setTag(1);
                        }
                    });
                    mCameraHelper2.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice usbDevice) {
            if (isRequestPermission) {
                isRequestPermission = false;
                mCameraHelper2.closeCamera();
                //更新画面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnCameraConnect2.setText("已断开2");
                        btnCameraConnect2.setTag(0);
                        tvCamera2.setText("");
                    }
                });
            }
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
                        btnCameraConnect2.setText("已连接2");
                        btnCameraConnect2.setTag(2);
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
                        if (mCameraHelper2 != null && mCameraHelper2.isCameraOpened()) {
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
            return mCameraHelper2.getUSBMonitor();
        }

        @Override
        public void onDialogResult(boolean canceled) {
            Log.i(TAG, "onDialogResult, cancel2");
        }

        @Override
        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
            if (!isPreview && mCameraHelper2.isCameraOpened()) {
                mCameraHelper2.startPreview(mUVCCameraView2);
                isPreview = true;
                Log.i(TAG, "onSurfaceCreated, onSurfaceCreated2");
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
            if (isPreview && mCameraHelper2.isCameraOpened()) {
                mCameraHelper2.stopPreview();
                isPreview = false;
            }
        }
    }
}
