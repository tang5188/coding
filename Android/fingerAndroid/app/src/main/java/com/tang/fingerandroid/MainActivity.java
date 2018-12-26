package com.tang.fingerandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.za.finger.ZAandroid;

/**
 * 指昂指纹设备接口测试
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    private Button btnOpenDevice, btnCloseDevice, btnRegistFinger, btnSearchFinger, btnDownChar1, btnDownChar2, btnReadCount, btnClearAll, btnClearSpec;
    private TextView tvDeviceMsg, tvRegistMsg, tvSearchMsg, tvDownMsg, tvCharCountMsg, tvFingerCharMsg;
    private EditText etSpecPageID;

    private ZAandroid a6 = new ZAandroid();
    //设备地址
    private int DEV_ADDR = 0xffffffff;
    //同参数：（0:256x288 1:256x360）
    private int IMG_SIZE = 0;

    private int defDeviceType = 11;
    private int defiCom = 32;
    private int defiBaud = 12;

    //指纹模式是否正常打开
    private boolean isOpen = false;

    private long sStart = System.currentTimeMillis();
    private long sEnd = System.currentTimeMillis();

    private boolean fpRegist = false;   //正在注册指纹的标志：true非注册模式，false注册模式
    private boolean fpSearch = false;   //正在搜索指纹的标志：true非搜索模式，false搜索模式
    private HandlerThread mThread;
    private Handler objHandler_fp;

    private int iPageID = 0;
    private int iBufferID = 1;
    private byte[] pTemplateBase = new byte[2304];
    private boolean isFingerOn = false;
    private int testCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mThread = new HandlerThread("MyHandlerThread");
        mThread.start();
        objHandler_fp = new Handler();

        btnOpenDevice = findViewById(R.id.btn_open_device);
        btnCloseDevice = findViewById(R.id.btn_close_device);
        btnRegistFinger = findViewById(R.id.btn_regist_finger);
        btnSearchFinger = findViewById(R.id.btn_search_finger);
        btnDownChar1 = findViewById(R.id.btn_down_char1);
        btnDownChar2 = findViewById(R.id.btn_down_char2);
        btnReadCount = findViewById(R.id.btn_read_count);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnClearSpec = findViewById(R.id.btn_clear_spec);
        tvDeviceMsg = findViewById(R.id.tv_device_msg);
        tvRegistMsg = findViewById(R.id.tv_regist_msg);
        tvSearchMsg = findViewById(R.id.tv_search_msg);
        tvDownMsg = findViewById(R.id.tv_down_msg);
        tvCharCountMsg = findViewById(R.id.tv_char_count_msg);
        tvFingerCharMsg = findViewById(R.id.tv_finger_char_msg);
        etSpecPageID = findViewById(R.id.et_spec_page_id);

        btnOpenDevice.setOnClickListener(this);
        btnCloseDevice.setOnClickListener(this);
        btnRegistFinger.setOnClickListener(this);
        btnSearchFinger.setOnClickListener(this);
        btnDownChar1.setOnClickListener(this);
        btnDownChar2.setOnClickListener(this);
        btnReadCount.setOnClickListener(this);
        btnClearAll.setOnClickListener(this);
        btnClearSpec.setOnClickListener(this);
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
            case R.id.btn_regist_finger:
                RegistFinger();
                break;
            case R.id.btn_search_finger:
                SearchFinger();
                break;
            case R.id.btn_down_char1:
                DownChar(1);
                break;
            case R.id.btn_down_char2:
                DownChar(2);
                break;
            case R.id.btn_read_count:
                ReadValidFingerCount();
                break;
            case R.id.btn_clear_all:
                ClearAll();
                break;
            case R.id.btn_clear_spec:
                ClearSpec();
                break;
        }
    }

    //打开设备
    private void OpenDevice() {
        if (isOpen) return;

        int ret = a6.ZAZOpenDeviceEx(0, defDeviceType, defiCom, defiBaud, 2, 0);
        Log.i(TAG, "ZAZOpenDeviceEx:" + defDeviceType + "," + defiCom + "," + defiBaud + "," + ret);

        if (ret == 1) {
            int ret2 = a6.ZAZSetImageSize(IMG_SIZE);
            Log.i(TAG, "ZAZSetImageSize:" + ret2);

            byte[] pVersion = new byte[512];
            int ret3 = a6.ZAZReadInfPage(DEV_ADDR, pVersion);
            Log.i(TAG, "ZAZReadInfPage:" + ret3 + "," + (new String(pVersion)));
            //指纹模块正常打开
            if (ret2 == 0 && ret3 == 0) {
                isOpen = true;
            }
        }

        if (isOpen) {
            tvDeviceMsg.setText("打开设备成功");
        } else {
            tvDeviceMsg.setText("打开设备失败");
        }
    }

    //关闭设备
    private void CloseDevice() {
        if (!isOpen) return;
        setFlag(true);
        int ret = a6.ZAZCloseDeviceEx();
        Log.i(TAG, "ZAZCloseDeviceEx:" + ret);
        isOpen = false;
        tvDeviceMsg.setText("指纹模块已关闭");
    }

    // region 注册指纹
    //注册指纹
    private void RegistFinger() {
        if (!isOpen) return;

        setFlag(true);
        waitSleep(500);
        fpRegist = false;
        objHandler_fp.removeCallbacks(fpRegistTasks);
        objHandler_fp.removeCallbacks(fpSearchTasks);

        sStart = System.currentTimeMillis();
        sEnd = System.currentTimeMillis();
        iBufferID = 1;
        isFingerOn = false;
        testCount = 0;
        objHandler_fp.postDelayed(fpRegistTasks, 0);
    }

    private Runnable fpRegistTasks = new Runnable() {
        @Override
        public void run() {
            sEnd = System.currentTimeMillis();
            //注册超时
            if (sEnd - sStart > 10000) {
                tvRegistMsg.setText("读取指纹等待超时【已终止】");
                return;
            }
            //注册停止
            if (fpRegist) {
                tvRegistMsg.setText("注册主动停止【已终止】");
                return;
            }

            int nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testCount = 0;
                waitSleep(200);
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }
            Log.i(TAG, "ZAZGetImage:" + nRet);
            if (nRet == a6.PS_OK) {
                if (isFingerOn) {
                    tvRegistMsg.setText("请重按手指...");
                    sStart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpRegistTasks, 100);
                    return;
                }
                //获取指纹特征
                nRet = a6.ZAZGenChar(DEV_ADDR, iBufferID);
                Log.i(TAG, "ZAZGenChar:" + nRet);
                if (nRet == a6.PS_OK) {
                    int[] id_iscore = new int[2];
                    //搜索指纹，避免指纹重复注册
                    nRet = a6.ZAZHighSpeedSearch(DEV_ADDR, iBufferID, 0, 1000, id_iscore);
                    Log.i(TAG, "ZAZHighSpeedSearch:" + nRet);
                    if (nRet == a6.PS_OK) {
                        tvRegistMsg.setText("指纹已注册，无需重复注册。ID:" + id_iscore[0] + " 【已终止】");
                        DispFingerCharByPageID(id_iscore[0], iBufferID);
                    } else {
                        iBufferID++;
                        isFingerOn = true;
                        //已经达到2次
                        if (iBufferID > 2) {
                            //0x01,0x02两次模板特征合成
                            nRet = a6.ZAZRegModule(DEV_ADDR);
                            Log.i(TAG, "ZAZRegModule:" + nRet);
                            if (nRet != a6.PS_OK) {
                                tvRegistMsg.setText("合成模板失败，请重新注册指纹【已终止】");
                            } else {
                                //将指纹特征保存
                                nRet = a6.ZAZStoreChar(DEV_ADDR, 1, iPageID);
                                Log.i(TAG, "ZAZStoreChar:" + nRet);
                                if (nRet == a6.PS_OK) {
                                    tvRegistMsg.setText("注册指纹成功！！！ ID:" + iPageID);
                                    iPageID++;
                                    DispFingerCharByBufferID(1);
                                } else {
                                    tvRegistMsg.setText("注册指纹失败【已终止】");
                                }
                            }
                        } else {
                            tvRegistMsg.setText("本次获取指纹成功...");
                            sStart = System.currentTimeMillis();
                            objHandler_fp.postDelayed(fpRegistTasks, 500);
                        }
                    }
                } else {
                    tvRegistMsg.setText("特征太差，请重新录入...");
                    sStart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpRegistTasks, 1000);
                }
            } else if (nRet == a6.PS_NO_FINGER) {
                isFingerOn = false;
                tvRegistMsg.setText(iBufferID + "正在读取指纹中...剩余时间：" + ((10000 - sEnd + sStart) / 1000 + "s"));
                objHandler_fp.postDelayed(fpRegistTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                tvRegistMsg.setText("图像获取中...");
                objHandler_fp.postDelayed(fpRegistTasks, 10);
            } else if (nRet == -2) {
                testCount++;
                if (testCount < 3) {
                    isFingerOn = false;
                    tvRegistMsg.setText(iBufferID + "正在读取指纹中...剩余时间：" + ((10000 - sEnd + sStart) / 1000 + "s"));
                    objHandler_fp.postDelayed(fpRegistTasks, 10);
                } else {
                    tvRegistMsg.setText("通讯异常【已终止】");
                }
            } else {
                tvRegistMsg.setText("通讯异常【已终止】");
            }
        }
    };
    // endregion

    // region 搜索指纹
    private void SearchFinger() {
        if (!isOpen) return;

        setFlag(true);
        waitSleep(500);
        fpSearch = false;
        objHandler_fp.removeCallbacks(fpRegistTasks);
        objHandler_fp.removeCallbacks(fpSearchTasks);

        sStart = System.currentTimeMillis();
        sEnd = System.currentTimeMillis();
        iBufferID = 1;
        testCount = 0;
        objHandler_fp.postDelayed(fpSearchTasks, 0);
    }

    private Runnable fpSearchTasks = new Runnable() {
        @Override
        public void run() {
            sEnd = System.currentTimeMillis();
            //搜索超时
            if (sEnd - sStart > 10000) {
                tvSearchMsg.setText("读取指纹等待超时【已终止】");
                return;
            }
            //搜索停止
            if (fpSearch) {
                tvSearchMsg.setText("搜索主动停止【已终止】");
                return;
            }

            int nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testCount = 0;
                waitSleep(200);
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }
            Log.i(TAG, "ZAZGetImage:" + nRet);
            if (nRet == a6.PS_OK) {
                nRet = a6.ZAZGenChar(DEV_ADDR, iBufferID);
                Log.i(TAG, "ZAZGenChar:" + nRet);
                if (nRet == a6.PS_OK) {
                    int[] id_iscore = new int[2];
                    nRet = a6.ZAZHighSpeedSearch(DEV_ADDR, iBufferID, 0, 1000, id_iscore);
                    Log.i(TAG, "ZAZHighSpeedSearch:" + nRet);
                    if (nRet == a6.PS_OK) {
                        tvSearchMsg.setText("搜索指纹成功！！！ID:" + id_iscore[0]);
                        //查询到模块中的指纹特征值，将其下载到2号缓冲区，并展示
                        DispFingerCharByPageID(id_iscore[0], 2);
                    } else {
                        tvSearchMsg.setText("搜索指纹失败【已终止】");
                        //未搜索到模块中的指纹特征值，展示当前指纹特征值
                        DispFingerCharByBufferID(1);
                    }
                } else {
                    tvSearchMsg.setText("特征太差，请重新录入...");
                    sStart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpSearchTasks, 1000);
                }
            } else if (nRet == a6.PS_NO_FINGER) {
                tvSearchMsg.setText("正在读取指纹中...剩余时间：" + ((10000 - sEnd + sStart) / 1000 + "s"));
                objHandler_fp.postDelayed(fpSearchTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                tvSearchMsg.setText("图像获取中...");
                objHandler_fp.postDelayed(fpSearchTasks, 10);
            } else if (nRet == -2) {
                testCount++;
                if (testCount < 3) {
                    isFingerOn = false;
                    tvRegistMsg.setText(iBufferID + "正在读取指纹中...剩余时间：" + ((10000 - sEnd + sStart) / 1000 + "s"));
                    objHandler_fp.postDelayed(fpSearchTasks, 10);
                } else {
                    tvRegistMsg.setText("通讯异常【已终止】");
                }
            } else {
                tvRegistMsg.setText("通讯异常【已终止】");
            }
        }
    };
    // endregion

    // region 下载指纹

    private int specPageID1 = 101;
    private int specPageID2 = 102;
    //测试使用的指纹特征值字符串
    private String testFingerChars1 = "03015C149200E000C000C0008000800000000000000000000000000000000000000000000000000200000000000000000000000000000000538D5AFE289404FE4795DA7E3B995AFE1C999C5E22A65A7E09A8841E45A8D89E0A30DB7E4333D77E66B3D57E2234993E6839AB7E233A573E57BBD57E3EC215BE55891C9F3FBD165F1D0AC614220D9C940000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000301651A9000F800F800E000C000C00080008000000000000000000000000000800080008000C002000000000000000000000000000000003F8C5A1E338FD9DE7294547E7419EA1E1C1E59BE401E97BE60A7D4BE3EA996DE1FAC57DE20B215FE27BBD51E1F4093FE35C2139E612C6B1F55AF151F3C32D55F75BDD3773CB7D49C543A52FC3EBD951C4FBDA93C3C0659FA38BCAB3A3887C27B210F8358218DAB73000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private String testFingerChars2 = "03015A1D8A000000000000000000000000000000000000000000000000000000000000008000C000401000000000000000000000000000001A0B5D5E3D8EEBFE6990669E0E9BC43E589C6B3E3E1D40BE2A24427E702E96BE453001BE503557FE3338427E433CC1BE35C2431E12909C5F4E946A7F6E9DA9FF231ED9DF652E2CDF15B7033F58A9D7BA5DAC2C7A67BE6B1A6A4018FA5024ACD850A780785A3ED73853A2D6B95A3CC119440966B6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000030158208C00C00080008000800080000000000000000000000000000000000000000000800080000000000000000000000000000000000017075F5E3E87A23E3112DDBE5497ABBE2423449E6D256BBE532600BE3EAC829E5838C1DE5FBE58FE44C042BE18C284BE5B071D3F2C09A01F218BDEDF29181C7F641D6A9F37A6DA7F26BE03BF6E09E17C748A62BC119285BC70B52C9C6C32979A158CC67B128EDDD84A05C6D346858916580ADD965F0D6256653080765A11A6B4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

    private void DownChar(int bufferID) {
        if (!isOpen) return;

        setFlag(true);
        waitSleep(500);
        objHandler_fp.removeCallbacks(fpRegistTasks);
        objHandler_fp.removeCallbacks(fpSearchTasks);

        int specPageID = bufferID == 1 ? specPageID1 : specPageID2;
        String testFingerChars = bufferID == 1 ? testFingerChars1 : testFingerChars2;
        byte[] pTemplate = hexStringToByteArr(testFingerChars);
        sStart = System.currentTimeMillis();
        //下载特征值到2号缓冲区
        Log.i(TAG, pTemplateBase.length + "---" + byteArrToHexString(pTemplate, pTemplate.length));
        int nRet = a6.ZAZDownChar(DEV_ADDR, 2, pTemplate, pTemplate.length);
        Log.i(TAG, bufferID + "_ZAZDownChar:" + nRet);
        sEnd = System.currentTimeMillis();

        if (nRet == a6.PS_OK) {
            String msg = "";

            int[] id_iscore = new int[2];
            //搜索指纹，避免指纹重复注册
            nRet = a6.ZAZHighSpeedSearch(DEV_ADDR, 2, 0, 1000, id_iscore);
            Log.i(TAG, "ZAZHighSpeedSearch:" + nRet);
            if (nRet == 0) {
                msg += "指纹已存在:" + id_iscore[0] + " ";
            }

            //保存特征码到特定号
            nRet = a6.ZAZStoreChar(DEV_ADDR, 2, specPageID);
            Log.i(TAG, bufferID + "_ZAZStoreChar:" + nRet);
            if (nRet == a6.PS_OK) {
                tvDownMsg.setText(msg + "下载指纹成功→" + specPageID);
            } else {
                tvDownMsg.setText(msg + "下载指纹失败");
            }
        } else {
            tvDownMsg.setText(bufferID + "_ZAZDownChar下载指纹失败:" + nRet);
        }
    }

    // endregion

    // region 获取数量、清空指纹等
    //获取指纹有效数量
    private void ReadValidFingerCount() {
        if (!isOpen) return;
        setFlag(true);
        int[] iMbNum = new int[2];
        int nRet = a6.ZAZTemplateNum(DEV_ADDR, iMbNum);
        if (nRet == a6.PS_OK) {
            tvCharCountMsg.setText(iMbNum[0] + "个");
        } else {
            tvCharCountMsg.setText("-1个");
        }
    }

    //清空全部指纹
    private void ClearAll() {
        if (!isOpen) return;
        setFlag(true);
        int nRet = a6.ZAZEmpty(DEV_ADDR);
        if (nRet == a6.PS_OK) {
            tvCharCountMsg.setText("清空成功");
        } else {
            tvCharCountMsg.setText("清空失败");
        }
    }

    //删除指定指纹
    private void ClearSpec() {
        if (!isOpen) return;
        String sPageID = etSpecPageID.getText().toString();
        if (TextUtils.isEmpty(sPageID)) return;

        setFlag(true);
        int iStartPageID = Integer.parseInt(sPageID);
        int iDelPageNum = 1;
        int nRet = a6.ZAZDelChar(DEV_ADDR, iStartPageID, iDelPageNum);
        if (nRet == a6.PS_OK) {
            tvCharCountMsg.setText("删除成功:" + iStartPageID);
        } else {
            tvCharCountMsg.setText("删除失败:" + iStartPageID);
        }
    }
    // endregion

    // region 辅助方法

    //显示指定模块号的指纹特征
    private void DispFingerCharByPageID(int pageID, int bufferID) {
        int nRet = a6.ZAZLoadChar(DEV_ADDR, 2, pageID);
        Log.i(TAG, "ZAZLoadChar:" + nRet);
        if (nRet == a6.PS_OK) {
            //展示模块中匹配到的指纹特征值
            DispFingerCharByBufferID(bufferID);
        }
    }

    //显示指纹特征值
    private void DispFingerCharByBufferID(int bufferID) {
        int[] iTemplateLength = new int[1];
        int nRet = a6.ZAZUpChar(DEV_ADDR, bufferID, pTemplateBase, iTemplateLength);
        Log.i(TAG, "ZAZUpChar:" + nRet + ", length:" + iTemplateLength[0]);
        if (nRet == a6.PS_OK) {
            String fingerChar = byteArrToHexString(pTemplateBase, iTemplateLength[0]);
            tvFingerCharMsg.setText("当前指纹特征值：" + fingerChar);
        }
    }

    //停止各项工作
    private void setFlag(boolean flag) {
        fpRegist = flag;
        fpSearch = flag;
        tvRegistMsg.setText("");
        tvSearchMsg.setText("");
        tvDownMsg.setText("");
        tvCharCountMsg.setText("");
        tvFingerCharMsg.setText("");
    }

    //执行等待
    private void waitSleep(long millis) {
        try {
            mThread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //byte数组转16进制字符串
    private String byteArrToHexString(byte[] bytes, int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bytes.length && i < length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            buffer.append(hex.toUpperCase());
        }
        Log.i(TAG, "指纹特征值：" + buffer.toString());
        return buffer.toString();
    }

    //16进制字符串转byte数组
    public static byte[] hexStringToByteArr(String hexStr) {
        int length = hexStr.length();
        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length / 2; i++) {
            String subStr = hexStr.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) (Integer.parseInt(subStr, 16) & 0xFF);
        }
        return bytes;
    }
    // endregion
}
