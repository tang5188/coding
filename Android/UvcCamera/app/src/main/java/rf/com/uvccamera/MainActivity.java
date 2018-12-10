package rf.com.uvccamera;

import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.List;

/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */
public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent {

    private static final String TAG = "MainActivity";

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private UVCCameraHandler mHandlerR;
    private CameraViewInterface mUVCCameraViewR;
    private ImageButton mCaptureButtonR;
    private Surface mRightPreviewSurface;

    private UVCCameraHandler mHandlerL;
    private CameraViewInterface mUVCCameraViewL;
    private ImageButton mCaptureButtonL;
    private Surface mLeftPreviewSurface;

    private UVCCameraHandler mHandler3;
    private CameraViewInterface mUVCCameraView3;
    private ImageButton mCaptureButton3;
    private Surface m3PreviewSurface;

    private UVCCameraHandler mHandler4;
    private CameraViewInterface mUVCCameraView4;
    private ImageButton mCaptureButton4;
    private Surface m4PreviewSurface;

    private Handler hander = new Handler();
    private int timer = 0;

    private Switch s_camera_L, s_camera_R, s_camera_3, s_camera_4;
    private TextView tv_camera_name_L, tv_camera_name_R, tv_camera_name_3, tv_camera_name_4;

    private DeviceFilter deviceFilter = null;
    private static final float[] BANDWIDTH_FACTORS = {0.15f, 0.3f, 0.6f, 0.01f};

    private int widthR = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int heightR = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int widthL = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int heightL = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int width3 = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int height3 = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int width4 = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int height4 = UVCCamera.DEFAULT_PREVIEW_WIDTH;

    private static final String CameraL = "USB 2.0 PC Camera";
    private static final String CameraR = "USB 2.0 Camera";
    private static final String Camera3 = "XCX-S58M21";
    private static final String Camera4 = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        widthL = 160;
        heightL = 120;
        widthR = 320;
        heightR = 240;
        width3 = 640;
        height3 = 480;
        width4 = 640;
        height4 = 480;

        findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
        mUVCCameraViewL = (CameraViewInterface) findViewById(R.id.camera_view_L);
        mUVCCameraViewL.setAspectRatio(widthL / (float) heightL);
        ((UVCCameraTextureView) mUVCCameraViewL).setOnClickListener(mOnClickListener);
        mCaptureButtonL = (ImageButton) findViewById(R.id.capture_button_L);
        mCaptureButtonL.setOnClickListener(mOnClickListener);
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mHandlerL = UVCCameraHandler.createHandler(this, mUVCCameraViewL, widthL, heightL, BANDWIDTH_FACTORS[0]);

        mUVCCameraViewR = (CameraViewInterface) findViewById(R.id.camera_view_R);
        mUVCCameraViewR.setAspectRatio(widthR / (float) heightR);
        ((UVCCameraTextureView) mUVCCameraViewR).setOnClickListener(mOnClickListener);
        mCaptureButtonR = (ImageButton) findViewById(R.id.capture_button_R);
        mCaptureButtonR.setOnClickListener(mOnClickListener);
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        mHandlerR = UVCCameraHandler.createHandler(this, mUVCCameraViewR, widthR, heightR, BANDWIDTH_FACTORS[1]);

        mUVCCameraView3 = (CameraViewInterface) findViewById(R.id.camera_view_3);
        mUVCCameraView3.setAspectRatio(width3 / (float) height3);
        ((UVCCameraTextureView) mUVCCameraView3).setOnClickListener(mOnClickListener);
        mCaptureButton3 = (ImageButton) findViewById(R.id.capture_button_3);
        mCaptureButton3.setOnClickListener(mOnClickListener);
        mCaptureButton3.setVisibility(View.INVISIBLE);
        mHandler3 = UVCCameraHandler.createHandler(this, mUVCCameraView3, width3, height3, BANDWIDTH_FACTORS[2]);

        mUVCCameraView4 = (CameraViewInterface) findViewById(R.id.camera_view_4);
        mUVCCameraView4.setAspectRatio(width4 / (float) height4);
        ((UVCCameraTextureView) mUVCCameraView4).setOnClickListener(mOnClickListener);
        mCaptureButton4 = (ImageButton) findViewById(R.id.capture_button_4);
        mCaptureButton4.setOnClickListener(mOnClickListener);
        mCaptureButton4.setVisibility(View.INVISIBLE);
        mHandler4 = UVCCameraHandler.createHandler(this, mUVCCameraView4, width4, height4, BANDWIDTH_FACTORS[3]);

        deviceFilter = new DeviceFilter(-1, -1, 239, 2, -1, null, null, null);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.setDeviceFilter(deviceFilter);

//        ((UVCCameraTextureView) mUVCCameraViewL).setRotation(90);
//        ((UVCCameraTextureView) mUVCCameraViewR).setRotation(90);
        s_camera_L = (Switch) findViewById(R.id.s_camera_L);
        s_camera_R = (Switch) findViewById(R.id.s_camera_R);
        s_camera_3 = (Switch) findViewById(R.id.s_camera_3);
        s_camera_4 = (Switch) findViewById(R.id.s_camera_4);
        tv_camera_name_L = (TextView) findViewById(R.id.tv_camera_name_L);
        tv_camera_name_R = (TextView) findViewById(R.id.tv_camera_name_R);
        tv_camera_name_3 = (TextView) findViewById(R.id.tv_camera_name_3);
        tv_camera_name_4 = (TextView) findViewById(R.id.tv_camera_name_4);
        s_camera_L.setOnCheckedChangeListener(this.checkedChangeListenerL);
        s_camera_R.setOnCheckedChangeListener(this.checkedChangeListenerR);
        s_camera_3.setOnCheckedChangeListener(this.checkedChangeListener3);
        s_camera_4.setOnCheckedChangeListener(this.checkedChangeListener4);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onResume();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onResume();
        if (mUVCCameraView3 != null)
            mUVCCameraView3.onResume();
        if (mUVCCameraView4 != null)
            mUVCCameraView4.onResume();
    }

    @Override
    protected void onStop() {
        mHandlerR.close();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onPause();
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        mHandlerL.close();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onPause();
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mHandler3.close();
        if (mUVCCameraView3 != null)
            mUVCCameraView3.onPause();
        mCaptureButton3.setVisibility(View.INVISIBLE);
        mHandler4.close();
        if (mUVCCameraView4 != null)
            mUVCCameraView4.onPause();
        mCaptureButton4.setVisibility(View.INVISIBLE);
        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerR != null) {
            mHandlerR = null;
        }
        if (mHandlerL != null) {
            mHandlerL = null;
        }
        if (mHandler3 != null) {
            mHandler3 = null;
        }
        if (mHandler4 != null) {
            mHandler4 = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewR = null;
        mCaptureButtonR = null;
        mUVCCameraViewL = null;
        mCaptureButtonL = null;
        mUVCCameraView3 = null;
        mCaptureButton3 = null;
        mUVCCameraView4 = null;
        mCaptureButton4 = null;
        super.onDestroy();
    }

    // region View event
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view_L:
                    if (mHandlerL != null) {
                        if (!mHandlerL.isOpened()) {
                            CameraDialog.showDialog(MainActivity.this);
                        } else {
                            mHandlerL.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_L:
                    if (mHandlerL != null) {
                        if (mHandlerL.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerL.isRecording()) {
                                    mCaptureButtonL.setColorFilter(0xffff0000);    // turn red
                                    mHandlerL.startRecording();
                                } else {
                                    mCaptureButtonL.setColorFilter(0);    // return to default color
                                    mHandlerL.stopRecording();
                                }
                            }
                        }
                    }
                    break;
                case R.id.camera_view_R:
                    if (mHandlerR != null) {
                        if (!mHandlerR.isOpened()) {
                            CameraDialog.showDialog(MainActivity.this);
                        } else {
                            mHandlerR.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_R:
                    if (mHandlerR != null) {
                        if (mHandlerR.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerR.isRecording()) {
                                    mCaptureButtonR.setColorFilter(0xffff0000);    // turn red
                                    mHandlerR.startRecording();
                                } else {
                                    mCaptureButtonR.setColorFilter(0);    // return to default color
                                    mHandlerR.stopRecording();
                                }
                            }
                        }
                    }
                    break;
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkedChangeListenerL = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkedChanged(mHandlerL, mUVCCameraViewL, mCaptureButtonL, tv_camera_name_L, CameraL, isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkedChangeListenerR = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkedChanged(mHandlerR, mUVCCameraViewR, mCaptureButtonR, tv_camera_name_R, CameraR, isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener3 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkedChanged(mHandler3, mUVCCameraView3, mCaptureButton3, tv_camera_name_3, Camera3, isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener4 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkedChanged(mHandler4, mUVCCameraView4, mCaptureButton4, tv_camera_name_4, Camera4, isChecked);
        }
    };

    private final void checkedChanged(UVCCameraHandler ohandler, CameraViewInterface oUVCCameraView,
                                      final ImageButton oCaptureButton, final TextView oTvCameraName, String oCamera, boolean isChecked) {
        if (ohandler == null) return;
        if (ohandler.isRecording()) return;
        if (isChecked) {
            if (!ohandler.isOpened()) {
                List<UsbDevice> devices = mUSBMonitor.getDeviceList(deviceFilter);
                for (int i = 0; i < devices.size(); i++) {
                    final UsbDevice device = devices.get(i);
                    if (!device.getProductName().equals(oCamera)) continue;

                    if (!mUSBMonitor.hasPermission(device)) {
                        //没有权限时，申请权限，然后通过onConnect接口回调打开摄像头
                        timer = 0;
                        requestPermission(device);
                    } else {
                        //打开摄像头，进行预览
                        UsbControlBlock controlBlock = mUSBMonitor.openDevice(device);
                        ohandler.open(controlBlock);
                        final SurfaceTexture st = oUVCCameraView.getSurfaceTexture();
                        ohandler.startPreview(new Surface(st));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                oCaptureButton.setVisibility(View.VISIBLE);
                                oTvCameraName.setText(device.getProductName() + ", " + device.getDeviceName());
                            }
                        });
                    }
                    break;
                }
            } else if (ohandler.isOpened() && !ohandler.isPreviewing()) {
                final SurfaceTexture st = oUVCCameraView.getSurfaceTexture();
                ohandler.startPreview(new Surface(st));
            }
        } else if (!isChecked && ohandler.isPreviewing()) {
            ohandler.stopPreview();
        }
    }
    // endregion

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (device.getProductName().equals(CameraL)) {
                Log.v(TAG, "-----onAttachL:" + device.getProductName());
            } else if (device.getProductName().equals(CameraR)) {
                Log.v(TAG, "-----onAttachR:" + device.getProductName());
            } else if (device.getProductName().equals(Camera3)) {
                Log.v(TAG, "-----onAttach3:" + device.getProductName());
            } else {
                Log.v(TAG, device + "");
            }

            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {//根据相机信息选择选需要打开的相机
                requestPermission(device);
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (device.getProductName().equals(CameraL) && !mHandlerL.isOpened()) {
                Log.v(TAG, "-----onConnectL:" + device.getProductName());
                mHandlerL.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();
                mHandlerL.startPreview(new Surface(st));
                timer = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCaptureButtonL.setVisibility(View.VISIBLE);
                        tv_camera_name_L.setText(device.getProductName() + ", " + device.getDeviceName());
                        s_camera_L.setOnCheckedChangeListener(null);
                        s_camera_L.setChecked(true);
                        s_camera_L.setOnCheckedChangeListener(checkedChangeListenerL);
                    }
                });
            } else if (device.getProductName().equals(CameraR) && !mHandlerR.isOpened()) {
                Log.v(TAG, "-----onConnectR:" + device.getProductName());
                mHandlerR.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewR.getSurfaceTexture();
                mHandlerR.startPreview(new Surface(st));
                timer = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCaptureButtonR.setVisibility(View.VISIBLE);
                        tv_camera_name_R.setText(device.getProductName() + ", " + device.getDeviceName());
                        s_camera_R.setOnCheckedChangeListener(null);
                        s_camera_R.setChecked(true);
                        s_camera_R.setOnCheckedChangeListener(checkedChangeListenerR);
                    }
                });
            } else if (device.getProductName().equals(Camera3) && !mHandler3.isOpened()) {
                Log.v(TAG, "-----onConnect3:" + device.getProductName());
                mHandler3.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraView3.getSurfaceTexture();
                mHandler3.startPreview(new Surface(st));
                timer = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCaptureButton3.setVisibility(View.VISIBLE);
                        tv_camera_name_3.setText(device.getProductName() + ", " + device.getDeviceName());
                        s_camera_3.setOnCheckedChangeListener(null);
                        s_camera_3.setChecked(true);
                        s_camera_3.setOnCheckedChangeListener(checkedChangeListener3);
                    }
                });
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if ((mHandlerL != null) && !mHandlerL.isEqual(device)) {
                Log.v(TAG, "-----onDisconnectL:" + device.getProductName());
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerL.close();
                        if (mLeftPreviewSurface != null) {
                            mLeftPreviewSurface.release();
                            mLeftPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerR != null) && !mHandlerR.isEqual(device)) {
                Log.v(TAG, "-----onDisconnectR:" + device.getProductName());
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerR.close();
                        if (mRightPreviewSurface != null) {
                            mRightPreviewSurface.release();
                            mRightPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandler3 != null) && !mHandler3.isEqual(device)) {
                Log.v(TAG, "-----onDisconnect3:" + device.getProductName());
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandler3.close();
                        if (m3PreviewSurface != null) {
                            m3PreviewSurface.release();
                            m3PreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (device.getProductName().equals(CameraL)) {
                Log.v(TAG, "-----onDettachL:" + device.getProductName());
            } else if (device.getProductName().equals(CameraR)) {
                Log.v(TAG, "-----onDettachR:" + device.getProductName());
            }
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (device.getProductName().equals(CameraL)) {
                Log.v(TAG, "-----onCancelL:" + device.getProductName());
            } else if (device.getProductName().equals(CameraR)) {
                Log.v(TAG, "-----onCancelR:" + device.getProductName());
            } else if (device.getProductName().equals(Camera3)) {
                Log.v(TAG, "-----onCancel3:" + device.getProductName());
            }
        }
    };

    //申请权限
    private void requestPermission(final UsbDevice device) {
        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "requestPermission:" + device.getProductName());
                timer++;
                mUSBMonitor.requestPermission(device);
            }
        }, timer * 200);
    }

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCameraButton();
                }
            }, 0);
        }
    }

    private void setCameraButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mHandlerL != null) && !mHandlerL.isOpened() && (mCaptureButtonL != null)) {
                    mCaptureButtonL.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerR != null) && !mHandlerR.isOpened() && (mCaptureButtonR != null)) {
                    mCaptureButtonR.setVisibility(View.INVISIBLE);
                }
                if ((mHandler3 != null) && !mHandler3.isOpened() && (mCaptureButton3 != null)) {
                    mCaptureButton3.setVisibility(View.INVISIBLE);
                }
            }
        }, 0);
    }
}
