package com.sample.demo.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import com.sample.demo.ScreenSaverActivity;

public class CountTimer extends CountDownTimer {
    private static String TAG = "CountTimer";
    private final Context context;

    /**
     * 参数 millisInFuture       倒计时总时间（如60S，120s等）
     * 参数 countDownInterval    渐变时间（每次倒计1s）
     */
    public CountTimer(long millisInFuture, long countDownInterval, Context context) {
        super(millisInFuture, countDownInterval);
        this.context = context;
    }

    // 计时完毕时触发
    @Override
    public void onFinish() {
        Intent intent = new Intent(this.context, ScreenSaverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

    // 计时过程显示
    @Override
    public void onTick(long millisUntilFinished) {
        Log.v(TAG, millisUntilFinished + "ms");
    }
}
