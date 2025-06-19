package com.example.BLPS.config;

import com.example.BLPS.Entities.Status;
import com.example.BLPS.Service.ApplicationService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaExternalTaskHandler {

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
        client.subscribe("selectDefaultPlatform")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        applicationService.changePlatform("phone");

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
        client.subscribe("returnAppsList")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        String currentPlatform = externalTask.getVariable("currentPlatform").toString();
                        System.out.println(currentPlatform);
                        applicationService.getApplicationsByCategories();

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
    }
}
