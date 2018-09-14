package com.tang.mqtt.info;

import com.google.gson.annotations.SerializedName;
import com.tang.mqtt.JsonBase;

public class RunResultInfo extends JsonBase {
    public RunResultInfo() {
        super.key = "runResult";
    }

    @SerializedName("minTime")
    public int minTime = 99999;
    @SerializedName("maxTime")
    public int maxTime;
    @SerializedName("averageTime")
    public double averageTime;
    @SerializedName("minNum")
    public int minNum = 99999;
    @SerializedName("maxNum")
    public int maxNum;
    @SerializedName("workNum")
    public int workNum;
}
