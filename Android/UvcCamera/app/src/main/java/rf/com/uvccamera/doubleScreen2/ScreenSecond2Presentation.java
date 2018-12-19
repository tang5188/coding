package rf.com.uvccamera.doubleScreen2;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import rf.com.uvccamera.R;

public class ScreenSecond2Presentation extends Presentation implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private SliderLayout mSlider;
    private PagerIndicator mIndicator;

    public ScreenSecond2Presentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_screen_second2);

        //分辨率显示
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Log.e("分辨率2", "Density is " + metrics.density + " densityDpi is " + metrics.densityDpi + " height: " + metrics.heightPixels +
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

        TextSliderView textSliderView1 = new TextSliderView(getContext());
        textSliderView1.image(R.drawable.screen101)
                .description("副屏照片1")
                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                .setOnSliderClickListener(this);
        mSlider.addSlider(textSliderView1);

        TextSliderView textSliderView2 = new TextSliderView(getContext());
        textSliderView2.image(R.drawable.screen102)
                .description("副屏照片2")
                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                .setOnSliderClickListener(this);
        mSlider.addSlider(textSliderView2);

        TextSliderView textSliderView3 = new TextSliderView(getContext());
        textSliderView3.image(R.drawable.screen103)
                .description("副屏照片3")
                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                .setOnSliderClickListener(this);
        mSlider.addSlider(textSliderView3);

        mSlider.setCustomIndicator(mIndicator);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setPresetTransformer(SliderLayout.Transformer.CubeIn);
        mSlider.setDuration(8000);
        mSlider.addOnPageChangeListener(this);
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
