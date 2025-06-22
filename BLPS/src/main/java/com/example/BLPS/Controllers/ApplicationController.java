package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Exceptions.AppNotFoundException;
import com.example.BLPS.Exceptions.AppsNotFoundException;
import com.example.BLPS.Exceptions.PlatformNotFoundException;
import com.example.BLPS.Service.ApplicationService;
import com.example.BLPS.Service.DeveloperService;
import com.example.BLPS.camunda.RestMethods;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final DeveloperService developerService;
    private final RestMethods restMethods;

    private static final String CAMUNDA_URL = "http://localhost:8085/engine-rest/process-definition/key/application_catalog_process/";

    @PostMapping("/start")
    public ResponseEntity<?> startProcess() {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, null);

        String status = (String) result.getOrDefault("status", "UNKNOWN");
        String processInstanceId = (String) result.get("processInstanceId");

        if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", status,
                    "error", result.getOrDefault("error", "Failed to load categories"),
                    "processInstanceId", result.get("processInstanceId")
            ));
        }

        return ResponseEntity.ok(Map.of("processInstanceId", processInstanceId));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(@RequestParam String processInstanceId) {
        try {
            String json = (String) restMethods.getVariableByProcessId(processInstanceId, "appsListJson");
            ObjectMapper mapper = new ObjectMapper();
            List<CategoryDto> categories = mapper.readValue(json, new TypeReference<>() {
            });

            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", "Failed to parse apps JSON: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/changePlatform")
    public ResponseEntity<Map<String, Object>> changePlatform(
            @RequestParam String platform,
            @RequestParam String processInstanceId
    ) {
        try {

            restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("selectedAction", "changePlatform"));
            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("currentPlatform", platform));

            String status = (String) result.getOrDefault("status", "UNKNOWN");

            if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", status,
                                "error", result.getOrDefault("error", "Couldn't change platform"),
                                "processInstanceId", processInstanceId
                        ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", result.getOrDefault("message", "Platform changed"),
                    "processInstanceId", processInstanceId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "FAILED",
                            "error", e.getMessage(),
                            "processInstanceId", processInstanceId
                    ));
        }
    }

    // Поиск приложения по названию (различные варианты совпадений)
    @GetMapping("/searchByName")
    public ResponseEntity<?> searchApplications(@RequestParam String name) {
        Object result = applicationService.searchApplications(name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getApp")
    public ResponseEntity<?> getApp(@RequestParam Long id, @RequestParam String processInstanceId) {
        try {
            restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("selectedAction", "clickApp"));
            Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, Map.of("selectedAppId", id));

            String status = (String) result.getOrDefault("status", "UNKNOWN");

            if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", status,
                                "error", result.getOrDefault("error", "Couldn't get app"),
                                "processInstanceId", processInstanceId
                        ));
            }

            try {
                String json = (String) restMethods.getVariableByProcessId(processInstanceId, "appJson");
                ObjectMapper mapper = new ObjectMapper();
                ApplicationDtoDetailed app = mapper.readValue(json, new TypeReference<>() {
                });

                return ResponseEntity.ok(app);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "status", "FAILED",
                        "error", "Failed to parse apps JSON: " + e.getMessage()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "FAILED",
                            "error", e.getMessage(),
                            "processInstanceId", processInstanceId
                    ));
        }
    }

    @GetMapping("/exactSearch")
    public ResponseEntity<?> exactSearch(@RequestParam String name) {
        try {
            return ResponseEntity.ok(applicationService.exactSearch(name));
        } catch (AppsNotFoundException e) {
            return ResponseEntity.ok(new NotFoundDto("Приложение с названием \"" + name + "\" не найдено. Вот приложения, которые могут вам понравиться", applicationService.getRecommendedApplications()));
        }
    }

    @PostMapping("/end")
    public ResponseEntity<?> endProcess(@RequestParam String processInstanceId) {
        Map<String, Object> result = restMethods.completeTaskAndWaitForResult(processInstanceId, null);

        String status = (String) result.getOrDefault("status", "UNKNOWN");

        if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", status,
                    "error", result.getOrDefault("error", "Failed to end process"),
                    "processInstanceId", result.get("processInstanceId")
            ));
        }

        return ResponseEntity.ok("Process " + processInstanceId + " ended successfully");
    }

    @GetMapping("/getDevs")
    public ResponseEntity<List<DeveloperDto>> getAllDevelopers() {
        return ResponseEntity.ok(developerService.getAllDevelopers());
    }

    @GetMapping("/applications")
    public List<ApplicationDto> getApplications() {
        return new ArrayList<>(applicationService.getAllApplications());
    }
}
