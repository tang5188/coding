package com.tang.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MyService extends Service {

    private final String TAG = this.getClass().getSimpleName();

    private int index = 0;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return new MyBind();
    }

    class MyBind extends IMyAidlService.Stub {

        @Override
        public String getString() throws RemoteException {
            index++;
            String string = "我是从服务器返回的, NO:" + index;
            return string;
        }
    }
}
