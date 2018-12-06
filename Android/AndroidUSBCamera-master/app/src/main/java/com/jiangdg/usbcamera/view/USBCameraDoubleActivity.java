package com.jiangdg.usbcamera.view;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jiangdg.usbcamera.R;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.util.List;

public class USBCameraDoubleActivity extends AppCompatActivity {

    private static final String TAG = "USBCameraDoubleActivity";

    public static final String CameraName1 = "USB 2.0 Camera";
    public static final String CameraName2 = "USB 2.0 PC Camera";

    private TextView tvCamera1, tvCamera2;
    private UVCCameraTextureView mTexture1, mTexture2;

    private USBMonitor usbMonitor;
    private UVCCamera uvcCamera1, uvcCamera2;
    private boolean isPreview1, isPreview2;

    private int time;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera_double);

        tvCamera1 = (TextView) findViewById(R.id.tvCamera1);
        tvCamera2 = (TextView) findViewById(R.id.tvCamera2);
        mTexture1 = (UVCCameraTextureView) findViewById(R.id.camera_view1);
        mTexture2 = (UVCCameraTextureView) findViewById(R.id.camera_view2);

        uvcCamera1 = new UVCCamera();
        uvcCamera2 = new UVCCamera();

        usbMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
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
                                usbMonitor.requestPermission(device);
                            }
                        }, time++ * 500);
                    }
                }
            }

            @Override
            public void onDettach(UsbDevice device) {

            }

            @Override
            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                if (device.getProductName().contains(CameraName1) ||
                        device.getProductName().contains(CameraName2)) {

                    if (device.getProductName().contains(CameraName1) && isPreview1) return;
                    if (device.getProductName().contains(CameraName2) && isPreview2) return;

                    new Thread(new myRunnable(device, ctrlBlock)).start();
                }
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                Log.d(TAG, "onDisconnect," + device.getProductName() + ", " + device.getDeviceName());
                if (device.getProductName().contains(CameraName1)) {
                    isPreview1 = false;
                    tvCamera1.setText("");
                } else if (device.getProductName().contains(CameraName2)) {
                    isPreview2 = false;
                    tvCamera2.setText("");
                }
            }

            @Override
            public void onCancel(UsbDevice device) {

            }
        });
        usbMonitor.register();

        ((Switch) findViewById(R.id.switch1)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    uvcCamera1.setPreviewTexture(mTexture1.getSurfaceTexture());
                    uvcCamera1.startPreview();
                } else {
                    uvcCamera1.stopPreview();
                }
            }
        });
        ((Switch) findViewById(R.id.switch2)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    uvcCamera2.setPreviewTexture(mTexture2.getSurfaceTexture());
                    uvcCamera2.startPreview();
                } else {
                    uvcCamera2.stopPreview();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            usbMonitor.unregister();
            if (uvcCamera1 != null) {
                uvcCamera1.stopPreview();
                uvcCamera1.close();
            }
            if (uvcCamera2 != null) {
                uvcCamera2.stopPreview();
                uvcCamera2.close();
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
        }

        @Override
        public void run() {
            if (usbControlBlock.getProductName().contains(CameraName1)) {
                camera = uvcCamera1;
            } else if (usbControlBlock.getProductName().contains(CameraName2)) {
                camera = uvcCamera2;
            }

            try {
                camera.open(usbControlBlock);
            } catch (Exception e) {
                Log.d(TAG, "开启相机错误！！！！" + device.getProductName() + ", " + camera.getDeviceName());
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
            if (usbControlBlock.getProductName().contains(CameraName1)) {
                camera.setPreviewTexture(mTexture1.getSurfaceTexture());
                isPreview1 = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCamera1.setText(device.getProductName() + ", " + camera.getDeviceName());
                    }
                });
            } else if (usbControlBlock.getProductName().contains(CameraName2)) {
                camera.setPreviewTexture(mTexture2.getSurfaceTexture());
                isPreview2 = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCamera2.setText(device.getProductName() + ", " + camera.getDeviceName());
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

            if (device.getProductName().equals(CameraName1)) {
                camera.setPreviewSize(640, 480, 1, 15, UVCCamera.FRAME_FORMAT_YUYV, 0.5f);
                Log.d(TAG, "**设置参数成功1=FRAME_FORMAT_MJPEG, " + device.getProductName() + ", " + camera.getDeviceName());
            } else if (device.getProductName().equals(CameraName2)) {
                camera.setPreviewSize(1280, 1024, 1, 15, UVCCamera.FRAME_FORMAT_YUYV, 0.5f);
                Log.d(TAG, "**设置参数成功2=FRAME_FORMAT_MJPEG, " + device.getProductName() + ", " + camera.getDeviceName());
            }
        } catch (final IllegalArgumentException e) {
            Log.d(TAG, "**设置参数失败=" + device.getProductName() + ", " + camera.getDeviceName());
            return;
        }
    }
}