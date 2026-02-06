package com.example.BLPS.Repositories;

import com.example.BLPS.Entities.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Integer> {
    Platform findPlatformByName(String name);
}
