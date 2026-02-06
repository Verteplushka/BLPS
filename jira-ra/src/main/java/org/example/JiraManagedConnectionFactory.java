package org.example;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

public class JiraManagedConnectionFactory implements ManagedConnectionFactory {
    private String jiraBaseUrl;
    private String username;
    private String password;

    public void setJiraBaseUrl(String jiraBaseUrl) {
        this.jiraBaseUrl = jiraBaseUrl;
    }

    public void setUserPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new JiraConnectionFactory(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        throw new ResourceException("ConnectionManager is required");
    }

    @Override
    public ManagedConnection createManagedConnection(javax.security.auth.Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
//        return new JiraManagedConnection(jiraBaseUrl, authToken);
        return new JiraManagedConnection(jiraBaseUrl, username, password);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JiraManagedConnectionFactory;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
