package rf.com.uvccamera.doubleScreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import rf.com.uvccamera.R;
import rf.com.uvccamera.application.MyApplication;

public class ScreenMainActivity extends AppCompatActivity {

    private Display externalDisplay;
    private ScreenSecondPresentation presentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main);

        //显示副屏
        showSecondScreen();
    }

    //副屏显示
    private void showSecondScreen() {
        externalDisplay = MyApplication.getInstance().externalDisplay;
        if (externalDisplay != null) {
            this.presentation = new ScreenSecondPresentation(ScreenMainActivity.this, externalDisplay);
            this.presentation.show();
        }
    }
}
