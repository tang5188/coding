package tang.com.sample.viewPager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import tang.com.sample.R;
import tang.com.sample.viewPager.fragment.AddressFragment;
import tang.com.sample.viewPager.fragment.FrdFragment;
import tang.com.sample.viewPager.fragment.SettingFragment;
import tang.com.sample.viewPager.fragment.WeixinFragment;

/*
    https://www.cnblogs.com/caobotao/p/5103673.html
 */
public class ViewPagerActivity extends FragmentActivity implements View.OnClickListener {

    //声明ViewPager
    private ViewPager mViewPager;
    //适配器
    private FragmentPagerAdapter mAdapter;
    //装载Fragment的集合
    private List<Fragment> mFragments;

    //四个Tab对应的布局
    private LinearLayout mTabWeixin;
    private LinearLayout mTabFrd;
    private LinearLayout mTabAddress;
    private LinearLayout mTabSetting;

    //四个Tab对应的ImageButton
    private ImageButton mImgWeixin;
    private ImageButton mImgFrd;
    private ImageButton mImgAddress;
    private ImageButton mImgSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_pager);

        initViews();
        initEvents();
        initDatas();
    }

    //初始化控件
    private void initViews() {
        mViewPager = findViewById(R.id.id_viewpager);

        mTabWeixin = findViewById(R.id.id_tab_weixin);
        mTabFrd = findViewById(R.id.id_tab_frd);
        mTabAddress = findViewById(R.id.id_tab_address);
        mTabSetting = findViewById(R.id.id_tab_setting);

        mImgWeixin = findViewById(R.id.id_tag_weixin_img);
        mImgFrd = findViewById(R.id.id_tag_frd_img);
        mImgAddress = findViewById(R.id.id_tag_address_img);
        mImgSetting = findViewById(R.id.id_tag_setting_img);
    }

    //初始化事件
    private void initEvents() {
        //设置四个Tab的点击事件
        mTabWeixin.setOnClickListener(this);
        mTabFrd.setOnClickListener(this);
        mTabAddress.setOnClickListener(this);
        mTabSetting.setOnClickListener(this);
    }

    //初始化数据
    private void initDatas() {
        mFragments = new ArrayList<>();
        //将四个Fragment加入集合中
        mFragments.add(new WeixinFragment());
        mFragments.add(new FrdFragment());
        mFragments.add(new AddressFragment());
        mFragments.add(new SettingFragment());

        //初始化适配器
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            //从集合中获取对应位置的Fragment
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            //获取集合中Fragment的总数
            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        //不要忘记设置ViewPager的适配器
        mViewPager.setAdapter(mAdapter);
        //设置ViewPager的切换监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //页面滚动事件
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //页面选中事件
            @Override
            public void onPageSelected(int position) {
                //设置position对应的集合中的Fragment
                mViewPager.setCurrentItem(position);
                resetImgs();
                selectTab(position);
            }

            //页面滚动状态改变事件
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        //先将四个ImageButton置为灰色
        resetImgs();
        //根据点击的Tab切换不同的页面及设置对应的ImageButton为绿色
        switch (v.getId()) {
            case R.id.id_tab_weixin:
                selectTab(0);
                break;
            case R.id.id_tab_frd:
                selectTab(1);
                break;
            case R.id.id_tab_address:
                selectTab(2);
                break;
            case R.id.id_tab_setting:
                selectTab(3);
                break;
        }
    }

    //将四个ImageButton设置为灰色
    private void resetImgs() {
        mImgWeixin.setImageResource(R.drawable.tab_weixin_normal);
        mImgFrd.setImageResource(R.drawable.tab_find_frd_normal);
        mImgAddress.setImageResource(R.drawable.tab_address_normal);
        mImgSetting.setImageResource(R.drawable.tab_settings_normal);
    }

    //根据点击的Tab设置对应的ImageButton为绿色
    private void selectTab(int position) {
        switch (position) {
            case 0:
                mImgWeixin.setImageResource(R.drawable.tab_weixin_pressed);
                break;
            case 1:
                mImgFrd.setImageResource(R.drawable.tab_find_frd_pressed);
                break;
            case 2:
                mImgAddress.setImageResource(R.drawable.tab_address_pressed);
                break;
            case 3:
                mImgSetting.setImageResource(R.drawable.tab_settings_pressed);
                break;
        }
        //设置当前点击的Tab所对应的页面
        mViewPager.setCurrentItem(position);
    }
}
