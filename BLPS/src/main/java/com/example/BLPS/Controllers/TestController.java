package com.example.BLPS.Controllers;

import com.example.BLPS.config.MqttMessageSender;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/test")
public class TestController {
    private final MqttMessageSender mqttMessageSender;

    @Autowired
    public TestController(MqttMessageSender mqttMessageSender) {
        this.mqttMessageSender = mqttMessageSender;
    }

    @PostMapping("/mqtt_send")
    public String sendMessage(@RequestParam(defaultValue = "moderation-queue") String topic,
                              @RequestParam String message) {
        try {
            mqttMessageSender.sendMessage(topic, message);
            return "Сообщение отправлено в MQTT: " + message;
        } catch (MqttException e) {
            e.printStackTrace();
            return "Ошибка при отправке сообщения: " + e.getMessage();
        }
    }
}
