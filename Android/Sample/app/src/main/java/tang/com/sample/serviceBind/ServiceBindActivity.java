package tang.com.sample.serviceBind;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tang.com.sample.R;

/*
    https://www.cnblogs.com/huangjialin/p/7738104.html
 */
public class ServiceBindActivity extends AppCompatActivity implements Button.OnClickListener {

    private static final String TAG = "ServiceBindActivity";

    private ServiceTest.MyBind myBind;
    private boolean isBinding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_bind);

        findViewById(R.id.btn_start_service).setOnClickListener(this);
        findViewById(R.id.btn_stop_service).setOnClickListener(this);
        findViewById(R.id.btn_bind_service).setOnClickListener(this);
        findViewById(R.id.btn_unbind_service).setOnClickListener(this);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBind = (ServiceTest.MyBind) service;
            myBind.getString();
            Log.i(TAG, " ----> onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, " ----> onServiceDisconnected");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.i(TAG, " ----> onNullBinding");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.i(TAG, " ----> onBindingDied");
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_service:
                Intent intentStart = new Intent(ServiceBindActivity.this, ServiceTest.class);
                startService(intentStart);
                Log.i(TAG, " ----> btn_start_service click");
                break;
            case R.id.btn_stop_service:
                Intent intentStop = new Intent(ServiceBindActivity.this, ServiceTest.class);
                stopService(intentStop);
                Log.i(TAG, " ----> btn_stop_service click");
                break;
            case R.id.btn_bind_service:
                Intent intentBind = new Intent(ServiceBindActivity.this, ServiceTest.class);
                boolean flag = bindService(intentBind, connection, BIND_AUTO_CREATE);
                isBinding = true;
                Log.i(TAG, " ----> btn_bind_service click:" + flag);
                break;
            case R.id.btn_unbind_service:
                if (isBinding) {
                    unbindService(connection);
                    isBinding = false;
                }
                Log.i(TAG, " ----> btn_unbind_service click");
                break;
        }
    }
}
