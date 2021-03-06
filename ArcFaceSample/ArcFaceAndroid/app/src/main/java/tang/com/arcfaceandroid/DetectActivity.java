package tang.com.arcfaceandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.ageestimation.ASAE_FSDKAge;
import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKError;
import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.ageestimation.ASAE_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKError;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKGender;
import com.arcsoft.genderestimation.ASGE_FSDKVersion;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.Camera2Manager;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_detect)
public class DetectActivity extends Activity implements Camera.AutoFocusCallback {

    private final String TAG = this.getClass().getSimpleName();

    private int mWidth, mHeight, mFormat;

    @ViewInject(R.id.surfaceView)
    private CameraSurfaceView mSurfaceView;
    @ViewInject(R.id.glsurfaceView)
    private CameraGLSurfaceView mGLSurfaceView;
    @ViewInject(R.id.textView)
    private TextView mTextView;
    @ViewInject(R.id.textView1)
    private TextView mTextView1;
    @ViewInject(R.id.imageView)
    private ImageView mImageView;
    @ViewInject(R.id.imageButton)
    private ImageButton mImageButton;

    private Camera mCamera;
    private int mCameraID;
    private int mCameraRotate;
    private boolean mCameraMirror;
    private Handler mHandler;

    AFT_FSDKVersion version = new AFT_FSDKVersion();
    AFT_FSDKEngine engine = new AFT_FSDKEngine();
    ASAE_FSDKVersion mAgeVersion = new ASAE_FSDKVersion();
    ASAE_FSDKEngine mAgeEngine = new ASAE_FSDKEngine();
    ASGE_FSDKVersion mGenderVersion = new ASGE_FSDKVersion();
    ASGE_FSDKEngine mGenderEngine = new ASGE_FSDKEngine();

    List<AFT_FSDKFace> result = new ArrayList<>();
    List<ASAE_FSDKAge> ages = new ArrayList<>();
    List<ASGE_FSDKGender> genders = new ArrayList<>();
    private FRAbsLoop mFRAbsLoop;
    byte[] mImageNV21 = null;
    private boolean isPostted;

    private AFT_FSDKFace mAFT_fsdkFace;

    Runnable hide = new Runnable() {
        @Override
        public void run() {
            mTextView.setAlpha(0.5f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mImageView.setImageAlpha(128);
            }
            isPostted = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        boolean isBackCamera = getIntent().getIntExtra("Camera", 0) == 0;
        mCameraID = isBackCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCameraRotate = isBackCamera ? 90 : 270;
        mCameraMirror = isBackCamera ? false : true;
        mWidth = 1280;
        mHeight = 960;
        mFormat = ImageFormat.NV21;
        mHandler = new Handler();

        mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, mCameraRotate);
        mSurfaceView.debug_print_fps(true, false);

        mTextView.setText("");
        mTextView1.setText("");
        mSurfaceView.setOnCameraListener(new CameraSurfaceView.OnCameraListener() {

            @Override
            public Camera setupCamera() {
                mCamera = Camera.open(mCameraID);
                try {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setPreviewSize(mWidth, mHeight);
                    parameters.setPreviewFormat(mFormat);

                    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                        Log.d(TAG, "Size:" + size.width + "x" + size.height);
                    }
                    for (Integer format : parameters.getSupportedPreviewFormats()) {
                        Log.d(TAG, "Format:" + format);
                    }

                    List<int[]> fps = parameters.getSupportedPreviewFpsRange();
                    for (int[] count : fps) {
                        Log.d(TAG, "T:");
                        for (int data : count) {
                            Log.d(TAG, "V:" + data);
                        }
                    }
                    mCamera.setParameters(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mCamera != null) {
                    mWidth = mCamera.getParameters().getPreviewSize().width;
                    mHeight = mCamera.getParameters().getPreviewSize().height;
                    Log.i(TAG, "size make:" + mWidth + "," + mHeight);
                }
                return mCamera;
            }

            @Override
            public void setupChanged(int format, int width, int height) {

            }

            @Override
            public boolean startPreviewImmediately() {
                return true;
            }

            @Override
            public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
                AFT_FSDKError error = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
                Log.d(TAG, "AFT_FSDK_FaceFeatureDetect:" + error.getCode());
                Log.d(TAG, "onPreview Face:" + result.size());
                for (AFT_FSDKFace face : result) {
                    Log.d(TAG, "onPreview Face:" + face.toString());
                }
                if (mImageNV21 == null) {
                    if (!result.isEmpty()) {
                        mAFT_fsdkFace = result.get(0).clone();
                        mImageNV21 = data.clone();
                    } else {
                        if (!isPostted) {
                            mHandler.removeCallbacks(hide);
                            mHandler.postDelayed(hide, 2000);
                            isPostted = true;
                        }
                    }
                }
                Rect[] rects = new Rect[result.size()];
                for (int i = 0; i < result.size(); i++) {
                    rects[i] = new Rect(result.get(i).getRect());
                }
                result.clear();
                return rects;
            }

            @Override
            public void onBeforeRender(CameraFrameData data) {

            }

            @Override
            public void onAfterRender(CameraFrameData data) {
                mGLSurfaceView.getGLES2Render().draw_rect((Rect[]) data.getParams(), Color.GREEN, 2);
            }
        });

        AFT_FSDKError error = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
        Log.d(TAG, "AFT_FSDK_InitialFaceEngine=" + error.getCode());
        error = engine.AFT_FSDK_GetVersion(version);
        Log.d(TAG, "AFT_FSDK_GetVersion=" + version.toString() + ", errorCode=" + error.getCode());

        ASAE_FSDKError error1 = mAgeEngine.ASAE_FSDK_InitAgeEngine(FaceDB.appid, FaceDB.fage_key);
        Log.d(TAG, "ASAE_FSDK_InitAgeEngine=" + error1.getCode());
        error1 = mAgeEngine.ASAE_FSDK_GetVersion(mAgeVersion);
        Log.d(TAG, "ASAE_FSDK_GetVersion=" + mAgeVersion.toString() + ", errorCode=" + error1.getCode());

        ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_InitgGenderEngine(FaceDB.appid, FaceDB.fgender_key);
        Log.d(TAG, "ASGE_FSDK_InitgGenderEngine=" + error2.getCode());
        error2 = mGenderEngine.ASGE_FSDK_GetVersion(mGenderVersion);
        Log.d(TAG, "ASGE_FSDK_GetVersion=" + mGenderVersion.toString() + ", errorCode=" + error2.getCode());

        mFRAbsLoop = new FRAbsLoop();
        mFRAbsLoop.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mFRAbsLoop.shutdown();

        AFT_FSDKError error = engine.AFT_FSDK_UninitialFaceEngine();
        Log.d(TAG, "AFT_FSDK_UninitialFaceEngine=" + error.getCode());

        ASAE_FSDKError error1 = mAgeEngine.ASAE_FSDK_UninitAgeEngine();
        Log.d(TAG, "ASAE_FSDK_UninitAgeEngine=" + error1.getCode());

        ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_UninitGenderEngine();
        Log.d(TAG, "ASGE_FSDK_UninitGenderEngine=" + error2.getCode());
    }

    @Event(value = R.id.glsurfaceView, type = View.OnTouchListener.class)
    private boolean glSurfaceView_OnTouch(View v, MotionEvent event) {
        CameraHelper.touchFocus(mCamera, event, v, this);
        return false;
    }

    @Event(value = R.id.imageButton, type = View.OnClickListener.class)
    private void imageButton_OnClick(View v) {
        if (v.getId() == R.id.imageButton) {
            if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                mCameraRotate = 270;
                mCameraMirror = true;
            } else {
                mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
                mCameraRotate = 90;
                mCameraMirror = false;
            }
            mSurfaceView.resetCamera();
            mGLSurfaceView.setRenderConfig(mCameraRotate, mCameraMirror);
            mGLSurfaceView.getGLES2Render().setViewAngle(mCameraMirror, mCameraRotate);
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            Log.d(TAG, "Camera Focus SUCCESS!");
        }
    }

    class FRAbsLoop extends AbsLoop {

        AFR_FSDKVersion version = new AFR_FSDKVersion();
        AFR_FSDKEngine engine = new AFR_FSDKEngine();
        AFR_FSDKFace result = new AFR_FSDKFace();

        List<FaceDB.FaceRegist> mRegist = ((MyApplication) DetectActivity.this.getApplicationContext()).mFaceDB.mRegister;
        List<ASAE_FSDKFace> face1 = new ArrayList<>();
        List<ASGE_FSDKFace> face2 = new ArrayList<>();

        @Override
        public void setup() {
            AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
            Log.d(TAG, "AFR_FSDK_InitialEngine=" + error.getCode());
            error = engine.AFR_FSDK_GetVersion(version);
            Log.d(TAG, "FR=" + version.toString() + "," + error.getCode());
        }

        @Override
        public void loop() {
            if (mImageNV21 != null) {
                final int rotate = mCameraRotate;

                long time = System.currentTimeMillis();
                Log.i(TAG, "AFR_FSDK_ExtractFRFeature:" + mImageNV21.length + ", " + mWidth + ", " + mHeight + ", " + mAFT_fsdkFace.getRect().toString() + ", " + mAFT_fsdkFace.getDegree());
                AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_fsdkFace.getRect(), mAFT_fsdkFace.getDegree(), result);
                Log.i(TAG, "AFR_FSDK_ExtractFRFeature cost:" + (System.currentTimeMillis() - time) + "ms");
                Log.i(TAG, "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + ", errorCode=" + error.getCode());

                //特征码提取成功
                if (error.getCode() == AFR_FSDKError.MOK) {
                    AFR_FSDKMatching score = new AFR_FSDKMatching();
                    float max = 0.0f;
                    String name = null;
                    for (FaceDB.FaceRegist fr : mRegist) {
                        for (AFR_FSDKFace face : fr.mFaceList.values()) {
                            error = engine.AFR_FSDK_FacePairMatching(result, face, score);
                            Log.d(TAG, "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
                            if (max < score.getScore()) {
                                max = score.getScore();
                                name = fr.mName;
                            }
                        }
                    }

                    //age & gender
                    face1.clear();
                    face2.clear();
                    face1.add(new ASAE_FSDKFace(mAFT_fsdkFace.getRect(), mAFT_fsdkFace.getDegree()));
                    face2.add(new ASGE_FSDKFace(mAFT_fsdkFace.getRect(), mAFT_fsdkFace.getDegree()));
                    ASAE_FSDKError error1 = mAgeEngine.ASAE_FSDK_AgeEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face1, ages);
                    ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_GenderEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face2, genders);
                    Log.d(TAG, "ASAE_FSDK_AgeEstimation_Image=" + error1.getCode() + ", ASGE_FSDK_GenderEstimation_Image=" + error2.getCode());
                    Log.d(TAG, "age:" + ages.get(0).getAge() + ", gender:" + genders.get(0).getGender());
                    final String age = ages.get(0).getAge() == 0 ? "年龄未知" : ages.get(0).getAge() + "岁";
                    final String gender = genders.get(0).getGender() == -1 ? "性别未知" : (genders.get(0).getGender() == 0 ? "男" : "女");

                    //crop
                    byte[] data = mImageNV21;
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
                    ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
                    yuvImage.compressToJpeg(mAFT_fsdkFace.getRect(), 80, ops);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
                    try {
                        ops.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (max > 0.6f) {
                        final float max_score = max;
                        Log.d(TAG, "fit score:" + max + ", name:" + name);
                        final String mNameShow = name;
                        mHandler.removeCallbacks(hide);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setAlpha(1.0f);
                                mTextView.setText(mNameShow);
                                mTextView.setTextColor(Color.RED);
                                mTextView1.setVisibility(View.VISIBLE);
                                mTextView1.setText("置信度：" + (float) ((int) (max_score * 1000)) / 1000.0);
                                mTextView1.setTextColor(Color.RED);
                                mImageView.setRotation(rotate);
                                if (mCameraMirror) {
                                    mImageView.setScaleY(-1);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    mImageView.setImageAlpha(255);
                                }
                                mImageView.setImageBitmap(bitmap);
                            }
                        });
                    } else {
                        final String mNameShow = "未识别";
                        DetectActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setAlpha(1.0f);
                                mTextView1.setVisibility(View.VISIBLE);
                                mTextView1.setText(gender + ", " + age);
                                mTextView1.setTextColor(Color.RED);
                                mTextView.setText(mNameShow);
                                mTextView.setTextColor(Color.RED);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    mImageView.setImageAlpha(255);
                                }
                                mImageView.setRotation(rotate);
                                if (mCameraMirror) {
                                    mImageView.setScaleY(-1);
                                }
                                mImageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
                mImageNV21 = null;
            }
        }

        @Override
        public void over() {
            AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
            Log.d(TAG, "AFR_FSDK_UninitialEngine:" + error.getCode());
        }
    }
}
