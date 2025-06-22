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

    private final RestMethods restMethods;

    private static final String CAMUNDA_URL = "http://localhost:8085/engine-rest/process-definition/key/admin_process/";

    @GetMapping("/pending")
    public ResponseEntity<List<ApplicationDtoDetailed>> getPendingApplications() {
        List<ApplicationDtoDetailed> pendingApps = applicationService.getApplicationsByStatus(Status.ADMIN_MODERATION);
        return ResponseEntity.ok(pendingApps);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id) {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, Map.of("applicationId", id, "action", "approve"));

        String status = (String) result.getOrDefault("status", "UNKNOWN");
        System.out.println();

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
    public ResponseEntity<Map<String, Object>> rejectApplication(@PathVariable Long id) {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, Map.of("applicationId", id, "action", "reject"));

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
                "status", "REJECTED",
                "message", result.getOrDefault("rejectedMessage", "Application rejected"),
                "processInstanceId", result.get("processInstanceId")
        ));
    }

    @PostMapping("/ban/{developerId}")
    public ResponseEntity<Map<String, Object>> banDeveloper(@PathVariable Integer developerId) {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, Map.of("developerId", developerId, "action", "ban"));

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
                "status", "BAN",
                "message", result.getOrDefault("banMessage", "Developer successfully banned"),
                "processInstanceId", result.get("processInstanceId")
        ));
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