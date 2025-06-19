package com.example.BLPS.config;

import com.example.BLPS.Entities.Status;
import com.example.BLPS.Service.ApplicationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CamundaExternalTaskHandler {

    private final ApplicationService applicationService;

    private final ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl("http://localhost:8085/engine-rest")
            .asyncResponseTimeout(10000)
            .build();

    @PostConstruct
    public void subscribeTasks() {
        client.subscribe("approveApplication")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Long appId = externalTask.getVariable("applicationId");
                    System.out.println("approve");
                    applicationService.updateModerationStatus(appId, Status.APPROVED);
                    externalTaskService.complete(externalTask);
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
                    Long appId = externalTask.getVariable("application");
                    System.out.println("reject");
                    applicationService.updateModerationStatus(appId, Status.REJECTED);
                    externalTaskService.complete(externalTask);
                })
                .open();
    }
}
