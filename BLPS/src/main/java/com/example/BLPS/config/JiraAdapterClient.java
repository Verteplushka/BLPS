package com.example.BLPS.config;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionFactory;
import org.example.JiraConnection;
import org.example.JiraConnectionFactory;
import org.example.JiraManagedConnectionFactory;
import org.springframework.stereotype.Component;

import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;

@Component
public class JiraAdapterClient {

    private final JiraConnectionFactory connectionFactory;

    public JiraAdapterClient() throws ResourceException {
        JiraManagedConnectionFactory mcf = new JiraManagedConnectionFactory();
        mcf.setJiraBaseUrl("http://localhost:8090");
        mcf.setAuthToken("Bearer ATATT3xFfGF0rTZ1PQBTnOwwEyiLVZDM_2IT6hHQD731TyM-KeOOJ6AZi8WZfqIvID9oUHh3WWBjhOs0XC9xEDCQVPHlEyhiUvPmaE9Xr7db5ye46wf4A6dd7p_T_U1Ef_rIhE-GxCEsEehbAxhqgXc2CE0RLbns1ZpuEBiED_zb_fyfsIAkPLg=3E5D3FDA");

        this.connectionFactory = new JiraConnectionFactory(mcf, new DummyConnectionManager());
    }

    public String createModerationTask(int appId, String name, String developer) throws ResourceException {
        try (JiraConnection conn = connectionFactory.getConnection()) {
            return conn.createModerationTask(appId, name, developer);
        }
    }

    public String getStatus(String taskId) throws ResourceException {
        try (JiraConnection conn = connectionFactory.getConnection()) {
            return conn.getTaskStatus(taskId);
        }
    }

    static class DummyConnectionManager implements ConnectionManager {
        @Override
        public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return mcf.createManagedConnection(null, cxRequestInfo).getConnection(null, cxRequestInfo);
        }
    }

}

