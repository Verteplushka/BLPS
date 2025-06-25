package org.example;

import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.ExternalTaskClient;
import org.example.Entity.Application;
import org.example.Entity.Status;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CamundaExternalTaskHandler {
    static final JiraAdapterClient jiraAdapterClient;

    static {
        try {
            jiraAdapterClient = new JiraAdapterClient();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    private final ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl("http://localhost:8085/engine-rest")
            .asyncResponseTimeout(10000)
            .build();

    public void subscribeTasks() {
        client.subscribe("checkApp")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Integer appId = Integer.parseInt(externalTask.getVariable("moderationRequest").toString());

                        boolean hasBadWords = Main.checkApp(appId);
                        variables.put("hasBadWords", hasBadWords);
                        variables.put("checkAppStatus", "SUCCESS");
                        variables.put("checkAppMessage", "Application checked successfully");
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        variables.put("checkAppStatus", "FAILED");
                        variables.put("checkAppMessage", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }
                })
                .open();
        client.subscribe("setFailedStatus")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Integer appId = Integer.parseInt(externalTask.getVariable("moderationRequest").toString());
                        Main.updateAppStatus(appId, Status.AUTO_MODERATION_FAILED);

                        variables.put("setFailedStatus", "SUCCESS");
                        variables.put("setFailedMessage", "FailedStatus set successfully");
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        variables.put("setFailedStatus", "FAILED");
                        variables.put("setFailedMessage", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }
                })
                .open();
        client.subscribe("setSuccessStatus")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        Integer appId = Integer.parseInt(externalTask.getVariable("moderationRequest").toString());
                        Main.updateAppStatus(appId, Status.ADMIN_MODERATION);

                        variables.put("setSuccessStatus", "SUCCESS");
                        variables.put("setSuccessMessage", "FailedStatus set successfully");
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        variables.put("setSuccessStatus", "FAILED");
                        variables.put("setSuccessMessage", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }
                })
                .open();
        client.subscribe("createJiraTask")
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = new HashMap<>();
                    try {
                        int appId = Integer.parseInt(externalTask.getVariable("moderationRequest").toString());
                        Application app = Main.getAppFromDatabase(appId);
                        jiraAdapterClient.createModerationTask(appId, app.getName(), app.getDeveloper().getName());

                        variables.put("setSuccessStatus", "SUCCESS");
                        variables.put("setSuccessMessage", "FailedStatus set successfully");
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        variables.put("setSuccessStatus", "FAILED");
                        variables.put("setSuccessMessage", e.getMessage());
                        externalTaskService.handleFailure(
                                externalTask,
                                e.getMessage(),
                                e.toString(),
                                0, // попыток больше не будет
                                0  // без задержки
                        );
                    }
                })
                .open();
    }
}
