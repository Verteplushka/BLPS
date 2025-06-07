package com.example.BLPS.delegate;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Entities.Status;
import com.example.BLPS.Service.ApplicationService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FetchPendingApplicationsDelegate implements JavaDelegate {
    @Autowired
    private ApplicationService applicationService;

    @Override
    public void execute(DelegateExecution execution) {
        List<ApplicationDtoDetailed> apps = applicationService.getApplicationsByStatus(Status.ADMIN_MODERATION);
        execution.setVariable("pendingApps", apps); // если нужно передать в форму
    }
}
