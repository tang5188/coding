package rf.com.uvccamera;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.List;

public final class CameraOneActivity extends BaseActivity implements CameraDialog.CameraDialogParent {

    private static final String TAG = "CameraOneActivity";

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private UVCCameraHandler mHandlerL;
    private UVCCameraTextureView mUVCCameraViewL;
    private ImageButton mCaptureButtonL;
    private Surface mPreviewSurfaceL;

    private Handler hander = new Handler();
    private int timer = 0;

    private Switch s_camera_L;
    private TextView tv_camera_name_L;

    private DeviceFilter deviceFilter = null;
    private static final float[] BANDWIDTH_FACTORS = {0.3f};

    private int widthL = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int heightL = UVCCamera.DEFAULT_PREVIEW_WIDTH;

    private static final String CameraL = "USB 2.0 PC Camera";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_one);

        widthL = 1280;
        heightL = 1024;

        findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
        mUVCCameraViewL = findViewById(R.id.camera_view_L);
        mUVCCameraViewL.setAspectRatio(widthL / (float) heightL);
        mUVCCameraViewL.setOnClickListener(mOnClickListener);
        mCaptureButtonL = (ImageButton) findViewById(R.id.capture_button_L);
        mCaptureButtonL.setOnClickListener(mOnClickListener);
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mHandlerL = UVCCameraHandler.createHandler(this, mUVCCameraViewL, widthL, heightL, BANDWIDTH_FACTORS[0]);

        deviceFilter = new DeviceFilter(-1, -1, 239, 2, -1, null, null, null);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.setDeviceFilter(deviceFilter);

        s_camera_L = (Switch) findViewById(R.id.s_camera_L);
        tv_camera_name_L = (TextView) findViewById(R.id.tv_camera_name_L);
        s_camera_L.setOnCheckedChangeListener(this.checkedChangeListenerL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onResume();
    }

    @Override
    protected void onStop() {
        mHandlerL.close();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onPause();
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerL != null) {
            mHandlerL = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewL = null;
        super.onDestroy();
    }

    // region View event
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view_L:
                    if (mHandlerL != null) {
                        if (!mHandlerL.isOpened()) {
                            CameraDialog.showDialog(CameraOneActivity.this);
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
                                //1.录像
//                                if (!mHandlerL.isRecording()) {
//                                    mCaptureButtonL.setColorFilter(0xffff0000);    // turn red
//                                    mHandlerL.startRecording();
//                                } else {
//                                    mCaptureButtonL.setColorFilter(0);    // return to default color
//                                    mHandlerL.stopRecording();
//                                }
                                //2.拍照
                                mHandlerL.captureStill();
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
                        USBMonitor.UsbControlBlock controlBlock = mUSBMonitor.openDevice(device);
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

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (device.getProductName().equals(CameraL)) {
                Log.v(TAG, "-----onAttachL:" + device.getProductName());
            } else {
                Log.v(TAG, device + "");
            }

            Toast.makeText(CameraOneActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {//根据相机信息选择选需要打开的相机
                requestPermission(device);
            }
        }

        private String devNameL = "";

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (device.getProductName().equals(CameraL) && !mHandlerL.isOpened()) {
                Log.v(TAG, "-----onConnectL:" + device.getProductName());
                mHandlerL.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();
                mHandlerL.startPreview(new Surface(st));
                timer = 0;
                devNameL = device.getDeviceName();
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
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if ((mHandlerL != null) && !mHandlerL.isEqual(device)) {
                Log.v(TAG, "-----onDisconnectL:" + device.getProductName());
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerL.close();
                        if (mPreviewSurfaceL != null) {
                            mPreviewSurfaceL.release();
                            mPreviewSurfaceL = null;
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
            }
            Toast.makeText(CameraOneActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (device.getProductName().equals(CameraL)) {
                Log.v(TAG, "-----onCancelL:" + device.getProductName());
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
            }
        }, 0);
    }
}
