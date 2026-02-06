package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Exceptions.ApplicationNotPendingModerationException;
import com.example.BLPS.camunda.RestMethods;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RestMethods restMethods;
    private static final String CAMUNDA_URL = "http://localhost:8085/engine-rest/process-definition/key/admin_process/";

    @PostMapping("/start")
    public ResponseEntity<?> startProcess() {
        String processInstanceId = restMethods.startProcess(CAMUNDA_URL, null);
        return ResponseEntity.ok(Map.of("processInstanceId", processInstanceId));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApplications(@RequestParam String processInstanceId) {
        try {
            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("action", "pending"));

            String json = (String) restMethods.getVariableByProcessId(processInstanceId, "pendingAppsJson");
            ObjectMapper mapper = new ObjectMapper();

            List<DeveloperApplicationDto> pendingApps = mapper.readValue(json, new TypeReference<List<DeveloperApplicationDto>>() {
            });
            return ResponseEntity.ok(pendingApps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", "Failed to get apps JSON: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id, @RequestParam String processInstanceId) {
        Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("applicationId", id, "action", "approve"));

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
    public ResponseEntity<Map<String, Object>> rejectApplication(@PathVariable Long id, @RequestParam String processInstanceId) {
        Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("applicationId", id, "action", "reject"));

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

    @PostMapping("/ban")
    public ResponseEntity<Map<String, Object>> banDeveloper(
            @RequestBody BanRequestDto banRequest,
            @RequestParam String processInstanceId) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("developerId", banRequest.getDeveloperId());
        variables.put("action", "ban");
        variables.put("banReason", banRequest.getReason());

        Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, variables);

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
                "processInstanceId", result.get("processInstanceId"),
                "reason", banRequest.getReason()
        ));
    }

    @PostMapping("/unban")
    public ResponseEntity<Map<String, Object>> unbanDeveloper(@RequestBody UnbanRequestDto unbanRequest, @RequestParam String processInstanceId) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("developerId", unbanRequest.getDeveloperId());
        variables.put("action", "unban");
        variables.put("unbanMessage", unbanRequest.getMessage() != null ? unbanRequest.getMessage() : "Developer unbanned");

        Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, variables);

        String status = (String) result.getOrDefault("status", "UNKNOWN");

        if ("FAILED".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", status,
                    "error", result.getOrDefault("error", "Unknown error"),
                    "processInstanceId", result.get("processInstanceId")
            ));
        }

        if ("TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(Map.of(
                    "status", "TIMEOUT",
                    "error", result.get("error"),
                    "processInstanceId", result.get("processInstanceId")
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "UNBAN",
                "message", result.getOrDefault("unbanMessage", "Developer successfully unbanned"),
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