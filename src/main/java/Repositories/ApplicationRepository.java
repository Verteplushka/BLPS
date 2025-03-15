package Repositories;


import entities.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    // Найти топовые приложения по рейтингу
    List<ApplicationEntity> findTop10ByOrderByRatingDesc();

    // Найти рекомендованные приложения
    List<ApplicationEntity> findByIsRecommendedTrue();

    // Найти приложения по категории
    List<ApplicationEntity> findByCategoryIgnoreCase(String category);
    // Поиск по названию (игнорирует регистр)
    List<ApplicationEntity> findByNameContainingIgnoreCase(String name);

    // Фильтр по категории и платформе
    List<ApplicationEntity> findByCategoryAndPlatform(String category, String platform);

}
