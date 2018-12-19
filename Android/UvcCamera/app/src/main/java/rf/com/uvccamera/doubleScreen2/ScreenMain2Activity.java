package rf.com.uvccamera.doubleScreen2;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import rf.com.uvccamera.R;

/**
 * 参考文档：http://www.cnblogs.com/IWings/p/6094708.html
 */
public class ScreenMain2Activity extends Activity {

    Presentation mPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main2);

        DisplayManager mDisplayManager;// 屏幕管理类
        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();
        if (mPresentation == null) {
            mPresentation = new ScreenSecond2Presentation(this, displays[displays.length - 1]);// displays[1]是副屏
            mPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mPresentation.show();
        }

        //设置全屏
        setFullScreen();
        //分辨率显示
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        Log.e("分辨率1", "Density is " + metrics.density + " densityDpi is " + metrics.densityDpi + " height: " + metrics.heightPixels +
                " width: " + metrics.widthPixels);
    }

    private void setFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
