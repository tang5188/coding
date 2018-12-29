package rf.com.uvccamera.application;

import android.app.Application;
import android.content.Context;

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
