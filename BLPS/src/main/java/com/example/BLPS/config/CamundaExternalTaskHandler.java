package com.example.BLPS.config;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CategoryDto;
import com.example.BLPS.Entities.Status;
import com.example.BLPS.Quartz.RatingUpdater;
import com.example.BLPS.Service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaExternalTaskHandler {
    @Autowired
    private RatingUpdater ratingUpdater;

    private final JdbcTemplate jdbcTemplate;
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
                        applicationService.rejectAllApplicationsByDeveloper(devId);

                        variables.put("banStatus", "SUCCESS");
                        variables.put("banMessage", "Dev banned successfully");

                    } catch (Exception e) {
                        variables.put("banStatus", "FAILED");
                        variables.put("banError", e.getMessage());
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
        client.subscribe("changePlatform")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        String currentPlatform = externalTask.getVariable("currentPlatform").toString();
                        System.out.println(currentPlatform);
                        applicationService.changePlatform(currentPlatform);

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
        client.subscribe("returnAppsList")
                .handler((externalTask, externalTaskService) -> {
                    try {
                        List<CategoryDto> categories = applicationService.getApplicationsByCategories();
                        applicationService.getApplicationsByCategories();

                        ObjectMapper mapper = new ObjectMapper();
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
                        Long currentPlatform = Long.valueOf(externalTask.getVariable("selectedAppId").toString());
                        ApplicationDtoDetailed app = applicationService.getApp(currentPlatform);

                        ObjectMapper mapper = new ObjectMapper();
                        String appsJson = mapper.writeValueAsString(app);

                        ObjectValue appsValue = Variables
                                .objectValue(appsJson)
                                .serializationDataFormat("application/json")
                                .create();

                        externalTaskService.complete(externalTask, Map.of("appJson", appsValue));

                    } catch (Exception e) {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("showAppStatus", "FAILED");
                        variables.put("showAppError", e.getMessage());
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

    }
}
