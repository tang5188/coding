package com.sample.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.sample.demo.base.BaseActivity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@ContentView(R.layout.activity_screen_saver)
public class ScreenSaverActivity extends BaseActivity {

    @ViewInject(R.id.gif_screen_pointer)
    private com.felipecsl.gifimageview.library.GifImageView gifScreenPointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        gifImageView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Intent intent = new Intent(ScreenSaverActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * GifImageView获取图片资源并通过流的形式传递到
     */
    private void gifImageView() {
        try {
            InputStream is = this.getResources().openRawResource(R.raw.screen_pointer);//获取动图资源
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[2048];
            int len = 0;
            while ((len = is.read(b, 0, 2048)) != -1) {
                baos.write(b, 0, len);
            }
            baos.flush();//刷新流，确保传递完全
            byte[] bytes = baos.toByteArray();//转换成Byte数组
            gifScreenPointer.setBytes(bytes);//设置gif图片
            gifScreenPointer.startAnimation();//运行动画
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
