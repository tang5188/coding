package com.tang.mqtt;

public interface MqttReceiveListener {
    void MqttReceiveListener(String topic, int qos, JsonBase message);
}
