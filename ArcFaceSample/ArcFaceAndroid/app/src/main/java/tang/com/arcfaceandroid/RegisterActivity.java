package tang.com.arcfaceandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.widget.ExtImageView;
import com.guo.android_extend.widget.HListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ContentView(R.layout.activity_register)
public class RegisterActivity extends Activity implements SurfaceHolder.Callback {

    private final String TAG = "RegisterActivity";
    private final static int MSG_CODE = 0x1000;
    private final static int MSG_EVENT_REG = 0x1001;
    private final static int MSG_EVENT_NO_FACE = 0x1002;
    private final static int MSG_EVENT_NO_FEATURE = 0x1003;
    private final static int MSG_EVENT_FD_ERROR = 0x1004;
    private final static int MSG_EVENT_FR_ERROR = 0x1005;
    private final static int MSG_EVENT_IMG_ERROR = 0x1006;

    private UIHandler mUIHandler;
    private String mFilePath;

    @ViewInject(R.id.surfaceView)
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private Rect src = new Rect();
    private Rect dst = new Rect();
    private Thread view;
    private EditText mEditText;
    private ExtImageView mExtImageView;
    @ViewInject(R.id.hlistView)
    private HListView mHlistView;
    private RegisterViewAdapter mRegisterViewAdapter;
    private AFR_FSDKFace mAFR_FSDKFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        if (!getIntentData(getIntent().getExtras())) {
            Log.e(TAG, "getIntentData fail!");
            this.finish();
        }

        mRegisterViewAdapter = new RegisterViewAdapter(this);
        mHlistView.setAdapter(mRegisterViewAdapter);
        mHlistView.setOnItemClickListener(mRegisterViewAdapter);

        mUIHandler = new UIHandler();
        mBitmap = MyApplication.decodeImage(mFilePath);
        src.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mSurfaceView.getHolder().addCallback(this);

        view = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mSurfaceHolder == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight() * 3 / 2];
                try {
                    ImageConverter converter = new ImageConverter();
                    converter.initial(mBitmap.getWidth(), mBitmap.getHeight(), ImageConverter.CP_PAF_NV21);
                    if (converter.convert(mBitmap, data)) {
                        Log.d(TAG, "convert ok!");
                    }
                    converter.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = MSG_CODE;
                    msg.arg1 = MSG_EVENT_IMG_ERROR;
                    msg.obj = e.getMessage();
                    mUIHandler.sendMessage(msg);
                }

                AFD_FSDKEngine engine = new AFD_FSDKEngine();
                AFD_FSDKVersion version = new AFD_FSDKVersion();
                List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
                AFD_FSDKError error = engine.AFD_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
                Log.d(TAG, "AFD_FSDK_InitialFaceEngine=" + error.getCode());
                if (error.getCode() != AFD_FSDKError.MOK) {
                    Message msg = Message.obtain();
                    msg.what = MSG_CODE;
                    msg.arg1 = MSG_EVENT_FD_ERROR;
                    msg.arg2 = error.getCode();
                    mUIHandler.sendMessage(msg);
                }
                error = engine.AFD_FSDK_GetVersion(version);
                Log.d(TAG, "AFD_FSDK_GetVersion=" + version.toString() + ", " + error.getCode());
                error = engine.AFD_FSDK_StillImageFaceDetection(data, mBitmap.getWidth(), mBitmap.getHeight(), AFD_FSDKEngine.CP_PAF_NV21, result);
                Log.d(TAG, "AFD_FSDK_StillImageFaceDetection=" + error.getCode() + "<" + result.size());
                while (mSurfaceHolder != null) {
                    Canvas canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        Paint paint = new Paint();
                        boolean fit_horizontal = canvas.getWidth() / (float) src.width() < canvas.getHeight() / (float) src.height() ? true : false;
                        float scale = 1.0f;
                        if (fit_horizontal) {
                            scale = canvas.getWidth() / (float) src.width();
                            dst.left = 0;
                            dst.top = (canvas.getHeight() - (int) (src.height() * scale)) / 2;
                            dst.right = dst.left + canvas.getWidth();
                            dst.bottom = dst.top + (int) (src.height() * scale);
                        } else {
                            scale = canvas.getHeight() / (float) src.height();
                            dst.left = (canvas.getWidth() - (int) (src.width() * scale)) / 2;
                            dst.top = 0;
                            dst.right = dst.left + (int) (src.width() * scale);
                            dst.bottom = dst.top + canvas.getHeight();
                        }
                        canvas.drawBitmap(mBitmap, src, dst, paint);
                        canvas.save();
                        canvas.scale((float) dst.width() / (float) src.width(), (float) dst.height() / (float) src.height());
                        canvas.translate(dst.left / scale, dst.top / scale);
                        for (AFD_FSDKFace face : result) {
                            paint.setColor(Color.RED);
                            paint.setStrokeWidth(10.0f);
                            paint.setStyle(Paint.Style.STROKE);
                            canvas.drawRect(face.getRect(), paint);
                        }
                        canvas.restore();
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                        break;
                    }
                }
                if (!result.isEmpty()) {
                    AFR_FSDKVersion version1 = new AFR_FSDKVersion();
                    AFR_FSDKEngine engine1 = new AFR_FSDKEngine();
                    AFR_FSDKFace result1 = new AFR_FSDKFace();
                    AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
                    Log.d("com.arcsoft", "AFR_FSDK_InitialEngine=" + error1.getCode());
                    if (error1.getCode() != AFR_FSDKError.MOK) {
                        Message msg = Message.obtain();
                        msg.what = MSG_CODE;
                        msg.arg1 = MSG_EVENT_FR_ERROR;
                        msg.arg2 = error1.getCode();
                        mUIHandler.sendMessage(msg);
                    }
                    error1 = engine1.AFR_FSDK_GetVersion(version1);
                    Log.d("com.arcsoft", "AFR_FSDK_GetVersion=" + version1.toString() + ", " + error1.getCode());
                    Log.i(TAG, "AFR_FSDK_ExtractFRFeature:" + data.length + ", " + mBitmap.getWidth() + ", " + mBitmap.getHeight() + ", " + new Rect(result.get(0).getRect()).toString() + ", " + result.get(0).getDegree());
                    error1 = engine1.AFR_FSDK_ExtractFRFeature(data, mBitmap.getWidth(), mBitmap.getHeight(), AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
                    Log.d("com.arcsoft", "AFR_FSDK_ExtractFRFeature=" + result1.getFeatureData()[0] + ", " + result1.getFeatureData()[1] + ", " + result1.getFeatureData()[2] + ", " + error1.getCode());
                    if (error1.getCode() == error1.MOK) {
                        mAFR_FSDKFace = result1.clone();
                        int width = result.get(0).getRect().width();
                        int height = result.get(0).getRect().height();
                        Bitmap face_bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                        Canvas face_canvas = new Canvas(face_bitmap);
                        face_canvas.drawBitmap(mBitmap, result.get(0).getRect(), new Rect(0, 0, width, height), null);

                        Message msg = Message.obtain();
                        msg.what = MSG_CODE;
                        msg.arg1 = MSG_EVENT_REG;
                        msg.obj = face_bitmap;
                        mUIHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = MSG_CODE;
                        msg.arg1 = MSG_EVENT_NO_FEATURE;
                        mUIHandler.sendMessage(msg);
                    }
                    error1 = engine1.AFR_FSDK_UninitialEngine();
                    Log.d("com.arcsoft", "AFR_FSDK_UninitialEngine=" + error1.getCode());
                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_CODE;
                    msg.arg1 = MSG_EVENT_NO_FACE;
                    mUIHandler.sendMessage(msg);
                }
                error = engine.AFD_FSDK_UninitialFaceEngine();
                Log.d(TAG, "AFD_FSDK_UninitialFaceEngine=" + error.getCode());
            }
        });
        view.start();
    }

    private boolean getIntentData(Bundle bundle) {
        try {
            mFilePath = bundle.getString("imagePath");
            if (mFilePath == null || mFilePath.isEmpty()) {
                return false;
            }
            Log.i(TAG, "getIntentData:" + mFilePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        try {
            view.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CODE) {
                if (msg.arg1 == MSG_EVENT_REG) {
                    LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
                    View layout = inflater.inflate(R.layout.dialog_register, null);
                    mEditText = layout.findViewById(R.id.et_view);
                    mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
                    mExtImageView = layout.findViewById(R.id.iv_image_view);
                    mExtImageView.setImageBitmap((Bitmap) msg.obj);
                    final Bitmap face = (Bitmap) msg.obj;
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("请输入注册名字")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(layout)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((MyApplication) RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(mEditText.getText().toString(), mAFR_FSDKFace, face);
                                    mRegisterViewAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else if (msg.arg1 == MSG_EVENT_NO_FEATURE) {
                    Toast.makeText(RegisterActivity.this, "人脸特征无法检测，请换一张照片", Toast.LENGTH_SHORT).show();
                } else if (msg.arg1 == MSG_EVENT_NO_FACE) {
                    Toast.makeText(RegisterActivity.this, "没有检测到人脸，请换一张照片", Toast.LENGTH_SHORT).show();
                } else if (msg.arg1 == MSG_EVENT_FD_ERROR) {
                    Toast.makeText(RegisterActivity.this, "FD初始化失败，错误码:" + msg.arg2, Toast.LENGTH_SHORT).show();
                } else if (msg.arg1 == MSG_EVENT_FR_ERROR) {
                    Toast.makeText(RegisterActivity.this, "FR初始化失败，错误码:" + msg.arg2, Toast.LENGTH_SHORT).show();
                } else if (msg.arg1 == MSG_EVENT_IMG_ERROR) {
                    Toast.makeText(RegisterActivity.this, "图像格式错误，" + msg.obj, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class Holder {
        ExtImageView siv;
        TextView tv;
    }

    class RegisterViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        Context mContext;
        LayoutInflater mLInflater;

        public RegisterViewAdapter(Context context) {
            mContext = context;
            mLInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return ((MyApplication) mContext.getApplicationContext()).mFaceDB.mRegister.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView != null) {
                holder = (Holder) convertView.getTag();
            } else {
                convertView = mLInflater.inflate(R.layout.item_sample, null);
                holder = new Holder();
                holder.siv = convertView.findViewById(R.id.iv_item_sample);
                holder.tv = convertView.findViewById(R.id.tv_item_sample);
                convertView.setTag(holder);
            }

            if (!((MyApplication) mContext.getApplicationContext()).mFaceDB.mRegister.isEmpty()) {
                FaceDB.FaceRegist face = ((MyApplication) mContext.getApplicationContext()).mFaceDB.mRegister.get(position);
                holder.tv.setText(face.mName);
                String keyPath = face.mFaceList.keySet().iterator().next();
                holder.siv.setImageBitmap(BitmapFactory.decodeFile(keyPath));
                holder.siv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                convertView.setWillNotDraw(false);
            }
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d("onItemClick", "onItemClick=" + position + ", pos=" + mHlistView.getScroll());
            final FaceDB faceDB = ((MyApplication) mContext.getApplicationContext()).mFaceDB;
            final String name = faceDB.mRegister.get(position).mName;
            final int count = faceDB.mRegister.get(position).mFaceList.size();
            final Map<String, AFR_FSDKFace> face = faceDB.mRegister.get(position).mFaceList;
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("删除注册名：" + name)
                    .setMessage("包含：" + count + "个注册人脸特征信息")
                    .setView(new ListView(mContext))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            faceDB.delete(name);
                            mRegisterViewAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }
}
