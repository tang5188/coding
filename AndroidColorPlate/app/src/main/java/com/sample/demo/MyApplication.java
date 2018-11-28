package com.sample.demo;

import android.app.Application;

import org.xutils.x;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //xUtils框架初始化
        x.Ext.init(this);
        x.Ext.setDebug(false);
    }
}
