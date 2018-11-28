package com.sample.demo;

import android.os.Bundle;
import android.util.Log;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import com.sample.demo.base.BaseTouchActivity;
import com.sample.demo.models.RoomInfos;
import com.sample.demo.view.ColorLayerImageView;
import com.sample.demo.view.DefaultDrawables;

import java.util.List;

@ContentView(R.layout.activity_color)
public class ColorActivity extends BaseTouchActivity {
    private static String TAG = "ColorActivity";
    //房间唯一标识符
    private String roomId = "";
    private RoomInfos.RoomInfo roomInfo = null;

    @ViewInject(R.id.view_room)
    private ColorLayerImageView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        Log.d(TAG, "---onCreate");

        //当前房间号
        roomId = getIntent().getStringExtra("room_id");
        Log.i(TAG, "open roomId:" + roomId);
        //显示房间数据
        DisplayRoomInfo();
        //显示房间导航按钮
        LoadRoomData();
    }

    private void DisplayRoomInfo() {
        this.roomInfo = DefaultDrawables.GetRoomInfo(this.roomId);
        //根据房间号，加载不同的布局
        List<Integer> drawableIds = DefaultDrawables.GetDrawableIds(ColorActivity.this, this.roomId);
        if (drawableIds == null ||
                drawableIds.size() == 0) return;
        //创建相应的显示图层
        view.RefreshLayerDrawable(this.roomId, drawableIds);
    }

    @Override
    protected String GetCurrentRoomId() {
        return roomId;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "---onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "---onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "---onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "---onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "---onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "---onDestroy");
    }
}
