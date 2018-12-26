package com.tang.mimaandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kmy.CEPP_Dri;

public class MainActivity extends Activity implements View.OnClickListener {

    private final String portName = "/dev/ttyO2";
    private final long baudRate = 9600;

    private Button btnOpenDevice, btnCloseDevice, btnInitEpp, btnGetVersion, btnEnterInput, btnExitInput;
    private TextView tvDeviceMsg, tvSerialIdMsg, tvVersionMsg, tvInputMsg, tvInputContent;

    private static CEPP_Dri m_sEPP = null;

    static {
        System.loadLibrary("EPP_API");
        m_sEPP = new CEPP_Dri();
    }

    //串口是否打开标记
    private boolean openFlag = false;
    //创建线程
    private boolean mRunning = false;
    private Handler hHandler;
    private HandlerThread hThread;
    //等待输入轮数
    private int MaxWaitRound = 3;
    //输入的字符拼接
    private String mInputContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenDevice = findViewById(R.id.btn_open_device);
        btnCloseDevice = findViewById(R.id.btn_close_device);
        btnInitEpp = findViewById(R.id.btn_init_epp);
        btnGetVersion = findViewById(R.id.btn_get_version);
        btnEnterInput = findViewById(R.id.btn_enter_input);
        btnExitInput = findViewById(R.id.btn_exit_input);
        tvDeviceMsg = findViewById(R.id.tv_device_msg);
        tvSerialIdMsg = findViewById(R.id.tv_serial_id_msg);
        tvVersionMsg = findViewById(R.id.tv_version_msg);
        tvInputMsg = findViewById(R.id.tv_input_msg);
        tvInputContent = findViewById(R.id.tv_input_content);

        btnOpenDevice.setOnClickListener(this);
        btnCloseDevice.setOnClickListener(this);
        btnInitEpp.setOnClickListener(this);
        btnGetVersion.setOnClickListener(this);
        btnEnterInput.setOnClickListener(this);
        btnExitInput.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_device:
                OpenDevice();
                break;
            case R.id.btn_close_device:
                CloseDevice();
                break;
            case R.id.btn_init_epp:
                InitEpp();
                break;
            case R.id.btn_get_version:
                GetVersion();
                break;
            case R.id.btn_enter_input:
                EnterInput();
                break;
            case R.id.btn_exit_input:
                ExitInput(false);
                break;
        }
    }

    //打开串口
    private void OpenDevice() {
        int ret = m_sEPP.EPP_OpenDevice(portName, baudRate);
        openFlag = ret >= 0;
        tvDeviceMsg.setText("打开串口:" + (openFlag ? "成功" : "失败"));
    }

    //关闭串口
    private void CloseDevice() {
        if (!openFlag) return;
        m_sEPP.EPP_CloseDevice();
        openFlag = false;
        tvDeviceMsg.setText("关闭串口成功");
    }

    //键盘复位
    private void InitEpp() {
        if (!openFlag) return;

        byte type = 0x00;
        m_sEPP.EPP_InitEPP(type);
    }

    //获取版本信息
    private void GetVersion() {
        if (!openFlag) return;

        byte[] SerialId = new byte[255];
        int ret1 = m_sEPP.EPP_GetSerialId(SerialId);
        String serialId = new String(SerialId);

        byte[] Version = new byte[255];
        int ret2 = m_sEPP.EPP_GetVersion(Version);
        String version = new String(Version);

        tvSerialIdMsg.setText(ret1 >= 0 ? serialId : "serialId失败");
        tvVersionMsg.setText(ret2 >= 0 ? version : "version失败");
    }

    //开始输入
    private void EnterInput() {
        if (!openFlag) return;
        tvInputMsg.setText("");
        byte bResult = 0x01;
        int ret = m_sEPP.EPP_UseEppPlainTextMode(bResult);
        tvInputMsg.setText("打开键盘:" + (ret >= 0 ? "成功" : "失败"));
        if (ret >= 0) {
            mInputContent = "";
            mRunning = true;
            hThread = new HandlerThread("MyHandlerThread");
            hThread.start();
            hHandler = new Handler(hThread.getLooper());
            hHandler.post(mBackgroundRunnable);
        }
    }

    //停止输入
    private void ExitInput(boolean okFlag) {
        if (!openFlag) return;

        mRunning = false;
        int ret = m_sEPP.EPP_ExitInputMode();
        if (hThread != null) {
            hThread.quit();
            hThread = null;
        }
        tvInputMsg.setText("键盘已关闭");
        mHandler.removeCallbacks(mBackgroundRunnable);
    }

    private Runnable mBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] KeyValue = new byte[1];
            int ret = -1;
            boolean isContinue = true;
            int curRound = 0;
            while (true) {
                if (!mRunning) break;
                ret = m_sEPP.EPP_GetKey(KeyValue);
                if (!mRunning) break;
                if (ret == 0 && KeyValue[0] >= 0) {
                    curRound = 0;
                    //接收输入数据
                    isContinue = receiveValue(KeyValue[0]);
                } else {
                    curRound++;
                    //超时退出输入模式
                    if (curRound > MaxWaitRound) {
                        isContinue = receiveValue((byte) 0xFF);
                    }
                }
                if (!isContinue) break;
            }
        }
    };

    //接收到键盘输入
    private boolean receiveValue(byte value) {
        Message message = new Message();
        message.what = 0;
        if (value == (byte) 0xFF) {            //超时
            message.what = 1;
        } else if (value == (byte) 0x1B) {     //退出
            message.what = 2;
        } else if (value == (byte) 0x0D) {     //确定
            message.what = 3;
        } else if (value == (byte) 0x08) {     //退格
            if (mInputContent.length() > 0) {
                mInputContent = mInputContent.substring(0, mInputContent.length() - 1);
            }
        } else if (value >= (byte) 0x30 && value <= (byte) 0x39) {    //0~9
            mInputContent += (char) value;
        }
        mHandler.sendMessage(message);
        boolean isContinue = (message.what == 0);
        return isContinue;
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ExitInput(false);
                    break;
                case 2:
                    ExitInput(false);
                    break;
                case 3:
                    ExitInput(true);
                    break;
                case 0:
                default:
                    tvInputContent.setText(mInputContent);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
