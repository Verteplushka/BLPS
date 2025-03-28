package com.example.BLPS.Repositories;

import com.example.BLPS.Entities.Application;
import com.example.BLPS.Entities.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByPlatformsContaining(Platform platform);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p = :platform ORDER BY a.rating DESC LIMIT 10")
    List<Application> findTop10ByPlatformOrderByRatingDesc(Platform platform);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p = :platform AND a.isRecommended = TRUE")
    List<Application> findByPlatformAndIsRecommendedTrue(Platform platform);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p = :platform AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Application> findByNameContainingIgnoreCaseAndPlatform(String name, Platform platform);
}


