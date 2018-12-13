package rf.com.uvccamera.application;

import android.app.Application;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

public class MyApplication extends Application {

    public static final String TAG = "MyApplication";

    public Display externalDisplay;

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

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        Log.d(TAG, "displays count:" + displays.length);
        if (displays.length > 0) {
            externalDisplay = displays[0];
        }
    }
}
