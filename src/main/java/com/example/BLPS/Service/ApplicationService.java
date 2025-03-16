package com.example.BLPS.Service;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Entities.Application;
import com.example.BLPS.Mapper.ApplicationMapper;
import com.example.BLPS.Repositories.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findAll();
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getTop10Applications() {
        List<Application> applications = applicationRepository.findTop10ByOrderByRatingDesc();
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getRecommendedApplications() {
        List<Application> applications = applicationRepository.findByIsRecommendedTrue();
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> searchByName(String name) {
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCase(name);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> searchByPlatform(String platform) {
        List<Application> applications = applicationRepository.findByPlatform(platform);
        return ApplicationMapper.toDtoList(applications);
    }
}
