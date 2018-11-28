package com.sample.demo;

import android.os.Bundle;

import com.sample.demo.base.BaseTouchActivity;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseTouchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        //显示房间导航按钮
        LoadRoomData();
    }
}
