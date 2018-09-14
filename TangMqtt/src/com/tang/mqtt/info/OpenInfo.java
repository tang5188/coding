package com.tang.mqtt.info;

import com.google.gson.annotations.SerializedName;
import com.tang.mqtt.JsonBase;

public class OpenInfo extends JsonBase{
    public OpenInfo() {
        super.key = "open";
    }

    /**
     * 闇�瑕佸彂閫佺殑Mqtt鍦板潃
     */
    @SerializedName("topic")
    public String address;
    /**
     * 鍚庡彴鍙戦�佹暟鎹椂闂存埑
     */
    @SerializedName("time")
    public String time;

    /**
     * 灞炴��
     */
    @SerializedName("socketid")
    public String socketid;

    /**
     * 浼氬憳缂栧彿
     */
    @SerializedName("custid")
    public int custid;
    /**
     * 鏀粯瀹濇垨寰俊浼氬憳缂栧彿
     */
    @SerializedName("openid")
    public String openid;

    /**
     * 0:鏀粯瀹�
     * 1:寰俊
     */
    @SerializedName("buytype")
    public int buytype;
    /**
     * 寰俊鏀粯灞炴��
     */
    @SerializedName("spbill_create_ip")
    public String spbill_create_ip;
    /**
     * 鎿嶄綔妯″紡
     */
    @SerializedName("operator")
    public String operator;
    /**
     * 鏌滃瓙鍙�
     */
    @SerializedName("box")
    public String box;
    /**
     * status:0
     * status:1 琛ㄧず閿佸凡鎵撳紑
     * status:2 琛ㄧず鐩樼偣涓�
     * status:3 閿佺姸鎬佸紓甯�
     * status:4 鏃堕棿鎴宠秴杩�30
     */
    @SerializedName("status")
    public int status;

    /**
     * 0:鏁版嵁
     * 1:閿佹墦寮�
     * 2:閿佸悎涓�
     */
    public int notify;

}