package rf.com.uvccamera.application;

import android.app.Application;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

public class MyApplication extends Application {

    public static final String TAG = "MyApplication";

    private static MyApplication myApplication;

    public static MyApplication getInstance() {
        return myApplication;
    }

    public static Context getContext() {
        return myApplication.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = this;
    }
}
