package rf.com.uvccamera.doubleScreen;

import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

import rf.com.uvccamera.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ScreenSecondPresentation extends Presentation {

    public ScreenSecondPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_screen_second);
    }
}
