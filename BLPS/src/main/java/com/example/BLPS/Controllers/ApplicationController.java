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

    @PostMapping("/changePlatform")
    public ResponseEntity<Map<String, Object>> changePlatform(@RequestParam String platform) {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, null);

        String status = (String) result.getOrDefault("status", "UNKNOWN");

        if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", status,
                            "error", result.getOrDefault("error", "Couldn't change platform"),
                            "processInstanceId", result.get("processInstanceId")
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "APPROVED",
                "message", result.getOrDefault("approvalMessage", "Application approved"),
                "processInstanceId", result.get("processInstanceId")
        ));

//        try {
//            applicationService.changePlatform(platform);
//            return ResponseEntity.ok().build();
//        } catch (PlatformNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() throws InterruptedException {
        Map<String, Object> result = restMethods.startProcessAndWaitForResult(CAMUNDA_URL, null);

        String status = (String) result.getOrDefault("status", "UNKNOWN");

        if ("FAILED".equalsIgnoreCase(status) || "TIMEOUT".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", status,
                    "error", result.getOrDefault("error", "Failed to load categories"),
                    "processInstanceId", result.get("processInstanceId")
            ));
        }

        Thread.sleep(5000);

        try {
            String json = (String) restMethods.getVariableByProcessId((String) result.get("processInstanceId"), "appsListJson");
            ObjectMapper mapper = new ObjectMapper();
            List<CategoryDto> categories = mapper.readValue(json, new TypeReference<>() {});

            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "error", "Failed to parse apps JSON: " + e.getMessage()
            ));
        }
        //        return ResponseEntity.ok(applicationService.getApplicationsByCategories());
    }

    // Поиск приложения по названию (различные варианты совпадений)
    @GetMapping("/searchByName")
    public ResponseEntity<?> searchApplications(@RequestParam String name) {
        Object result = applicationService.searchApplications(name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getApp")
    public ResponseEntity<ApplicationDtoDetailed> getApp(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(applicationService.getApp(id));
        } catch (AppNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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

    @GetMapping("/getDevs")
    public ResponseEntity<List<DeveloperDto>> getAllDevelopers() {
        return ResponseEntity.ok(developerService.getAllDevelopers());
    }

    @GetMapping("/applications")
    public List<ApplicationDto> getApplications() {
        return new ArrayList<>(applicationService.getAllApplications());
    }
}
