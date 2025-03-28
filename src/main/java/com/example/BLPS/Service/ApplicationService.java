package com.example.BLPS.Service;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Entities.Application;
import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Mapper.ApplicationMapper;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Repositories.PlatformRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PlatformRepository platformRepository;
    private Platform platform;
    public ApplicationService(ApplicationRepository applicationRepository, PlatformRepository platformRepository){
        this.applicationRepository = applicationRepository;
        this.platformRepository = platformRepository;
    }

    @PostConstruct
    private void init() {
        this.platform = platformRepository.findPlatformByName("phone");
    }

    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findByPlatformsContaining(this.platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getTop10Applications() {
        List<Application> applications = applicationRepository.findTop10ByPlatformOrderByRatingDesc(this.platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getRecommendedApplications() {
        List<Application> applications = applicationRepository.findByPlatformAndIsRecommendedTrue(this.platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> searchByName(String name) {
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, this.platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public void changePlatform(String platformName){
        this.platform =  platformRepository.findPlatformByName(platformName);
    }
}
