package com.example.BLPS.Quartz;

import com.example.BLPS.Entities.Application;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Service.DeveloperService;
import com.example.BLPS.Service.PlatformService;
import com.example.BLPS.Service.TagService;
import com.example.BLPS.config.MqttMessageSender;
import lombok.NoArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import com.example.BLPS.Repositories.ApplicationRepository;
//import org.springframework.transaction.support.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

@Component
@NoArgsConstructor(force = true)
@Transactional
public class UpdateRatingsJob implements Job {
    @Autowired
    private RatingUpdater ratingUpdater;
    @Override
    public void execute(JobExecutionContext context) {
        ratingUpdater.updateRatings();
    }
}
