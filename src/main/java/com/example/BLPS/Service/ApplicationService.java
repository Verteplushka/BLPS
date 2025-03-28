package com.example.BLPS.Service;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Dto.CategoryDto;
import com.example.BLPS.Dto.SearchResultDto;
import com.example.BLPS.Entities.Application;
import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Entities.Tag;
import com.example.BLPS.Mapper.ApplicationMapper;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Repositories.PlatformRepository;
import com.example.BLPS.Repositories.TagRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PlatformRepository platformRepository;
    private final TagRepository tagRepository;
    private Platform platform;
    public ApplicationService(ApplicationRepository applicationRepository, PlatformRepository platformRepository, TagRepository tagRepository){
        this.applicationRepository = applicationRepository;
        this.platformRepository = platformRepository;
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    private void init() {
        this.platform = platformRepository.findPlatformByName("phone");
    }

    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findByPlatformsContaining(platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getTop10Applications() {
        List<Application> applications = applicationRepository.findTop10ByPlatformOrderByRatingDesc(platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getRecommendedApplications() {
        List<Application> applications = applicationRepository.findByPlatformAndIsRecommendedTrue(platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> searchByName(String name) {
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public void changePlatform(String platformName) {
        Platform foundPlatform = platformRepository.findPlatformByName(platformName);
        if (foundPlatform == null) {
            throw new RuntimeException("Platform with name '" + platformName + "' not found.");
        }
        this.platform = foundPlatform;
    }

    public List<CategoryDto> getApplicationsByCategory() {
        List<Tag> tags = tagRepository.findAll();
        List<CategoryDto> categories = new ArrayList<>();

        for (Tag tag : tags) {
            List<Application> applications = applicationRepository.findByTagsContainingAndPlatform(tag, platform);
            if(!applications.isEmpty()){
                CategoryDto categoryDto = new CategoryDto(tag.getName(), ApplicationMapper.toDtoList(applications));
                categories.add(categoryDto);
            }
        }

        return categories;
    }
    public ApplicationDto findByExactName(String name) {
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, platform);
        for (Application application : applications) {
            if (application.getName().equalsIgnoreCase(name)) {
                return ApplicationMapper.toDto(application);
            }
        }
        return null;
    }


    public SearchResultDto searchApplications(String name) {
        SearchResultDto resultDto = new SearchResultDto();

        // Проверка на полное совпадение
        ApplicationDto exactMatch = findByExactName(name);
        if (exactMatch != null) {
            resultDto.setExactMatch(exactMatch);
            resultDto.setMessage("Приложение найдено по полному совпадению.");
            return resultDto;
        }

        // Поиск по неполному совпадению
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, platform);
        if (!applications.isEmpty()) {
            resultDto.setSimilarMatches(ApplicationMapper.toDtoList(applications));
            resultDto.setMessage("Найдено приложение с похожим названием. Хотите выполнить поиск только по полному совпадению?");
        } else {
            resultDto.setMessage("Приложение с названием \"" + name + "\" не найдено.");
        }

        return resultDto;
    }



}
