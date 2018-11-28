package tang.com.arcfaceandroid;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.webkit.PermissionRequest;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends Activity {

    public static int PERMISSION_REQ = 0x123456;

    private String[] mPermission = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> mRequestPermission = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String one : mPermission) {
                if (PackageManager.PERMISSION_GRANTED != this.checkPermission(one, Process.myPid(), Process.myUid())) {
                    mRequestPermission.add(one);
                }
            }
            if (!mRequestPermission.isEmpty()) {
                this.requestPermissions(mRequestPermission.toArray(new String[mRequestPermission.size()]), PERMISSION_REQ);
                return;
            }
        }
        startActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (requestCode == PERMISSION_REQ) {
            for (int i = 0; i < grantResults.length; i++) {
                for (String one : mPermission) {
                    if (permissions[i].equals(one) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mRequestPermission.remove(one);
                    }
                }
            }
            startActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_REQ) {
            if (resultCode == 0) {
                this.finish();
            }
        }
    }

    private void startActivity() {
        if (mRequestPermission.isEmpty()) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
            progressDialog.setTitle("loading register data....");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MyApplication application = (MyApplication) PermissionActivity.this.getApplicationContext();
                    application.mFaceDB.loadFaces();
                    PermissionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Intent intent = new Intent(PermissionActivity.this, MainActivity.class);
                            startActivityForResult(intent, PERMISSION_REQ);
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "permission denied!", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PermissionActivity.this.finish();
                }
            }, 3000);
        }
    }
}
