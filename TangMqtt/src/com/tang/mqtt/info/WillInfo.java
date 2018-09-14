package com.tang.mqtt.info;

import com.google.gson.annotations.SerializedName;
import com.tang.mqtt.JsonBase;

public class WillInfo extends JsonBase {
    public WillInfo() {
        super.key = "will";
    }
    @SerializedName("value")
    public String value;
    @SerializedName("box")
    public String box_id;
}
