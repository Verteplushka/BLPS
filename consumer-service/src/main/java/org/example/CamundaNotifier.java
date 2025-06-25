package org.example;

import java.net.http.*;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class CamundaNotifier {

    private static final String CAMUNDA_URL = "http://localhost:8085/engine-rest/message";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendModerationMessage(int appId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageName", "ModerationFinished");
            payload.put("businessKey", String.valueOf(appId));

            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> moderationResult = new HashMap<>();
            moderationResult.put("value", appId);
            moderationResult.put("type", "Integer");
            variables.put("moderationRequest", moderationResult);

            payload.put("processVariables", variables);

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CAMUNDA_URL))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(json))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("✅ Message correlated with Camunda");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
