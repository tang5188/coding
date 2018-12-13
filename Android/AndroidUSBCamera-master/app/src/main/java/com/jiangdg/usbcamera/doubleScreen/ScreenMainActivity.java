package com.jiangdg.usbcamera.doubleScreen;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.service.carrier.CarrierService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jiangdg.usbcamera.R;
import com.jiangdg.usbcamera.application.MyApplication;
import com.jiangdg.usbcamera.view.USBCameraDoubleActivity;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.util.List;

public class ScreenMainActivity extends AppCompatActivity {

    private Display externalDisplay;
    private ScreenSecondPresentation presentation;

    private static final String TAG = "ScreenMainActivity";
    public static final String CameraNameMain = "USB 2.0 PC Camera";
    public static final String CameraNameSecond = "USB 2.0 Camera";

    private TextView tvCameraMain;
    private UVCCameraTextureView mTextureMain;

    private USBMonitor usbMonitor;
    private UVCCamera uvcCameraMain;
    private boolean isPreviewMain;

    private int timer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main);
        //显示副屏
        showSecondScreen();

        tvCameraMain = (TextView) findViewById(R.id.tv_camera_main);
        mTextureMain = (UVCCameraTextureView) findViewById(R.id.camera_main);
        uvcCameraMain = new UVCCamera();
        usbMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(final UsbDevice device) {
                String productName = device.getProductName();
                Log.d(TAG, "onAttach:==" + productName + ", " + device.getDeviceName());
                if (device.getDeviceClass() != 239 || device.getDeviceSubclass() != 2) {
                    return;
                }
                if (!productName.contains(CameraNameMain) && !productName.contains(CameraNameSecond)) {
                    return;
                }
                if (productName.contains(CameraNameMain) && isPreviewMain) {
                    return;
                }
                if (productName.contains(CameraNameSecond) && presentation.getIsPreviewSecond()) {
                    return;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "requestPermission:" + device.getProductName() + ", " + device.getDeviceName());
                        timer++;
                        usbMonitor.requestPermission(device);
                    }
                }, timer * 500);
            }

            @Override
            public void onDettach(UsbDevice device) {

            }

            @Override
            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                String productName = device.getProductName();
                if (!productName.contains(CameraNameMain) && !productName.contains(CameraNameSecond)) {
                    return;
                }
                if (productName.contains(CameraNameMain) && isPreviewMain) {
                    return;
                }
                if (productName.contains(CameraNameSecond) && presentation.getIsPreviewSecond()) {
                    return;
                }
                if (productName.contains(CameraNameMain)) {
                    new Thread(new ScreenMainActivity.myRunnable(device, ctrlBlock)).start();
                } else if (productName.contains(CameraNameSecond)) {
                    presentation.startSecondCamera(device, ctrlBlock);
                }
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                String productName = device.getProductName();
                Log.d(TAG, "onDisconnect," + productName + ", " + device.getDeviceName());
                if (productName.contains(CameraNameMain)) {
                    isPreviewMain = false;
                    tvCameraMain.setText("");
                } else if (productName.contains(CameraNameSecond)) {
                    presentation.setIsPreviewSecond(false);
                    presentation.setTvCameraText("");
                }
            }

            @Override
            public void onCancel(UsbDevice device) {

            }
        });
        usbMonitor.setDeviceFilter(new DeviceFilter(-1, -1, 239, 2, -1, null, null, null));
        usbMonitor.register();

        ((Switch) findViewById(R.id.switch_main)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uvcCameraMain.setPreviewTexture(mTextureMain.getSurfaceTexture());
                    uvcCameraMain.startPreview();
                } else {
                    uvcCameraMain.stopPreview();
                }
            }
        });
    }

    //副屏显示
    private void showSecondScreen() {
        externalDisplay = MyApplication.getInstance().externalDisplay;
        if (externalDisplay != null) {
            this.presentation = new ScreenSecondPresentation(ScreenMainActivity.this, externalDisplay);
            this.presentation.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            usbMonitor.unregister();
            if (uvcCameraMain != null) {
                uvcCameraMain.stopPreview();
                uvcCameraMain.close();
            }
            if(presentation!=null){
                presentation.onDestroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class myRunnable implements Runnable {
        private UsbDevice device;
        private USBMonitor.UsbControlBlock usbControlBlock;
        private UVCCamera camera;

        public myRunnable(UsbDevice device, USBMonitor.UsbControlBlock usbControlBlock) {
            this.device = device;
            this.usbControlBlock = usbControlBlock;
            this.camera = uvcCameraMain;
        }

        @Override
        public void run() {
            try {
                camera.open(usbControlBlock);
            } catch (Exception e) {
                Log.d(TAG, "开启相机错误！！！！" + device.getProductName() + ", " + camera.getDeviceName());
                e.printStackTrace();
                return;
            }

            List<Size> supportedSizeList = camera.getSupportedSizeList();
            if (supportedSizeList != null) {
                for (Size size : supportedSizeList) {
                    Log.d(TAG, "run: size=" + size.width + "---" + size.height);
                }
            }

            setCameraParameter(device, camera);
            //根据不同相机接入name  或者根据pid vid 指定相机在那个view显示
            if (usbControlBlock.getProductName().contains(CameraNameMain)) {
                camera.setPreviewTexture(mTextureMain.getSurfaceTexture());
                isPreviewMain = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCameraMain.setText(device.getProductName() + ", " + camera.getDeviceName());
                    }
                });
            }
            camera.startPreview();
        }
    }

    //相机参数  可根据不同相机设置不同参数，如果一个OTG接入多个相机 需注意带宽和帧率设置，否则出现带宽不够导致相机不显示数据
    private void setCameraParameter(UsbDevice device, UVCCamera camera) {
        try {
            //设置预览尺寸 根据设备自行设置
            //UVCCamera.FRAME_FORMAT_YUYV
            //UVCCamera.FRAME_FORMAT_MJPEG, //此格式设置15帧生效

            if (device.getProductName().equals(CameraNameMain)) {
                camera.setPreviewSize(1280, 1024, 1, 31, UVCCamera.FRAME_FORMAT_MJPEG, 0.5f);
                Log.d(TAG, "**设置参数成功1=FRAME_FORMAT_MJPEG, " + device.getProductName() + ", " + camera.getDeviceName());
            }
        } catch (final IllegalArgumentException e) {
            Log.d(TAG, "**设置参数失败=" + device.getProductName() + ", " + camera.getDeviceName());
            e.printStackTrace();
            return;
        }
    }
}
