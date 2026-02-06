package com.example.BLPS.Controllers;

import com.example.BLPS.config.JiraAdapterClient;
import com.example.BLPS.config.MqttMessageSender;
import jakarta.resource.ResourceException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/test")
public class TestController {

    private final MqttMessageSender mqttMessageSender;
    private final JiraAdapterClient jiraAdapterClient;

    @Autowired
    public TestController(MqttMessageSender mqttMessageSender, JiraAdapterClient jiraAdapterClient) {
        this.mqttMessageSender = mqttMessageSender;
        this.jiraAdapterClient = jiraAdapterClient;
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

    @PostMapping("/jira_test")
    public String testJiraConnection(@RequestParam int appId,
                                     @RequestParam String name,
                                     @RequestParam String developer) {
        try {
            String taskId = jiraAdapterClient.createModerationTask(appId, name, developer);
            String status = jiraAdapterClient.getStatus(taskId);
            return "Jira task создан: " + taskId + ", статус: " + status;
        } catch (ResourceException e) {
            e.printStackTrace();
            return "Ошибка при взаимодействии с Jira: " + e.getMessage();
        }
    }
}
