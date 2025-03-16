package com.example.BLPS.Repositories;

import com.example.BLPS.Entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findTop10ByOrderByRatingDesc();

    List<Application> findByIsRecommendedTrue();

    List<Application> findByNameContainingIgnoreCase(String name);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p.name = :platform")
    List<Application> findByPlatform(@Param("platform") String platform);
}

