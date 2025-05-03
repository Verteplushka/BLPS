package com.example.BLPS.Service;

import com.example.BLPS.Entities.Developer;
import com.example.BLPS.Repositories.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeveloperService {
    private final DeveloperRepository developerRepository;

    @Autowired
    public DeveloperService(DeveloperRepository developerRepository){
        this.developerRepository = developerRepository;
    }

    public Developer findById(Integer developerId) {
        return developerRepository.findById(developerId).orElse(null);
    }

    public Developer findByUserId(int userId) {
        return developerRepository.findByUserId(userId);
    }
}
