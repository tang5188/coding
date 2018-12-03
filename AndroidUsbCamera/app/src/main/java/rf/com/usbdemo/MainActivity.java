package rf.com.usbdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rf.com.usbdemo.utils.ByteUtil;
import rf.com.usbdemo.utils.CRC16;
import rf.com.usbdemo.utils.TcpManager;

public class MainActivity extends Activity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    private static final String TAG = "MainActivity";

    @BindView(R.id.camera_view)
    public View mTextureView;
    @BindView(R.id.seekbar_brightness)
    public SeekBar mSeekBrightness;
    @BindView(R.id.seekbar_contrast)
    public SeekBar mSeekContrast;
    @BindView(R.id.switch_rec_voice)
    public Switch mSwitchVoice;

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AlertDialog mDialog;

    private boolean isRequest;
    private boolean isPreview;

    /**
     * 服务器地址
     */
    private String pUsername = "XZY";
    /**
     * 服务器地址
     */
    private String serverUrl = "192.168.0.112";
    /**
     * 服务器端口
     */
    private int serverPort = 8888;

    private TcpManager manager;//zjh add
    private int m_DefaultframeLen = 1024;
    /**
     * 是否发送视频
     */
    private boolean startSendVideo = false;
    /**
     * 是否连接主机
     */
    private boolean connectedServer = false;

    private Button myBtn01, myBtn02;

    /**
     * 视频刷新间隔
     */
    private int VideoPreRate = 1;
    /**
     * 当前视频序号
     */
    private int tempPreRate = 0;

    /**
     * 发送视频宽度
     */
    private int VideoWidth = 320;
    /**
     * 发送视频高度
     */
    private int VideoHeight = 240;
    /**
     * 视频格式索引
     */
    private int VideoFormatIndex = 0;

    /**
     * 发送视频宽度比例
     */
    private float VideoWidthRatio = 1;
    /**
     * 发送视频高度比例
     */
    private float VideoHeightRatio = 1;
    /**
     * 视频质量
     */
    private int VideoQuality = 85;

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                showShortMsg("check no usb camera");
                return;
            }
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
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
                            mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                            mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);   //zjh dele
        initView();

        manager = TcpManager.getInstance();
        manager.connect(mHandler);

        // step.1 initialize UVCCameraHelper
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);


        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                //zjh receive data
            }
        });


        myBtn01 = (Button) findViewById(R.id.button1);
        myBtn02 = (Button) findViewById(R.id.button2);


        //开始连接主机按钮
        myBtn01.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });


        myBtn02.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String picPath = "/storage/sdcard0/DCIM/Camera/IMG_20170525_132208.jpg";
                try {
                    InputStream in = new FileInputStream(picPath);
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    int no = 0;
                    int frameLen = GetFileSize(picPath);
                    Log.e("frameLen", "frameLen" + frameLen);
                    while ((len = in.read(buffer)) != -1) {
                        Log.e("len", "len" + len);
                        byte[] _command = new byte[]{0x54};
                        byte[] _inOrOut = new byte[]{0x00};
                        SendPhoto(len + 1 + 13, _command, frameLen, no, len + 1, _inOrOut, buffer);
                        no++;
                    }
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showShortMsg("send photo ok");
            }
        });
    }

    private void initView() {
        //setSupportActionBar(mToolbar);  //zjh dele

        mSeekBrightness.setMax(100);
        mSeekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    mCameraHelper.setModelValue(UVCCameraHelper.MODE_BRIGHTNESS, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekContrast.setMax(100);
        mSeekContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    mCameraHelper.setModelValue(UVCCameraHelper.MODE_CONTRAST, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_takepic:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                String picPath = UVCCameraHelper.ROOT_PATH + System.currentTimeMillis()
                        + UVCCameraHelper.SUFFIX_JPEG;
                mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                    @Override
                    public void onCaptureResult(String path) {
                        Log.i(TAG, "save path：" + path);

                        try {
                            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qt);
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                            outstream.flush();
                            //启用线程将图像数据发送出去
                            Thread th = new MySendFileThread(outstream, pUsername, serverUrl, serverPort);
                            th.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        showShortMsg("send photo ok");
                    }
                });

                break;
            case R.id.menu_recording:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                if (!mCameraHelper.isPushing()) {
                    String videoPath = UVCCameraHelper.ROOT_PATH + System.currentTimeMillis();
                    FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264");
                    // if you want to record,please create RecordParams like this
                    RecordParams params = new RecordParams();
                    params.setRecordPath(videoPath);
                    params.setRecordDuration(0);                        // 设置为0，不分割保存
                    params.setVoiceClose(mSwitchVoice.isChecked());    // is close voice
                    mCameraHelper.startPusher(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                        @Override
                        public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                            // type = 1,h264 video stream
                            if (type == 1) {
//                                Toast.makeText(getApplicationContext(), "onEncodeResult",
//                                                Toast.LENGTH_SHORT).show();
                                FileUtils.putFileStream(data, offset, length);
                            }
                            // type = 0,aac audio stream
                            if (type == 0) {

                            }
                        }

                        @Override
                        public void onRecordResult(String videoPath) {
                            Log.i(TAG, "videoPath = " + videoPath);
                        }
                    });
                    // if you only want to push stream,please call like this
                    // mCameraHelper.startPusher(listener);
                    showShortMsg("start record...");
                    mSwitchVoice.setEnabled(false);
                } else {
                    FileUtils.releaseFile();
                    mCameraHelper.stopPusher();
                    showShortMsg("stop record...");
                    mSwitchVoice.setEnabled(true);
                }
                break;
            case R.id.menu_resolution:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                showResolutionListDialog();
                break;
            case R.id.menu_focus:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                mCameraHelper.startCameraFoucs();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResolutionList());
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened())
                    return;
                final String resolution = (String) adapterView.getItemAtPosition(position);
                String[] tmp = resolution.split("x");
                if (tmp != null && tmp.length >= 2) {
                    int widht = Integer.valueOf(tmp[0]);
                    int height = Integer.valueOf(tmp[1]);
                    mCameraHelper.updateResolution(widht, height);
                }
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
    }

    // example: {640x480,320x240,etc}
    private List<String> getResolutionList() {
        List<Size> list = mCameraHelper.getSupportedPreviewSizes();
        List<String> resolutions = null;
        if (list != null && list.size() != 0) {
            resolutions = new ArrayList<>();
            for (Size size : list) {
                if (size != null) {
                    resolutions.add(size.width + "x" + size.height);
                }
            }
        }
        return resolutions;
    }

    @Override
    protected void onDestroy() {

        manager.disConnect(); //zjh add
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
        Log.i(TAG, "Surface.onDialogResult");
    }

    public boolean isCameraOpened() {
        Log.i(TAG, "Surface.isCameraOpened");
        return mCameraHelper.isCameraOpened();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
        Log.i(TAG, "onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
        Log.i(TAG, "onSurfaceDestroy");
    }


    /**
     * 发送命令线程
     */
    class MySendCommondThread extends Thread {
        private String commond;

        public MySendCommondThread(String commond) {
            this.commond = commond;
        }

        public void run() {
            //实例化Socket
            try {
                Socket socket = new Socket(serverUrl, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(commond);
                out.flush();
                Log.e("com.xzy.run", commond);
            } catch (UnknownHostException e) {
            } catch (IOException e) {
                Log.e("com.xzy.run", e.getMessage());
            }
        }
    }


    /**
     * 发送文件线程
     */
    class MySendFileThread extends Thread {
        private String username;
        private String ipname;
        private int port;
        private byte byteBuffer[] = new byte[1024];
        private OutputStream outsocket;
        private ByteArrayOutputStream myoutputstream;

        public MySendFileThread(ByteArrayOutputStream myoutputstream, String username, String ipname, int port) {
            this.myoutputstream = myoutputstream;
            this.username = username;
            this.ipname = ipname;
            this.port = port;
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                //将图像数据通过Socket发送出去
                Socket tempSocket = new Socket(ipname, port);
                outsocket = tempSocket.getOutputStream();
                //写入头部数据信息
                String msg = java.net.URLEncoder.encode("PHONEVIDEO|" + username + "|", "utf-8");
                byte[] buffer = msg.getBytes();
                outsocket.write(buffer);

                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int amount;
                while ((amount = inputstream.read(byteBuffer)) != -1) {
                    outsocket.write(byteBuffer, 0, amount);
                }
                myoutputstream.flush();
                myoutputstream.close();
                tempSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //===zjh add ============
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TcpManager.STATE_FROM_SERVER_OK:
                    int result = (int) msg.obj;
                    Toast.makeText(getApplicationContext(), "" + result, Toast.LENGTH_SHORT).show();
                    break;
                case TcpManager.PHOTO_Mode:
                    int moderet = (int) msg.obj;
                    Toast.makeText(getApplicationContext(), "" + moderet, Toast.LENGTH_SHORT).show();
                    RecPhotoMode();
                    break;
                case TcpManager.PHOTO_INV:
                    int invret = (int) msg.obj;
                    Toast.makeText(getApplicationContext(), "" + invret, Toast.LENGTH_SHORT).show();
                    RecTakePhotoInv();
                    break;
                case TcpManager.PHOTO_SIZE:
                    int size = (int) msg.obj;
                    Toast.makeText(getApplicationContext(), "" + size, Toast.LENGTH_SHORT).show();
                    RecTakePhotoInv();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    //拍照模式
    private void GetPhotoMode() {
        //49 4D 41 47 45 0D 00 00 00 52 01 00 00 00 00 00 00 00 0A 0D 5C A9
        byte[] msg = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45, 0x0D, 0x00, 0x00, 0x00, 0x52, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x0D, 0x5c, (byte) 0xA9};
        manager.sendMessage(msg);
    }

    private void RecPhotoMode() {
        //49 4D 41 47 45 0E 00 00 00 52 01 00 00 00 01 00 00 00 01 0A 0D 78 7E
        byte[] _msg = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45, 0x0E, 0x00, 0x00, 0x00, 0x52, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x0A, 0x0D, 0x78, 0x7E};
        manager.sendMessage(_msg);
    }

    //获取拍照时间间隔
    private void GetTakePhotoInv() {
        //49 4D 41 47 45 0D 00 00 00 51 01 00 00 00 00 00 00 00 0A 0D 18 A6
        byte[] _msg = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45, 0x0D, 0x00, 0x00, 0x00, 0x51, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x0D, 0x18, (byte) 0xA6};
        manager.sendMessage(_msg);
    }

    private void RecTakePhotoInv() {
        //49 4D 41 47 45 0E 00 00 00 51 01 00 00 00 01 00 00 00 01 0A 0D 78 7E
        byte[] _msg = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45, 0x0E, 0x00, 0x00, 0x00, 0x51, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x0A, 0x0D, 0x7C, 0x7A};
        manager.sendMessage(_msg);
    }

    private void GetTakePhotoSize() {
        //49 4D 41 47 45 0D 00 00 00 53 01 00 00 00 00 00 00 00 0A 0D A0 AD
        byte[] _msg = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45, 0x0D, 0x00, 0x00, 0x00, 0x53, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x0D, (byte) 0xA0, (byte) 0xAD};
        manager.sendMessage(_msg);
    }

    private void RecTakePhotoSize() {
        //49 4D 41 47 45 0E 00 00 00 53 01 00 00 00 01 00 00 00 01 0A 0D BB 83
        byte[] _msg = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45, 0x0E, 0x00, 0x00, 0x00, 0x53, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x0A, 0x0D, (byte) 0xBB, (byte) 0x83};
        manager.sendMessage(_msg);
    }

    private void SendPhoto(int frameLen, byte[] command, int frameNum, int frameNo, int contentLen, byte[] inOrOut, byte[] content) {
        byte[] _head = new byte[]{0x49, 0x4D, 0x41, 0x47, 0x45};
        byte[] _frameLen = GetByte4(frameLen);
        byte[] _frameNum = GetByte2(frameNum);
        byte[] _frameNo = GetByte2(frameNo);
        byte[] _contentLen = GetByte4(contentLen);
        byte[] _frameTail = new byte[]{0x0A, 0x0D};
        byte[] _sendMsg = ByteUtil.addBytes(_head, _frameLen);
        _sendMsg = ByteUtil.addBytes(_sendMsg, command);
        _sendMsg = ByteUtil.addBytes(_sendMsg, _frameNum);
        _sendMsg = ByteUtil.addBytes(_sendMsg, _frameNo);
        _sendMsg = ByteUtil.addBytes(_sendMsg, _contentLen);
        _sendMsg = ByteUtil.addBytes(_sendMsg, inOrOut);
        _sendMsg = ByteUtil.addBytes(_sendMsg, content);
        _sendMsg = ByteUtil.addBytes(_sendMsg, _frameTail);
        byte[] crc4 = ByteUtil.intToByteArray(CRC16.calcCrc16(_sendMsg));
        byte[] _crc2 = new byte[2];
        _crc2[0] = crc4[2];
        _crc2[1] = crc4[3];
        //Log.e("crc" , ByteUtil.byteArrToHexString(_crc2));
        _sendMsg = ByteUtil.addBytes(_sendMsg, _crc2);
        manager.sendMessage(_sendMsg);
        //Log.e("SendPhoto" , ByteUtil.byteArrToHexString(_sendMsg));
    }

    private byte[] GetByte4(int intByte) {
        byte[] _GetByte4 = new byte[]{0x00, 0x00, 0x00, 0x00};
        byte[] _intTobyte4 = ByteUtil.intToByteArray(intByte);
        _GetByte4[0] = _intTobyte4[3];
        _GetByte4[1] = _intTobyte4[2];
        _GetByte4[2] = _intTobyte4[1];
        _GetByte4[3] = _intTobyte4[0];
        return _GetByte4;
    }

    private byte[] GetByte2(int intByte) {
        byte[] _GetByte2 = new byte[]{0x00, 0x00};
        byte[] _intTobyte2 = ByteUtil.intToByteArray(intByte);
        _GetByte2[0] = _intTobyte2[3];
        _GetByte2[1] = _intTobyte2[2];
        return _GetByte2;
    }

    int CRC16_Check(byte Pushdata[], int length) {
        int Reg_CRC = 0xffff;
        int temp;
        int i, j;

        for (i = 0; i < length; i++) {
            temp = Pushdata[i];
            if (temp < 0) temp += 256;
            temp &= 0xff;
            Reg_CRC ^= temp;

            for (j = 0; j < 8; j++) {
                if ((Reg_CRC & 0x0001) == 0x0001)
                    Reg_CRC = (Reg_CRC >> 1) ^ 0xA001;
                else
                    Reg_CRC >>= 1;
            }
        }
        return (Reg_CRC & 0xffff);
    }

    private int GetFileSize(String filePath) {
        int ret = 0;
        File f = new File(filePath);
        if (f.exists() && f.isFile()) {
            Long fileLen = f.length();
            ret = Integer.parseInt(String.valueOf(fileLen)) / 1024;
            if (Integer.parseInt(String.valueOf(fileLen)) % 1024 > 0)
                ret = ret + 1;
        } else {
            ret = 0;

        }
        return ret;
    }
}
