package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Exceptions.CreateAppFailedException;
import com.example.BLPS.Service.ApplicationService;
import com.example.BLPS.Utils.UserXmlReader;
import com.example.BLPS.camunda.RestMethods;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
public class DeveloperController {

    private final ApplicationService applicationService;
    private final RestMethods restMethods;
    private static final String CAMUNDA_URL = "http://localhost:8085/engine-rest/process-definition/key/developer_process/";

    @PostMapping("/start")
    public ResponseEntity<?> startProcess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, Map.of("username", username));

        String status = (String) result.getOrDefault("status", "UNKNOWN");
        String processInstanceId = (String) result.get("processInstanceId");

        if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", status,
                    "error", result.getOrDefault("error", "Failed to start process"),
                    "processInstanceId", result.get("processInstanceId")
            ));
        }

        return ResponseEntity.ok(Map.of("processInstanceId", processInstanceId));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyApplications(@RequestParam String processInstanceId) {
        try {
            String json = (String) restMethods.getVariableByProcessId(processInstanceId, "devAppsListJson");
            ObjectMapper mapper = new ObjectMapper();

            List<DeveloperApplicationDto> apps = mapper.readValue(json, new TypeReference<List<DeveloperApplicationDto>>() {
            });
            return ResponseEntity.ok(apps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createApplication(@RequestBody CreateApplicationDto request,
                                               @RequestParam String processInstanceId) {
        try {
            restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("selectedAction", "createNew"));
            Map<String, Object> vars = new HashMap<>();
            vars.put("name", request.getName());
            vars.put("description", request.getDescription());
            vars.put("price", Math.round(request.getPrice()));
            vars.put("imageUrl", request.getImageUrl());
            vars.put("hasPaidContent", request.getHasPaidContent());
            vars.put("hasAds", request.getHasAds());
            vars.put("isEditorsChoice", request.getIsEditorsChoice());
            vars.put("ageLimit", request.getAgeLimit());
            vars.put("isRecommended", request.getIsRecommended());
            vars.put("platformIds", String.join(",", request.getPlatformIds().stream().map(String::valueOf).toList()));
            vars.put("tagIds", String.join(",", request.getTagIds().stream().map(String::valueOf).toList()));

            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, vars);

            String status = (String) result.getOrDefault("status", "UNKNOWN");

            if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", status,
                        "error", result.getOrDefault("error", "Couldn't create app"),
                        "processInstanceId", processInstanceId
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "App created",
                    "processInstanceId", processInstanceId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", e.getMessage(),
                    "processInstanceId", processInstanceId
            ));
        }
    }


    @PutMapping("/update/{appId}")
    public ResponseEntity<Map<String, Object>> updateApplication(
            @PathVariable Long appId,
            @RequestBody CreateApplicationDto request,
            @RequestParam String processInstanceId
    ) {
        try {
            restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("selectedAction", "update"));

            Map<String, Object> vars = new HashMap<>();
            vars.put("appId", appId);
            vars.put("name", request.getName());
            vars.put("description", request.getDescription());
            vars.put("price", Math.round(request.getPrice()));
            vars.put("imageUrl", request.getImageUrl());
            vars.put("hasPaidContent", request.getHasPaidContent());
            vars.put("hasAds", request.getHasAds());
            vars.put("isEditorsChoice", request.getIsEditorsChoice());
            vars.put("ageLimit", request.getAgeLimit());
            vars.put("isRecommended", request.getIsRecommended());
            vars.put("platformIds", String.join(",", request.getPlatformIds().stream().map(String::valueOf).toList()));
            vars.put("tagIds", String.join(",", request.getTagIds().stream().map(String::valueOf).toList()));

            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, vars);

            String status = (String) restMethods.getVariableByProcessId(processInstanceId, "showAppNotFoundErrorStatus");

            if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "status", status,
                        "error", result.getOrDefault("error", "Failed to update application"),
                        "processInstanceId", processInstanceId
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Application updated successfully",
                    "processInstanceId", processInstanceId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", "Failed to update application: " + e.getMessage(),
                    "processInstanceId", processInstanceId
            ));
        }
    }

    @DeleteMapping("/delete/{appId}")
    public ResponseEntity<Map<String, Object>> deleteApplication(
            @PathVariable Long appId,
            @RequestParam String processInstanceId
    ) {
        try {
            restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("selectedAction", "delete"));

            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(
                    processInstanceId,
                    Map.of("appId", appId)
            );

            String status = (String) restMethods.getVariableByProcessId(processInstanceId, "showAppNotFoundErrorStatus");

            if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "status", status,
                        "error", result.getOrDefault("error", "Failed to delete application"),
                        "processInstanceId", processInstanceId
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Application deleted successfully",
                    "processInstanceId", processInstanceId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", "Failed to delete application: " + e.getMessage(),
                    "processInstanceId", processInstanceId
            ));
        }
    }

    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endProcess(@RequestParam String processInstanceId) {
        try {
            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, null);

            String status = (String) result.getOrDefault("status", "UNKNOWN");

            if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", status,
                        "error", result.getOrDefault("error", "Failed to end process"),
                        "processInstanceId", processInstanceId
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Process ended successfully",
                    "processInstanceId", processInstanceId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", "Failed to end process: " + e.getMessage(),
                    "processInstanceId", processInstanceId
            ));
        }
    }
}
