package org.example;

import jakarta.resource.spi.ConnectionManager;

import jakarta.resource.ResourceException;

public class JiraConnectionFactory {
    private final JiraManagedConnectionFactory mcf;
    private final ConnectionManager connectionManager;

    public JiraConnectionFactory(JiraManagedConnectionFactory mcf, ConnectionManager connectionManager) {
        this.mcf = mcf;
        this.connectionManager = connectionManager;
    }

    public JiraConnection getConnection() throws ResourceException {
        return (JiraConnection) connectionManager.allocateConnection(mcf, null);
    }
}
