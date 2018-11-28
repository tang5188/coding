package com.sample.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import com.sample.demo.R;
import com.sample.demo.common.PrefUtils;
import com.sample.demo.common.TwoTuple;

/**
 * 色板布局颜色控制画面
 */
public class ColorLayerImageView extends android.support.v7.widget.AppCompatImageView {

    private Context context;

    //房间号
    private String roomId;
    //图片的size、资源号
    private Size size;
    private List<Integer> drawableIds;
    //描绘的图层
    private LayerDrawable drawables;

    public ColorLayerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    //设定当前画面布局
    public void RefreshLayerDrawable(String roomId, List<Integer> drawableIds) {
        this.roomId = roomId;
        this.drawableIds = drawableIds;

        Drawable[] layers = new Drawable[this.drawableIds.size()];
        //向布局中设定
        for (int i = 0; i < this.drawableIds.size(); i++) {
            Drawable drawable = context.getDrawable(this.drawableIds.get(i));
            //设定保存的颜色
            int color = PrefUtils.getInt(context, this.roomId + "_" + i, -1);
            if (color != -1) drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            layers[i] = drawable;
        }
        drawables = new LayerDrawable(layers);
        this.setImageDrawable(drawables);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (drawables != null) {
            this.size = new Size(drawables.getIntrinsicWidth(), drawables.getIntrinsicHeight());
            setMeasuredDimension(drawables.getIntrinsicWidth(), drawables.getIntrinsicHeight());
        } else {
            this.size = new Size(100, 100);
            setMeasuredDimension(100, 100);
        }
    }

    int i = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            TwoTuple<Integer, Drawable> drawable = findDrawable(x, y);
            if (drawable != null) {
                if (i == 888) {
                    //改变drawable的代码
                    Drawable drawable1 = this.context.getDrawable(R.drawable.crab_mask5);
                    drawables.setDrawable(drawable.First, drawable1);
                } else {
                    int randomColor = randomColor();
                    drawable.Second.setColorFilter(randomColor, PorterDuff.Mode.SRC_IN);
                    //保存设定
                    PrefUtils.putInt(context, this.roomId + "_" + drawable.First, randomColor);
                }
                i++;
            }
        }
        return super.onTouchEvent(event);
    }

    private int randomColor() {
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return color;
    }

    private TwoTuple<Integer, Drawable> findDrawable(float x, float y) {
        if (drawables == null ||
                drawableIds.size() == 0) return null;
        //将点击的坐标转化为图片的坐标
        Point ptImage = ConvertPoint2Bitmap(x, y);

        Drawable drawable = null;
        Bitmap bitmap = null;
        final int numberOfLayers = drawables.getNumberOfLayers();
        for (int i = numberOfLayers - 1; i >= 0; i--) {
            drawable = drawables.getDrawable(i);
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            try {
                int pixel = bitmap.getPixel(ptImage.x, ptImage.y);
                if (pixel == Color.TRANSPARENT) continue;
            } catch (Exception e) {
                continue;
            }
            return new TwoTuple(i, drawable);
        }
        return null;
    }

    //图片中心对齐，将鼠标点击的坐标转换为图片坐标
    private Point ConvertPoint2Bitmap(float x, float y) {
        float xr = (this.getWidth() - this.size.getWidth()) / 2;
        float yr = (this.getHeight() - this.size.getHeight()) / 2;

        Point ptRet = new Point();
        ptRet.x = (int) (x - xr);
        ptRet.y = (int) (y - yr);
        return ptRet;
    }
}
