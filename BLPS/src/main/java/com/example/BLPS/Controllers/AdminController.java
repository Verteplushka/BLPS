package com.example.BLPS.Controllers;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RestTemplate restTemplate;

    @PostMapping("/start-moderation")
    public ResponseEntity<Void> startModerationProcess(@RequestParam Long applicationId,
                                                       @RequestParam Integer developerId) {
        String url = "http://localhost:8085/engine-rest/process-definition/key/moderationProcess/start";

        Map<String, Object> body = Map.of(
                "variables", Map.of(
                        "applicationId", Map.of("value", applicationId),
                        "developerId", Map.of("value", developerId)
                )
        );

        restTemplate.postForEntity(url, body, String.class);
        return ResponseEntity.ok().build();
    }
}
