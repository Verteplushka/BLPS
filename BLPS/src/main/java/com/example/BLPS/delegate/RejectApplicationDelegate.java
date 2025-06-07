package com.example.BLPS.delegate;

import com.example.BLPS.Entities.Status;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("rejectApplicationDelegate")
@RequiredArgsConstructor
public class RejectApplicationDelegate implements JavaDelegate {
    private final ApplicationService applicationService;

    @Override
    public void execute(DelegateExecution execution) {
        Long appId = (Long) execution.getVariable("applicationId");
        applicationService.updateModerationStatus(appId, Status.REJECTED);
    }
}

