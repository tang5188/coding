package rf.com.uvccamera.doubleScreen2;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import rf.com.uvccamera.R;

/**
 * 参考文档：http://www.cnblogs.com/IWings/p/6094708.html
 */
public class ScreenMain2Activity extends Activity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private Presentation mPresentation;

    private SliderLayout mSlider;
    private PagerIndicator mIndicator;

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

        mSlider = (SliderLayout) findViewById(R.id.home_slider_ad);
        mIndicator = (PagerIndicator) findViewById(R.id.home_indicator_ad);
        this.initSlider();
    }

    @Override
    protected void onStop() {
        mSlider.stopAutoCycle();
        super.onStop();
    }

    private void initSlider() {

        TextSliderView textSliderView1 = new TextSliderView(ScreenMain2Activity.this);
        textSliderView1.image(R.drawable.screen001)
                .description("主屏照片1")
                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                .setOnSliderClickListener(this);
        mSlider.addSlider(textSliderView1);

        TextSliderView textSliderView2 = new TextSliderView(ScreenMain2Activity.this);
        textSliderView2.image(R.drawable.screen002)
                .description("主屏照片2")
                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                .setOnSliderClickListener(this);
        mSlider.addSlider(textSliderView2);

        TextSliderView textSliderView3 = new TextSliderView(ScreenMain2Activity.this);
        textSliderView3.image(R.drawable.screen003)
                .description("主屏照片3")
                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                .setOnSliderClickListener(this);
        mSlider.addSlider(textSliderView3);

        mSlider.setCustomIndicator(mIndicator);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
        mSlider.setDuration(7000);
        mSlider.addOnPageChangeListener(this);
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

    // region 轮播回调
    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    // endregion
}
