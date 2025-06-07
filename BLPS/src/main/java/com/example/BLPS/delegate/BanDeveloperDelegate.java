package com.example.BLPS.delegate;

import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("banDeveloperDelegate")
@RequiredArgsConstructor
public class BanDeveloperDelegate implements JavaDelegate {
    private final ApplicationService applicationService;

    @Override
    public void execute(DelegateExecution execution) {
        Integer devId = (Integer) execution.getVariable("developerId");
        applicationService.rejectAllApplicationsByDeveloper(devId);
    }
}

