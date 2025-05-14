package com.example.BLPS.Quartz;

import com.example.BLPS.Entities.Application;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Service.DeveloperService;
import com.example.BLPS.Service.PlatformService;
import com.example.BLPS.Service.TagService;
import com.example.BLPS.config.MqttMessageSender;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import com.example.BLPS.Repositories.ApplicationRepository;
//import org.springframework.transaction.support.TransactionDefinition;
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
public class UpdateRatingsJob implements Job {


    private final ApplicationRepository applicationRepository;
    private final PlatformTransactionManager transactionManager;
    @Autowired
    public UpdateRatingsJob(ApplicationRepository applicationRepository,PlatformTransactionManager transactionManager) {
        this.applicationRepository = applicationRepository;
        this.transactionManager = transactionManager;
    }
//    @Autowired
//    private  ApplicationRepository applicationRepository;
//    @Autowired
//    private  PlatformTransactionManager transactionManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Начинаем транзакцию
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            System.out.println("⏰ [Scheduled Quartz Task] Updating app ratings at " + java.time.LocalDateTime.now());
            List<Application> apps = applicationRepository.findAll();

            // Возможные изменения и веса (чем ближе к 0 — тем выше шанс)
            float[] changes = {-0.5f, -0.4f, -0.3f, -0.2f, -0.1f, 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
            double[] weights = {1, 2, 4, 6, 8, 10, 8, 6, 4, 2, 1}; // сумма: 52

            double totalWeight = 0;
            for (double w : weights) totalWeight += w;

            for (Application app : apps) {
                Float oldRating = app.getRating() != null ? app.getRating() : 0.0f;

                // Выбираем случайное изменение с учётом веса
                double rand = Math.random() * totalWeight;
                double cumulative = 0;
                Float change = 0f;

                for (int i = 0; i < changes.length; i++) {
                    cumulative += weights[i];
                    if (rand <= cumulative) {
                        change = changes[i];
                        break;
                    }
                }

                Float newRating = Math.max(0.0f, Math.min(5.0f, oldRating + change));

                System.out.printf("🔄 App ID %d | %s | Old: %.2f → New: %.2f (Δ %.1f)%n",
                        app.getId(),
                        app.getName(),
                        oldRating,
                        newRating,
                        change
                );

                app.setRating(newRating);
                System.out.println("New updated rating: "+app.getRating());
                applicationRepository.save(app);
            }

            System.out.println("✅ Daily rating update completed");

            // Коммитим транзакцию
            transactionManager.commit(status);
        } catch (Exception e) {
            // Откатываем транзакцию в случае ошибки
            transactionManager.rollback(status);
            throw new JobExecutionException("Failed to update ratings: " + e.getMessage(), e);
        }
    }
}
