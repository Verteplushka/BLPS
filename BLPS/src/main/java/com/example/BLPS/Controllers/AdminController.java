package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Entities.Status;
import com.example.BLPS.Exceptions.ApplicationNotPendingModerationException;
import com.example.BLPS.Service.ApplicationService;
import com.example.BLPS.camunda.RestMethods;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationService applicationService;

    private final RestTemplate restTemplate;

    private final RestMethods restMethods;

    private static final String CAMUNDA_URL = "http://localhost:8085/engine-rest/process-definition/key/admin_process/start";

    @GetMapping("/pending")
    public ResponseEntity<List<ApplicationDtoDetailed>> getPendingApplications() {
        List<ApplicationDtoDetailed> pendingApps = applicationService.getApplicationsByStatus(Status.ADMIN_MODERATION);
        return ResponseEntity.ok(pendingApps);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id) {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(id, "approve");

        String status = (String) result.getOrDefault("status", "UNKNOWN");

        if ("FAILED".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", status,
                            "error", result.getOrDefault("error", "Unknown error"),
                            "processInstanceId", result.get("processInstanceId")
                    ));
        }

        if ("TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(Map.of(
                            "status", "TIMEOUT",
                            "error", result.get("error"),
                            "processInstanceId", result.get("processInstanceId")
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "APPROVED",
                "message", result.getOrDefault("approvalMessage", "Application approved"),
                "processInstanceId", result.get("processInstanceId")
        ));
    }


    @PostMapping("/reject/{id}")
    public ResponseEntity<Void> rejectApplication(@PathVariable Long id) {
        Map<String, Object> requestBody = Map.of(
                "variables", Map.of(
                        "applicationId", Map.of("value", id),
                        "action", Map.of("value", "reject")
                )
        );
        restTemplate.postForEntity(CAMUNDA_URL, requestBody, String.class);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ban/{developerId}")
    public ResponseEntity<Void> banDeveloper(@PathVariable Integer developerId) {
        Map<String, Object> requestBody = Map.of(
                "variables", Map.of(
                        "developerId", Map.of("value", developerId),
                        "applicationId", Map.of("value", -1), // фиктивное значение, если требуется
                        "action", Map.of("value", "ban")
                )
        );
        restTemplate.postForEntity(CAMUNDA_URL, requestBody, String.class);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ApplicationNotPendingModerationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotPending(ApplicationNotPendingModerationException e) {
        return e.getMessage();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException e) {
        return e.getMessage();
    }
}