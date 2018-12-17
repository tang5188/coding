package rf.com.uvccamera.doubleScreen2;

import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import rf.com.uvccamera.R;

/**
 * 参考文档：http://www.cnblogs.com/IWings/p/6094708.html
 */
public class ScreenMain2Activity extends AppCompatActivity {

    Presentation mPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main2);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.e("分辨率", "Density is " + displayMetrics.density + " densityDpi is " + displayMetrics.densityDpi + " height: " + displayMetrics.heightPixels +
                " width: " + displayMetrics.widthPixels);

        DisplayManager mDisplayManager;// 屏幕管理类
        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();

        if (mPresentation == null) {
            mPresentation = new ScreenSecond2Presentation(this, displays[displays.length - 1]);// displays[1]是副屏
            mPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mPresentation.show();
        }
    }
}
