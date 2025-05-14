package com.example.BLPS.Repositories;

import com.example.BLPS.Entities.Developer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperRepository extends JpaRepository<Developer, Integer> {
    Developer findByUserId(Integer userId);
}
