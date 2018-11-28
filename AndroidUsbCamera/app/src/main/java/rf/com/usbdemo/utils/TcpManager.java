package rf.com.usbdemo.utils;


import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2018/7/25.
 */

public class TcpManager {

    public static final int STATE_FROM_SERVER_OK = 0;
    public static final int PHOTO_Mode =1;
    public static final int PHOTO_INV =2;
    public static final int PHOTO_SIZE =3;
    private static String dsName = "192.168.1.108";
    private static int dstPort = 9987;
    private static Socket socket;

    private static TcpManager instance;

    private TcpManager() {
    };

    public static TcpManager getInstance() {
        if (instance == null) {
            synchronized (TcpManager.class) {
                if (instance == null) {
                    instance = new TcpManager();
                }
            }
        }
        return instance;
    }

    /**
     * 连接
     *
     * @return
     */
    public boolean connect(final Handler handler) {

        if (socket == null || socket.isClosed()) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        socket = new Socket(dsName, dstPort);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        throw new RuntimeException("连接错误: " + e.getMessage());
                    }

                    try {
                        // 输入流，为了获取客户端发送的数据
                        InputStream is = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = is.read(buffer)) != -1) {
                            //final String result = new String(buffer, 0, len);

                            if(buffer[9] == 0x52)//获取拍照模式
                            {
                                Message msg = Message.obtain();
                                msg.obj = ByteUtil.byteToInt(buffer[18]);
                                msg.what = PHOTO_Mode;
                                handler.sendMessage(msg);
                            }

                            if(buffer[9] == 0x51)//获取时间间隔
                            {
                                Message msg = Message.obtain();
                                int ldata = ByteUtil.byteToInt(buffer[18]);
                                int hdata = ByteUtil.byteToInt(buffer[19])*256;
                                msg.obj = hdata + ldata;
                                msg.what = PHOTO_INV;
                                handler.sendMessage(msg);
                            }


                            if(buffer[9] == 0x53)//获取FRAME SIZE
                            {
                                Message msg = Message.obtain();
                                int ldata = ByteUtil.byteToInt(buffer[18]);
                                int hdata = ByteUtil.byteToInt(buffer[19])*256;
                                msg.obj = hdata + ldata;
                                msg.what = PHOTO_SIZE;
                                handler.sendMessage(msg);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

        return true;
    }

    /**
     * 发送信息
     *
     * @param content
     */
    public void sendMessage(byte[] content) {
        OutputStream os = null;
        try {
            if (socket != null) {
                os = socket.getOutputStream();
                os.write(content);
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("发送失败:" + e.getMessage());
        }
        //此处不能关闭
//      finally {
//          if (os != null) {
//              try {
//                  os.close();
//              } catch (IOException e) {
//                  throw new RuntimeException("未正常关闭输出流:" + e.getMessage());
//              }
//          }
//      }
    }

    /**
     * 关闭连接
     */
    public void disConnect() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭异常:" + e.getMessage());
            }
            socket = null;
        }
    }

}
