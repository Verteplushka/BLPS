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
        // Для approveApplication
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
                    Integer devId = externalTask.getVariable("developerId");
                    System.out.println("ban");
                    applicationService.rejectAllApplicationsByDeveloper(devId);
                    externalTaskService.complete(externalTask);
                })
                .open();
        client.subscribe("rejectApplication")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Long appId = externalTask.getVariable("applicationId");
                    System.out.println("reject");
                    jdbcTemplate.update(
                            "UPDATE applications SET moderation_status = ? WHERE id = ?",
                            "REJECTED",
                            appId
                    );
                    externalTaskService.complete(externalTask);
                })
                .open();
    }
}
