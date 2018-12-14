package tang.com.sample.adapter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tang.com.sample.R;
import tang.com.sample.adapter.bean.BaseItemBean;

/*
    http://www.cnblogs.com/caobotao/p/5061627.html
 */
public class BaseAdapterActivity extends AppCompatActivity {

    private ListView mLvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_adapter);

        List<BaseItemBean> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(new BaseItemBean(R.mipmap.ic_launcher, "标题" + i, "内容" + i));
        }
        mLvMain = findViewById(R.id.lv_main);
        mLvMain.setAdapter(new MyBaseAdapter(items));
    }

    class MyBaseAdapter extends BaseAdapter {

        private List<BaseItemBean> mList;
        private LayoutInflater mLayoutInflater;

        public MyBaseAdapter(List<BaseItemBean> list) {
            this.mList = list;
            this.mLayoutInflater = LayoutInflater.from(BaseAdapterActivity.this);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public BaseItemBean getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getViewOne(position, convertView, parent);
        }

        private View getViewOne(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.base_adapter_item, null);
                viewHolder.ivImage = convertView.findViewById(R.id.iv_image);
                viewHolder.tvTitle = convertView.findViewById(R.id.tv_title);
                viewHolder.tvContent = convertView.findViewById(R.id.tv_content);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            BaseItemBean itemBean = mList.get(position);

            viewHolder.ivImage.setImageResource(itemBean.itemImageResId);
            viewHolder.tvTitle.setText(itemBean.itemTitle);
            viewHolder.tvContent.setText(itemBean.itemContent);

            return convertView;
        }

        class ViewHolder {
            public ImageView ivImage;
            public TextView tvTitle;
            public TextView tvContent;
        }
    }
}
