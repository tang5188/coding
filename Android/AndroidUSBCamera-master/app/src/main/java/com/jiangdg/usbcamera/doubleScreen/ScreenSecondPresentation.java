package com.jiangdg.usbcamera.doubleScreen;

import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jiangdg.usbcamera.R;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.util.List;

import static com.serenegiant.utils.UIThreadHelper.runOnUiThread;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ScreenSecondPresentation extends Presentation {

    private static final String TAG = "ScreenSecond";

    private boolean isPreviewSecond;
    private TextView tvCameraSecond;
    private UVCCameraTextureView mTextureSecond;
    private UVCCamera uvcCameraSecond;

    public ScreenSecondPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_screen_second);

        tvCameraSecond = (TextView) findViewById(R.id.tv_camera_second);
        mTextureSecond = (UVCCameraTextureView) findViewById(R.id.camera_second);
        uvcCameraSecond = new UVCCamera();

        ((Switch) findViewById(R.id.switch_second)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uvcCameraSecond.setPreviewTexture(mTextureSecond.getSurfaceTexture());
                    uvcCameraSecond.startPreview();
                } else {
                    uvcCameraSecond.stopPreview();
                }
            }
        });
    }

    public void onDestroy() {
        try {
            if (uvcCameraSecond != null) {
                uvcCameraSecond.stopPreview();
                uvcCameraSecond.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsPreviewSecond() {
        return isPreviewSecond;
    }

    public void setIsPreviewSecond(boolean value) {
        this.isPreviewSecond = value;
    }

    public void setTvCameraText(String text) {
        tvCameraSecond.setText(text);
    }

    public void startSecondCamera(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
        new Thread(new ScreenSecondPresentation.myRunnable(device, ctrlBlock)).start();
    }

    class myRunnable implements Runnable {
        private UsbDevice device;
        private USBMonitor.UsbControlBlock usbControlBlock;
        private UVCCamera camera;

        public myRunnable(UsbDevice device, USBMonitor.UsbControlBlock usbControlBlock) {
            this.device = device;
            this.usbControlBlock = usbControlBlock;
            this.camera = uvcCameraSecond;
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
            if (usbControlBlock.getProductName().contains(ScreenMainActivity.CameraNameSecond)) {
                camera.setPreviewTexture(mTextureSecond.getSurfaceTexture());
                isPreviewSecond = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCameraSecond.setText(device.getProductName() + ", " + camera.getDeviceName());
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

            if (device.getProductName().equals(ScreenMainActivity.CameraNameSecond)) {
                camera.setPreviewSize(1280, 720, 1, 31, UVCCamera.FRAME_FORMAT_MJPEG, 0.5f);
                Log.d(TAG, "**设置参数成功2=FRAME_FORMAT_MJPEG, " + device.getProductName() + ", " + camera.getDeviceName());
            }
        } catch (final IllegalArgumentException e) {
            Log.d(TAG, "**设置参数失败=" + device.getProductName() + ", " + camera.getDeviceName());
            e.printStackTrace();
            return;
        }
    }
}
