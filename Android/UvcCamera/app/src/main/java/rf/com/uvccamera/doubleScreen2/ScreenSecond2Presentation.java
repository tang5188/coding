package rf.com.uvccamera.doubleScreen2;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import rf.com.uvccamera.R;

public class ScreenSecond2Presentation extends Presentation {
    public ScreenSecond2Presentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_screen_second2);
    }
}
