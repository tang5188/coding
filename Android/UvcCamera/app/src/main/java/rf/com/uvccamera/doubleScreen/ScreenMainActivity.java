package rf.com.uvccamera.doubleScreen;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

import rf.com.uvccamera.R;
import rf.com.uvccamera.application.MyApplication;

public class ScreenMainActivity extends Activity {

    private DisplayManager displayManager;
    private ScreenSecondPresentation presentation;

    MediaPlayer mMediaPlayer = null;

    private TextureView mTextureView;
    SurfaceTexture mSurfaceTexture;
    Surface mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main);

        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        //显示副屏
        showSecondScreen();

        mMediaPlayer = new MediaPlayer();
        mTextureView = findViewById(R.id.tv_main);

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                //SurfaceTexture准备就绪
                if (mSurfaceTexture == null) {
                    mSurfaceTexture = surfaceTexture;
                    openMediaPlayer();
                } else {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i("videoView", what + "," + extra);
                return false;
            }
        });
    }

    private void openMediaPlayer() {
        // 设置dataSource
        try {
            //路径按实际配置
            File file = new File("/storage/emulated/0/Movies/0.mp4");

            mMediaPlayer.setDataSource(file.getPath());
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //副屏显示
    private void showSecondScreen() {
        Display[] displays = displayManager.getDisplays();
        this.presentation = new ScreenSecondPresentation(ScreenMainActivity.this, displays[displays.length - 1]);
        this.presentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        this.presentation.show();
    }
}
