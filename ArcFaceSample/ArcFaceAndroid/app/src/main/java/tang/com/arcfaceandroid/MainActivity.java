package tang.com.arcfaceandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrinterId;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.AndroidException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.x;

import tang.com.arcfaceandroid.util.PathGet;

@ContentView(R.layout.activity_main)
public class MainActivity extends Activity {

    private final String TAG = this.getClass().toString();

    private static final int REQUEST_CODE_IAMGE_CAMERA = 1;
    private static final int REQUEST_CODE_IAMGE_OP = 2;
    private static final int REQUEST_CODE_OP = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //注册人脸
    @Event(value = R.id.btn_register, type = View.OnClickListener.class)
    private void btnRegister_onClick(View v) {
        new AlertDialog.Builder(this)
                .setTitle("请选择注册方式")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setItems(new String[]{"打开图片", "拍摄照片"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {       //拍摄照片
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            ContentValues values = new ContentValues(1);
                            values.put(MediaStore.Images.Media.MIME_TYPE, "iamge/jpeg");
                            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            ((MyApplication) MainActivity.this.getApplicationContext()).setCaptureImage(uri);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            startActivityForResult(intent, REQUEST_CODE_IAMGE_CAMERA);
                        } else {            //打开图片
                            Intent getImageByAlbum = new Intent(Intent.ACTION_GET_CONTENT);
                            getImageByAlbum.addCategory(Intent.CATEGORY_OPENABLE);
                            getImageByAlbum.setType("image/jpeg");
                            startActivityForResult(getImageByAlbum, REQUEST_CODE_IAMGE_OP);
                        }
                    }
                })
                .show();
    }

    //人脸检测、识别
    @Event(value = R.id.btn_detect, type = View.OnClickListener.class)
    private void btnDetect_onClick(View v) {
        if (((MyApplication) MainActivity.this.getApplicationContext()).mFaceDB.mRegister.isEmpty()) {
            Toast.makeText(this, "没有注册人脸，请先注册！", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("请选择相机")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setItems(new String[]{"后置相机", "前置相机"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startDetector(which);
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IAMGE_OP && resultCode == RESULT_OK) {
            Uri mPath = data.getData();
            String file = PathGet.getPath(MainActivity.this, mPath);
            Bitmap bmp = MyApplication.decodeImage(file);
            if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0) {
                Log.e(TAG, "error");
            } else {
                Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight() + "]");
            }
            startRegister(bmp, file);
        } else if (requestCode == REQUEST_CODE_OP) {
            Log.i(TAG, "result=" + resultCode);
            if (data == null) return;
            Bundle bundle = data.getExtras();
            String path = bundle.getString("imagePath");
            Log.i(TAG, "path=" + path);
        } else if (requestCode == REQUEST_CODE_IAMGE_CAMERA && resultCode == RESULT_OK) {
            Uri mPath = ((MyApplication) MainActivity.this.getApplicationContext()).getCaptureImage();
            String file = PathGet.getPath(MainActivity.this, mPath);
            Bitmap bmp = MyApplication.decodeImage(file);
            startRegister(bmp, file);
        }
    }

    //启动注册画面
    private void startRegister(Bitmap bmp, String file) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", file);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_OP);
    }

    //启动检测画面
    private void startDetector(int camera) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        intent.putExtra("Camera", camera);
        startActivityForResult(intent, REQUEST_CODE_OP);
    }
}
