package org.example;

import jakarta.resource.ResourceException;

public interface JiraConnection extends java.io.Closeable {
    String createModerationTask(int appId, String appName, String developer) throws ResourceException;
    String getTaskStatus(String taskId) throws ResourceException;
    void close();
}
