package com.example.BLPS.config;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Entities.Status;
import com.example.BLPS.Quartz.RatingUpdater;
import com.example.BLPS.Service.ApplicationService;
import com.example.BLPS.Service.DeveloperService;
import com.example.BLPS.Utils.UserXmlReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.variable.Variables.booleanValue;

@Component
@RequiredArgsConstructor
public class CamundaExternalTaskHandler {
    @Autowired
    private RatingUpdater ratingUpdater;

    private final ApplicationService applicationService;

    private final ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl("http://localhost:8085/engine-rest")
            .asyncResponseTimeout(10000)
            .build();

    @PostConstruct
    public void subscribeTasks() {
        client.subscribe("approveApplication")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Long appId = Long.valueOf(externalTask.getVariable("applicationId").toString());
                        applicationService.updateModerationStatus(appId, Status.APPROVED);

                        variables.put("approvalStatus", "SUCCESS");
                        variables.put("approvalMessage", "Application approved successfully");

                    } catch (Exception e) {
                        variables.put("approvalStatus", "FAILED");
                        variables.put("approvalError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }

                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("banDeveloper")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Integer devId = Integer.valueOf(externalTask.getVariable("developerId").toString());
                        String reason = (String) externalTask.getVariable("banReason");

                        applicationService.rejectAllApplicationsByDeveloper(devId, reason);

                        variables.put("banStatus", "SUCCESS");
                        variables.put("banMessage", "Developer banned successfully");
                    } catch (Exception e) {
                        variables.put("banStatus", "FAILED");
                        variables.put("banError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0,
                                0
                        );
                    }
                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("rejectApplication")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Long appId = Long.valueOf(externalTask.getVariable("applicationId").toString());
                        applicationService.updateModerationStatus(appId, Status.REJECTED);

                        variables.put("rejectStatus", "SUCCESS");
                        variables.put("rejectMessage", "Application rejected successfully");

                    } catch (Exception e) {
                        variables.put("rejectStatus", "FAILED");
                        variables.put("rejectError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }

                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("unbanDeveloper")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Integer devId = Integer.valueOf(externalTask.getVariable("developerId").toString());
                        String reason = (String) externalTask.getVariable("unbanMessage");

                        applicationService.unbanDeveloper(devId, reason);

                        variables.put("status", "SUCCESS");
                        variables.put("unbanMessage", "Developer successfully unbanned");
                    } catch (Exception e) {
                        variables.put("status", "FAILED"); // контроллер ожидает ключ "status"
                        variables.put("error", e.getMessage()); // и "error"
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0,
                                0
                        );

                    }

                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("changePlatform")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        String currentPlatform = externalTask.getVariable("currentPlatform").toString();
                        applicationService.changePlatform(currentPlatform);

                        variables.put("changeStatus", "SUCCESS");
                        variables.put("changeMessage", "Platform changed successfully");
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "PLATFORM_DOES_NOT_EXIST", "Chosen platform does not exist");
                    }
                })
                .open();
        client.subscribe("showPlatformError")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    String currentPlatform = externalTask.getVariable("currentPlatform").toString();

                    variables.put("changeStatus", "FAILED");
                    variables.put("changeMessage", currentPlatform + " platform does not exist");

                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("selectDefaultPlatform")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        applicationService.changePlatform("phone");
                        System.out.println("Platform changed");

                        variables.put("changeStatus", "SUCCESS");
                        variables.put("changeMessage", "Platform changed successfully");

                    } catch (Exception e) {
                        variables.put("changeStatus", "FAILED");
                        variables.put("changeError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }

                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("getTop10")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        List<ApplicationDto> apps = applicationService.getTop10Applications();

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(apps);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("top10", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("status", "FAILED");
                        variables.put("error", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("getRecommended")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        List<ApplicationDto> apps = applicationService.getRecommendedApplications();

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(apps);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("recommended", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("status", "FAILED");
                        variables.put("error", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("getByTags")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        List<CategoryDto> apps = applicationService.getApplicationsByTags();

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(apps);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("byTags", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("status", "FAILED");
                        variables.put("error", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("mergeResults")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonTop10 = externalTask.getVariable("top10");
                        String jsonRecommended = externalTask.getVariable("recommended");
                        String jsonByTags = externalTask.getVariable("byTags");

                        List<ApplicationDto> top10 = mapper.readValue(jsonTop10, new TypeReference<List<ApplicationDto>>() {
                        });
                        List<ApplicationDto> recommended = mapper.readValue(jsonRecommended, new TypeReference<List<ApplicationDto>>() {
                        });
                        List<CategoryDto> byTags = mapper.readValue(jsonByTags, new TypeReference<List<CategoryDto>>() {
                        });

                        List<CategoryDto> categories = new ArrayList<>();
                        categories.add(new CategoryDto("popular", top10));
                        categories.add(new CategoryDto("recommended", recommended));
                        categories.addAll(byTags);

                        String appsJson = mapper.writeValueAsString(categories);
                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("appsListJson", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("status", "FAILED");
                        variables.put("error", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("showAppInfo")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        Long appId = Long.valueOf(externalTask.getVariable("selectedAppId").toString());
                        ApplicationDtoDetailed app = applicationService.getApp(appId);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(app);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("showAppStatus", "SUCCESS", "appJson", appsValue));
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "APP_DOES_NOT_EXIST", "Chosen app does not exist");
                    }
                })
                .open();
        client.subscribe("showAppError")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    Long appId = Long.valueOf(externalTask.getVariable("selectedAppId").toString());

                    variables.put("showAppStatus", "FAILED");
                    variables.put("showAppMessage", "App with id " + appId + " does not exist");

                    externalTaskService.complete(externalTask, variables);
                })
                .open();
        client.subscribe("searchAppByExactMatch")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String name = externalTask.getVariable("searchQuery").toString();
                        ExactMatchDto app = applicationService.searchExactMatch(name);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(app);

                        ObjectValue appValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        // restMethods.setParams(externalTask.getProcessInstanceId(), Map.of("foundExactApp", booleanValue(app != null)));

                        externalTaskService.complete(externalTask, Map.of("foundExactApp", booleanValue(app != null), "appJson", appValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("searchAppByExactMatchStatus", "FAILED");
                        variables.put("searchAppByExactMatchError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("searchAppWithSimilarName")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String name = externalTask.getVariable("searchQuery").toString();
                        ExactMatchDto app = applicationService.searchFuzzyMatch(name);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(app);

                        ObjectValue appValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("appJson", appValue, "foundSimilarApp", app != null));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("searchAppWithSimilarNameStatus", "FAILED");
                        variables.put("searchAppWithSimilarNameError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("searchAppsWithSimilarName")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String name = externalTask.getVariable("searchQuery").toString();
                        List<ApplicationDto> apps = applicationService.searchPartialMatch(name);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(apps);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("appsListJson", appsValue, "foundSimilarApps", apps != null));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("searchAppsWithSimilarNameStatus", "FAILED");
                        variables.put("searchAppsWithSimilarNameError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("showRecommendedApps")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String name = externalTask.getVariable("searchQuery").toString();
                        NotFoundDto apps = applicationService.nothingFound(name);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(apps);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("appsListJson", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("showRecommendedAppsStatus", "FAILED");
                        variables.put("showRecommendedAppsError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("updateRatings")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        ratingUpdater.updateRatings();
                        externalTaskService.complete(externalTask);
                    } catch (Exception e) {
                        externalTaskService.handleFailure(
                                externalTask,
                                "Failed to update ratings: " + e.getMessage(),
                                e.toString(),
                                0,
                                1000
                        );
                    }
                })
                .open();
        client.subscribe("showDevelopersApps")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String username = externalTask.getVariable("username").toString();
                        List<DeveloperApplicationDto> apps = applicationService.getAllApplicationsForDeveloperByName(username);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(apps);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("devAppsListJson", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("showDevelopersAppsStatus", "FAILED");
                        variables.put("showDevelopersAppsError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("saveApp")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String username = externalTask.getVariable("username").toString();
                        CreateApplicationDto dto = new CreateApplicationDto();

                        dto.setName((String) externalTask.getVariable("name"));
                        dto.setDescription((String) externalTask.getVariable("description"));
                        dto.setPrice(Float.parseFloat(externalTask.getVariable("price").toString()));
                        dto.setImageUrl((String) externalTask.getVariable("imageUrl"));
                        dto.setHasPaidContent(Boolean.parseBoolean(externalTask.getVariable("hasPaidContent").toString()));
                        dto.setHasAds(Boolean.parseBoolean(externalTask.getVariable("hasAds").toString()));
                        dto.setIsEditorsChoice(Boolean.parseBoolean(externalTask.getVariable("isEditorsChoice").toString()));
                        dto.setAgeLimit(Integer.parseInt(externalTask.getVariable("ageLimit").toString()));
                        dto.setIsRecommended(Boolean.parseBoolean(externalTask.getVariable("isRecommended").toString()));

                        // Преобразуем comma-separated строку в список Integer
                        String platformsRaw = (String) externalTask.getVariable("platformIds");
                        List<Integer> platformIds = Arrays.stream(platformsRaw.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                        dto.setPlatformIds(platformIds);

                        String tagsRaw = (String) externalTask.getVariable("tagIds");
                        List<Integer> tagIds = Arrays.stream(tagsRaw.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                        dto.setTagIds(tagIds);

                        // Пример логики: сохраняем приложение
                        ApplicationDtoDetailed savedApp = applicationService.createApplication(username, dto);

                        externalTaskService.complete(externalTask, Map.of("saveAppStatus", "COMPLETED", "savedApp", savedApp));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("saveAppStatus", "FAILED");
                        variables.put("saveAppError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0,
                                0
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("updateApp")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String username = externalTask.getVariable("username").toString();
                        Long appId = Long.valueOf(externalTask.getVariable("appId").toString());
                        CreateApplicationDto dto = new CreateApplicationDto();

                        dto.setName((String) externalTask.getVariable("name"));
                        dto.setDescription((String) externalTask.getVariable("description"));
                        dto.setPrice(Float.parseFloat(externalTask.getVariable("price").toString()));
                        dto.setImageUrl((String) externalTask.getVariable("imageUrl"));
                        dto.setHasPaidContent(Boolean.parseBoolean(externalTask.getVariable("hasPaidContent").toString()));
                        dto.setHasAds(Boolean.parseBoolean(externalTask.getVariable("hasAds").toString()));
                        dto.setIsEditorsChoice(Boolean.parseBoolean(externalTask.getVariable("isEditorsChoice").toString()));
                        dto.setAgeLimit(Integer.parseInt(externalTask.getVariable("ageLimit").toString()));
                        dto.setIsRecommended(Boolean.parseBoolean(externalTask.getVariable("isRecommended").toString()));

                        // Преобразуем comma-separated строку в список Integer
                        String platformsRaw = (String) externalTask.getVariable("platformIds");
                        List<Integer> platformIds = Arrays.stream(platformsRaw.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                        dto.setPlatformIds(platformIds);

                        String tagsRaw = (String) externalTask.getVariable("tagIds");
                        List<Integer> tagIds = Arrays.stream(tagsRaw.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                        dto.setTagIds(tagIds);

                        // Пример логики: сохраняем приложение
                        ApplicationDtoDetailed updatedApp = applicationService.updateApplication(username, appId, dto);

                        externalTaskService.complete(externalTask, Map.of("saveAppStatus", "COMPLETED", "updatedApp", updatedApp));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("saveAppStatus", "FAILED");
                        variables.put("saveAppError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0,
                                0
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
        client.subscribe("deleteApp")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String username = externalTask.getVariable("username").toString();
                        Long appId = Long.valueOf(externalTask.getVariable("appId").toString());
                        applicationService.deleteApplicationByIdAndDevName(appId, username);

                        externalTaskService.complete(externalTask, Map.of("deleteAppResult", "App with id " + appId + " has been successfully deleted"));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("deleteAppStatus", "FAILED");
                        variables.put("deleteAppError", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                        externalTaskService.complete(externalTask, variables);
                    }
                })
                .open();
    }
}
