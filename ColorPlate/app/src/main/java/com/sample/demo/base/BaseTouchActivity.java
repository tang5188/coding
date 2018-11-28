package com.sample.demo.base;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sample.demo.ColorActivity;
import com.sample.demo.R;
import com.sample.demo.common.GetResourcesUtils;
import com.sample.demo.models.RoomInfos;
import com.sample.demo.view.DefaultDrawables;

public class BaseTouchActivity extends BaseActivity {
    private static String TAG = "BaseTouchActivity";
    private CountTimer countTimerView;

    private LinearLayout llContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTimer();
    }

    // region 右下角按钮
    public void LoadRoomData() {
        llContainer = findViewById(R.id.llContainer);
        if (llContainer == null) return;
        //获取房间的设定资源
        RoomInfos roomInfos = DefaultDrawables.GetRoomSetting();
        //循环创建按钮
        for (int i = 0; i < roomInfos.infos.size(); i++) {
            //当前按钮信息
            RoomInfos.RoomInfo roomInfo = roomInfos.infos.get(i);

            Button button = new Button(this);
            int drawableId = GetResourcesUtils.getDrawableId(this, roomInfo.icon);
            if (drawableId > 0) {
                // 使用代码设置drawableLeft
                Drawable drawable = this.getDrawable(drawableId);
                drawable.setBounds(10, 0, 140, 130);
                button.setCompoundDrawables(drawable, null, null, null);
            }
            if (GetCurrentRoomId().equals(roomInfo.id)) {
                button.setBackgroundResource(R.drawable.button_round_shap_selected);
            } else {
                button.setBackgroundResource(R.drawable.button_round_shap);
            }
            button.setWidth(400);
            button.setText(roomInfo.name);
            button.setTag(roomInfo.id);
            button.setOnClickListener(new MyOnClickListener());
            //添加进布局中
            llContainer.addView(button);
            //设定按钮的边距
            if (i > 0) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();
                layoutParams.setMargins(0, 20, 0, 0);
                button.setLayoutParams(layoutParams);
            }
        }
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //当前点击按钮的标识符
            String roomId = (String) v.getTag();
            if (GetCurrentRoomId().equals(roomId)) return;

            //打开指定页面
            Intent intent = new Intent(BaseTouchActivity.this, ColorActivity.class);
            intent.putExtra("room_id", roomId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //获取当前房间号
    protected String GetCurrentRoomId() {
        return "";
    }
    // endregion

    // region 屏保相关
    private void timeStart() {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                countTimerView.start();
            }
        });
    }

    private void initTimer() {
        //初始化CountTimer，设置倒计时为2分钟。
        countTimerView = new CountTimer(DefaultDrawables.SleepSeconds * 1000, 1000, BaseTouchActivity.this);
    }

    //触摸时，重新开始计时
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            //获取触摸动作，如果ACTION_UP，计时开始。
            case MotionEvent.ACTION_UP:
                countTimerView.cancel();
                countTimerView.start();
                break;
            //否则其他动作计时取消
            default:
                countTimerView.cancel();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "______onPause");
        countTimerView.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "______onResume");
        timeStart();
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "______onStop");
        countTimerView.cancel();
        super.onStop();
    }

    // endregion
}
