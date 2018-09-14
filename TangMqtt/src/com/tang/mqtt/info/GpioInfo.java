package com.tang.mqtt.info;

import com.google.gson.annotations.SerializedName;
import com.tang.mqtt.JsonBase;

public class GpioInfo extends JsonBase{
    public GpioInfo() {
        super.key = "gpio";
    }
    @SerializedName("operator")
    public String operator;
    @SerializedName("index")
    public int index;
    @SerializedName("value")
    public int value;
}
