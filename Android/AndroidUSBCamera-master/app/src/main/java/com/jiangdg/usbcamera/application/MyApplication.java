package com.jiangdg.usbcamera.application;

import android.app.Application;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

import com.jiangdg.usbcamera.utils.CrashHandler;

/**
 * 全局类
 * <p>
 * Created by jianddongguo on 2017/7/20.
 */

public class MyApplication extends Application {

    public static final String TAG = "MyApplication";

    private static MyApplication myApplication;

    public static MyApplication getInstance() {
        return myApplication;
    }

    public static Context getContext() {
        return myApplication.getApplicationContext();
    }

    public Display externalDisplay;

    private CrashHandler mCrashHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext(), getClass());

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        Log.d(TAG, "displays count:" + displays.length);
        if (displays.length > 0) {
            externalDisplay = displays[0];
        }
    }
}
