package tang.com.sample.serviceAIDL;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tang.aidlserver.IMyAidlService;

import tang.com.sample.R;

/**
 * 进程间通信示例，应用到aidlServer工程
 */
public class ServiceAidlActivity extends Activity {

    private static final String TAG = "ServiceAidlActivity";

    private Button btnBind, btnUnbind;
    private Button btnGetMessage;
    private IMyAidlService myAidlService;

    private boolean bindFlag = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, " -----> onServiceConnected");
            myAidlService = IMyAidlService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myAidlService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_aidl);

        btnBind = findViewById(R.id.btn_bind_service);
        btnUnbind = findViewById(R.id.btn_unbind_service);
        btnGetMessage = findViewById(R.id.btn_get_message);

        //绑定服务
        btnBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " -----> btnBind onClick");
                Intent intent = new Intent();
                intent.setAction("com.tang.aidl.service");
                intent.setComponent(new ComponentName("com.tang.aidlserver", "com.tang.aidlserver.MyService"));
                //魅族手机：不要问我为什么会这样，因为我怎么知道深度定制的Flyme系统在中间到底干了什么鬼！
                startService(intent);   //参考链接：https://blog.csdn.net/liuweihhhh/article/details/79162824
                boolean flag = bindService(intent, connection, BIND_AUTO_CREATE);
                Log.i(TAG, " -----> bindService:" + flag);
                bindFlag = true;
            }
        });
        //解绑服务
        btnUnbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " -----> btnUnbind onClick");
                if (bindFlag) {
                    unbindService(connection);
                    bindFlag = false;
                }
            }
        });
        //显示消息
        btnGetMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " -----> btnGetMessage onClick");
                if (myAidlService != null) {
                    String msg = "null";
                    try {
                        msg = myAidlService.getString();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ServiceAidlActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (bindFlag) {
            unbindService(connection);
            bindFlag = false;
        }
        super.onDestroy();
    }
}
