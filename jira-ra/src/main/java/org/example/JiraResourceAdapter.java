package org.example;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;

public class JiraResourceAdapter implements ResourceAdapter {

    @Override
    public void start(BootstrapContext ctx) {
        System.out.println("JiraResourceAdapter started.");
    }

    @Override
    public void stop() {
        System.out.println("JiraResourceAdapter stopped.");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory mef, jakarta.resource.spi.ActivationSpec as) throws ResourceException {
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory mef, jakarta.resource.spi.ActivationSpec as) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }
}
