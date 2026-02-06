package com.example.BLPS.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttMessageSender {

    private MqttClient mqttClient;
    private boolean isConnected = false;

    public void sendMessage(String topic, String message) throws MqttException {
        if (mqttClient == null) {
            mqttClient = new MqttClient("tcp://localhost:1884", MqttClient.generateClientId());
        }

        if (!isConnected) {
            mqttClient.connect();
            isConnected = true;
        }

        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttClient.publish("moderation-queue", mqttMessage);
    }
}
