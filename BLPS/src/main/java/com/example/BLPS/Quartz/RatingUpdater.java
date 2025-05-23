package com.example.BLPS.Quartz;

import com.example.BLPS.Entities.Application;
import com.example.BLPS.Repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class RatingUpdater {

    //private final PlatformTransactionManager transactionManager;

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationRepository applicationRepository;
    @Autowired
    public RatingUpdater(JdbcTemplate jdbcTemplate, ApplicationRepository applicationRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationRepository = applicationRepository;
    }
    @Transactional
    public void updateRatings() {
        try {
            System.out.println("⏰ [Scheduled Quartz Task] Updating app ratings at " + java.time.LocalDateTime.now());
            List<Map<String, Object>> apps = jdbcTemplate.queryForList("SELECT id, rating FROM applications");

            // Возможные изменения и веса (чем ближе к 0 — тем выше шанс)
            float[] changes = {-0.5f, -0.4f, -0.3f, -0.2f, -0.1f, 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
            double[] weights = {1, 2, 4, 6, 8, 10, 8, 6, 4, 2, 1}; // сумма: 52

            double totalWeight = 0;
            for (double w : weights) totalWeight += w;

            for (Map<String, Object> app : apps) {
                Long id = ((Number) app.get("id")).longValue();
                Float oldRating = app.get("rating") != null ? ((Number) app.get("rating")).floatValue() : 0.0f;

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

                System.out.printf("🔄 App ID %d | Old: %.2f → New: %.2f (Δ %.1f)%n",
                        ((Number) app.get("id")).longValue(),
                        oldRating,
                        newRating,
                        change
                );


                // app.setRating(newRating);
                // System.out.println("New updated rating: "+app.getRating());
                //System.out.println("New full app: "+app);
               // applicationRepository.save(app);
                jdbcTemplate.update("UPDATE applications SET rating = ? WHERE id = ?", newRating, id);
            }

            System.out.println("✅ Daily rating update completed");

            // Коммитим транзакцию
            // transactionManager.commit(status);
        } catch (Exception e) {
            // Откатываем транзакцию в случае ошибки
            // transactionManager.rollback(status);
            throw new RuntimeException("Failed to update ratings: " + e.getMessage(), e);
        }
    }
    }

