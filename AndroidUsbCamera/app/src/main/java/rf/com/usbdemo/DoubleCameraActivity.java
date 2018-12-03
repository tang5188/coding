package rf.com.usbdemo;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.widget.CameraViewInterface;

import butterknife.BindView;
import butterknife.ButterKnife;
import rf.com.usbdemo.helper.UVCDoubleCameraHelper;

public class DoubleCameraActivity extends Activity {

    private static final String TAG = "DoubleCameraActivity";

    private boolean isRequestPermission;
    private boolean isPreview;

    private UVCDoubleCameraHelper mCameraHelper;

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
        setContentView(R.layout.activity_double_camera);
        ButterKnife.bind(this);

        mUVCCameraView1 = (CameraViewInterface) mTextureView1;
        mUVCCameraView2 = (CameraViewInterface) mTextureView2;

        mCameraHelper = UVCDoubleCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCDoubleCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView1, mUVCCameraView2, listener1);

        sCameraConnect1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCameraHelper.startPreview1(mUVCCameraView1);
                } else {
                    mCameraHelper.stopPreview1();
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

    private UVCDoubleCameraHelper.OnMyDevConnectListener listener1 = new UVCDoubleCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(final UsbDevice usbDevice) {
        }

        @Override
        public void onDettachDev(final UsbDevice usbDevice) {
            //更新画面
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (usbDevice.getProductName().contains(UVCDoubleCameraHelper.CameraName1)) {
                        tvCamera1.setText("");
                    } else if (usbDevice.getProductName().contains(UVCDoubleCameraHelper.CameraName2)) {
                        tvCamera2.setText("");
                    }
                }
            });
        }

        @Override
        public void onConnectDev(final UsbDevice usbDevice, boolean isConnected) {
            //更新画面
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (usbDevice.getProductName().contains(UVCDoubleCameraHelper.CameraName1)) {
                        tvCamera1.setText(usbDevice.getProductName());
                    } else if (usbDevice.getProductName().contains(UVCDoubleCameraHelper.CameraName2)) {
                        tvCamera2.setText(usbDevice.getProductName());
                    }
                }
            });
        }

        @Override
        public void onDisConnectDev(UsbDevice usbDevice) {

        }
    };
}
