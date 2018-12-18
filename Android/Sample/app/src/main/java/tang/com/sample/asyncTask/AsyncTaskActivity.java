package tang.com.sample.asyncTask;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import tang.com.sample.R;

/*
    https://www.cnblogs.com/caobotao/p/5020857.html
 */
public class AsyncTaskActivity extends AppCompatActivity implements Button.OnClickListener {

    private Button btnProgress;
    private Button btnImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_task);

        //加载照片
        btnImage = findViewById(R.id.btn_async_image);
        btnImage.setOnClickListener(this);
        //加载进度条
        btnProgress = findViewById(R.id.btn_async_progress);
        btnProgress.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_async_image:
                startActivity(new Intent(AsyncTaskActivity.this, AsyncTaskImageActivity.class));
                break;
            case R.id.btn_async_progress:
                startActivity(new Intent(AsyncTaskActivity.this, AsyncTaskProgressActivity.class));
                break;
        }
    }
}
