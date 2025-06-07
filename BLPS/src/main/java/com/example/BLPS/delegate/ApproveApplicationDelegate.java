package com.example.BLPS.delegate;

import com.example.BLPS.Entities.Status;
import com.example.BLPS.Service.ApplicationService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApproveApplicationDelegate implements JavaDelegate {
    @Autowired
    private ApplicationService applicationService;

    @Override
    public void execute(DelegateExecution execution) {
        Long appId = (Long) execution.getVariable("applicationId");
        applicationService.updateModerationStatus(appId, Status.APPROVED);
    }
}

