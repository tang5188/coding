package com.tang.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.tang.mqtt.info.GpioInfo;
import com.tang.mqtt.info.OpenInfo;
import com.tang.mqtt.info.RunResultInfo;
import com.tang.mqtt.info.WillInfo;

import java.lang.reflect.Type;

public class JsonBase {
    @SerializedName("key")
    public String key;
    private static transient Gson json = new GsonBuilder()
            .registerTypeAdapter(JsonBase.class, new JsonDeserializer() {
                @Override
                public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    String key = json.getAsJsonObject().get("key").getAsString();
                    switch (key) {
                        case "will":
                            return context.deserialize(json, WillInfo.class);
                        case "open":
                            return context.deserialize(json, OpenInfo.class);
                        case "gpio":
                            return context.deserialize(json, GpioInfo.class);
                        case "runResult":
                            return context.deserialize(json, RunResultInfo.class);
                        default:
                            return context.deserialize(json, JsonBase.class);
                    }
                }
            })
            .create();

    public String toJsonString() {
        return json.toJson(this);
    }

    public static JsonBase fromJson(String str) {
        return json.fromJson(str, JsonBase.class);
    }
}
