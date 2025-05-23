package org.example;

import jakarta.resource.ResourceException;

public class JiraConnectionImpl implements JiraConnection {
    public JiraConnectionImpl(String jiraBaseUrl, String authToken) {
    }

    public String createModerationTask(int appId, String appName, String developer) throws ResourceException {
        System.out.println("Creating Jira task for app " + appName);
        return "TASK-" + appId; // заглушка
    }

    public String getTaskStatus(String taskId) throws ResourceException {
        return "In Progress"; // заглушка
    }

    public void close() {
        System.out.println("Closing Jira connection");
    }
}
