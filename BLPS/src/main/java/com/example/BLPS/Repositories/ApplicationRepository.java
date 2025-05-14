package com.example.BLPS.Repositories;

import com.example.BLPS.Entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByPlatformsContainingAndStatus(Platform platform, Status status);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p = :platform AND a.status = :status ORDER BY a.rating DESC LIMIT 10")
    List<Application> findTop10ByPlatformAndStatusOrderByRatingDesc(@Param("platform") Platform platform, @Param("status") Status status);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p = :platform AND a.isRecommended = TRUE AND a.status = :status")
    List<Application> findByPlatformAndIsRecommendedTrueAndStatus(@Param("platform") Platform platform, @Param("status") Status status);

    @Query("SELECT a FROM Application a JOIN a.platforms p WHERE p = :platform AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) AND a.status = :status")
    List<Application> findByNameContainingIgnoreCaseAndPlatformAndStatus(@Param("name") String name, @Param("platform") Platform platform, @Param("status") Status status);

    @Query("SELECT a FROM Application a JOIN a.tags t JOIN a.platforms p WHERE t = :tag AND p = :platform AND a.status = :status")
    List<Application> findByTagsContainingAndPlatformAndStatus(@Param("tag") Tag tag, @Param("platform") Platform platform, @Param("status") Status status);

    List<Application> findAllByDeveloper(Developer developer);
    List<Application> findAllByStatus(Status status);
}


