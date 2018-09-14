package com.tang.mqtt;

public interface MqttConnectStatusListener {
    void MqttConnectStatusListener(int status, String dateTime, String info);
}
