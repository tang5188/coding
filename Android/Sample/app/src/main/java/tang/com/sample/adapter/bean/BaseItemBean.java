package tang.com.sample.adapter.bean;

public class BaseItemBean {
    public int itemImageResId;
    public String itemTitle;
    public String itemContent;

    public BaseItemBean(int itemImageResId, String itemTitle, String itemContent) {
        this.itemImageResId = itemImageResId;
        this.itemTitle = itemTitle;
        this.itemContent = itemContent;
    }
}
