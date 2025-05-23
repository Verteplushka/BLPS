package org.example;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;

public class JiraManagedConnection implements ManagedConnection {
    private final String jiraBaseUrl;
    private String username;
    private String password;
    private JiraConnectionImpl connectionImpl;

    public JiraManagedConnection(String jiraBaseUrl, String username, String password) {
        this.jiraBaseUrl = jiraBaseUrl;
        this.username = username;
        this.password = password;
        this.connectionImpl = new JiraConnectionImpl(jiraBaseUrl, username, password);
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return connectionImpl;
    }

    @Override
    public void destroy() throws ResourceException {
    }

    @Override
    public void cleanup() throws ResourceException {
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
    }
}
