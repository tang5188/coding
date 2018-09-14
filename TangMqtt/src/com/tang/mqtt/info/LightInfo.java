package com.tang.mqtt.info;

import com.google.gson.annotations.SerializedName;
import com.tang.mqtt.JsonBase;

public class LightInfo extends JsonBase {
    @SerializedName("value")
    public String value;
}
