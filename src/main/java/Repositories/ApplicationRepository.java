package Repositories;

import Entities.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    // Поиск по названию (игнорирует регистр)
    List<ApplicationEntity> findByNameContainingIgnoreCase(String name);

    // Фильтр по категории и платформе
    List<ApplicationEntity> findByCategoryAndPlatform(String category, String platform);
}