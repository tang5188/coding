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

    private static final String CameraL = "USB 2.0 PC Camera";
    private static final String CameraR = "USB 2.0 Camera";

    private static final boolean DEBUG = false;    // FIXME set false when production
    private static final String TAG = "MainActivity";

    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

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
    private Handler hander = new Handler();
    private int timer = 0;

    private Switch s_camera_L, s_camera_R;
    private TextView tv_camera_name_L, tv_camera_name_R;

    private DeviceFilter deviceFilter = null;
    private int widthR = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int heightR = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int widthL = UVCCamera.DEFAULT_PREVIEW_WIDTH;
    private int heightL = UVCCamera.DEFAULT_PREVIEW_WIDTH;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        widthL = 1280;
        heightL = 1024;
        widthR = 1280;
        heightR = 720;

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

        deviceFilter = new DeviceFilter(-1, -1, 239, 2, -1, null, null, null);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.setDeviceFilter(deviceFilter);

        ((UVCCameraTextureView) mUVCCameraViewL).setRotation(90);
        ((UVCCameraTextureView) mUVCCameraViewR).setRotation(90);
        s_camera_L = (Switch) findViewById(R.id.s_camera_L);
        s_camera_R = (Switch) findViewById(R.id.s_camera_R);
        tv_camera_name_L = (TextView) findViewById(R.id.tv_camera_name_L);
        tv_camera_name_R = (TextView) findViewById(R.id.tv_camera_name_R);
        s_camera_L.setOnCheckedChangeListener(this.checkedChangeListenerL);
        s_camera_R.setOnCheckedChangeListener(this.checkedChangeListenerR);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onResume();
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onResume();
    }

    @Override
    protected void onStop() {
        mHandlerR.close();
        if (mUVCCameraViewR != null)
            mUVCCameraViewR.onPause();
        mHandlerL.close();
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        if (mUVCCameraViewL != null)
            mUVCCameraViewL.onPause();
        mCaptureButtonL.setVisibility(View.INVISIBLE);
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
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewR = null;
        mCaptureButtonR = null;
        mUVCCameraViewL = null;
        mCaptureButtonL = null;
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
            if (mHandlerL == null) return;
            if (mHandlerL.isRecording()) return;
            if (isChecked) {
                if (!mHandlerL.isOpened()) {
                    List<UsbDevice> devices = mUSBMonitor.getDeviceList(deviceFilter);
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getProductName().equals(CameraL)) {
                            UsbControlBlock controlBlock = mUSBMonitor.openDevice(devices.get(i));
                            mHandlerL.open(controlBlock);
                            final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();
                            mHandlerL.startPreview(new Surface(st));
                            break;
                        }
                    }
                } else if (mHandlerL.isOpened() && !mHandlerL.isPreviewing()) {
                    final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();
                    mHandlerL.startPreview(new Surface(st));
                }
            } else if (!isChecked && mHandlerL.isPreviewing()) {
                mHandlerL.stopPreview();
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkedChangeListenerR = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mHandlerR == null) return;
            if (mHandlerR.isRecording()) return;
            if (isChecked) {
                if (!mHandlerR.isOpened()) {
                    List<UsbDevice> devices = mUSBMonitor.getDeviceList(deviceFilter);
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getProductName().equals(CameraR)) {
                            UsbControlBlock controlBlock = mUSBMonitor.openDevice(devices.get(i));
                            mHandlerR.open(controlBlock);
                            final SurfaceTexture st = mUVCCameraViewR.getSurfaceTexture();
                            mHandlerR.startPreview(new Surface(st));
                            break;
                        }
                    }
                } else if (mHandlerR.isOpened() && !mHandlerR.isPreviewing()) {
                    final SurfaceTexture st = mUVCCameraViewR.getSurfaceTexture();
                    mHandlerR.startPreview(new Surface(st));
                }
            } else if (!isChecked && mHandlerR.isPreviewing()) {
                mHandlerR.stopPreview();
            }
        }
    };
    // endregion

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:" + device);
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {//根据相机信息选择选需要打开的相机
                hander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        timer++;
                        mUSBMonitor.requestPermission(device);
                    }
                }, timer * 200);
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:" + device);
            if (device.getProductName().equals(CameraL) && !mHandlerL.isOpened()) {
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
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device);
            if ((mHandlerL != null) && !mHandlerL.isEqual(device)) {
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
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device);
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:");
        }
    };

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
            }
        }, 0);
    }
}
