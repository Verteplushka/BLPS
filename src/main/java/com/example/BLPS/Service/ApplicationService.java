package com.example.BLPS.Service;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Entities.Application;
import com.example.BLPS.Entities.Developer;
import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Entities.Tag;
import com.example.BLPS.Exceptions.AppNotFoundException;
import com.example.BLPS.Exceptions.AppsNotFoundException;
import com.example.BLPS.Exceptions.CreateAppFailedException;
import com.example.BLPS.Exceptions.PlatformNotFoundException;
import com.example.BLPS.Mapper.ApplicationMapper;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Utils.StringUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PlatformService platformService;
    private final TagService tagService;
    private final DeveloperService developerService;
    private final PlatformTransactionManager transactionManager;

    private Platform platform;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, PlatformService platformService, TagService tagService, DeveloperService developerService, PlatformTransactionManager transactionManager) {
        this.applicationRepository = applicationRepository;
        this.platformService = platformService;
        this.tagService = tagService;
        this.developerService = developerService;
        this.transactionManager = transactionManager;
    }

    @PostConstruct
    private void init() {
        System.out.println(">>> TM is: " + transactionManager.getClass().getName());
        this.platform = platformService.findPlatformByName("phone");
    }

    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findByPlatformsContaining(platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public void changePlatform(String platformName) {
        Platform foundPlatform = platformService.findPlatformByName(platformName);
        if (foundPlatform == null) {
            throw new PlatformNotFoundException("Platform with name '" + platformName + "' not found.");
        }
        this.platform = foundPlatform;
    }

    private List<ApplicationDto> getTop10Applications() {
        List<Application> applications = applicationRepository.findTop10ByPlatformOrderByRatingDesc(platform);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getRecommendedApplications() {
        List<Application> applications = applicationRepository.findByPlatformAndIsRecommendedTrue(platform);
        return ApplicationMapper.toDtoList(applications);
    }

    private List<CategoryDto> getApplicationsByTags() {
        List<Tag> tags = tagService.findAll();
        List<CategoryDto> categories = new ArrayList<>();

        for (Tag tag : tags) {
            List<Application> applications = applicationRepository.findByTagsContainingAndPlatform(tag, platform);
            if (!applications.isEmpty()) {
                CategoryDto categoryDto = new CategoryDto(tag.getName(), ApplicationMapper.toDtoList(applications));
                categories.add(categoryDto);
            }
        }

        return categories;
    }

    public List<CategoryDto> getApplicationsByCategories() {
        List<CategoryDto> categories = new ArrayList<>();
        //todo на enum переписать
        categories.add(new CategoryDto("popular", getTop10Applications()));
        categories.add(new CategoryDto("recommended", getRecommendedApplications()));
        categories.addAll(getApplicationsByTags());

        return categories;
    }

    public ApplicationDtoDetailed findByExactName(String name) {
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, platform);
        for (Application application : applications) {
            if (application.getName().equalsIgnoreCase(name)) {
                return ApplicationMapper.toDtoDetailed(application);
            }
        }
        return null;
    }


    public Object searchApplications(String name) {
        // Проверка на полное совпадение
        ApplicationDtoDetailed exactMatch = findByExactName(name);
        if (exactMatch != null) {
            return new ExactMatchDto(exactMatch, "Приложение найдено по полному совпадению.");
        }

        // Поиск похожего приложения
        if (name.length() > 2) {
            Application fuzzyMatch = findFuzzyMatch(name);
            if (fuzzyMatch != null) {
                return new ExactMatchDto(ApplicationMapper.toDtoDetailed(fuzzyMatch), "Найдено похожее приложение. Хотите вместо этого выполнить поиск по исходной строке?");
            }
        }

        // Поиск по неполному совпадению
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, platform);

        if (!applications.isEmpty()) {
            return ApplicationMapper.toDtoList(applications);
        }

        // Если ничего не найдено
        return new NotFoundDto("Приложение с названием \"" + name + "\" не найдено. Вот приложения, которые могут вам понравиться", getRecommendedApplications());
    }

    private Application findFuzzyMatch(String query) {
        List<Application> allApplications = applicationRepository.findAll();
        Application bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Application app : allApplications) {
            int distance = StringUtils.levenshteinDistance(query.toLowerCase(), app.getName().toLowerCase());

            if (distance <= 3 && distance < bestDistance) {
                bestDistance = distance;
                bestMatch = app;
            }
        }

        return bestMatch;
    }

    public List<ApplicationDto> exactSearch(String name) {
        List<ApplicationDto> applicationDtos =  ApplicationMapper.toDtoList(applicationRepository.findByNameContainingIgnoreCaseAndPlatform(name, platform));
        if (applicationDtos.isEmpty()) {
            throw new AppsNotFoundException("No applications found for exact search with name \" " + name + "\"");
        }
        return applicationDtos;
    }

    public ApplicationDtoDetailed getApp(Long id) {
        Application foundApplication = applicationRepository.findById(id).orElse(null);
        if (foundApplication == null) {
            throw new AppNotFoundException("Application with id = " + id + " is not found");
        }
        return ApplicationMapper.toDtoDetailed(foundApplication);
    }

    public ApplicationDtoDetailed createApplication(CreateApplicationDto request) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Developer developer = developerService.findById(request.getDeveloperId());
            List<Platform> platforms = platformService.findAllById(request.getPlatformIds());
            List<Tag> tags = tagService.findAllById(request.getTagIds());

            Application application = ApplicationMapper.toEntity(request, developer, platforms, tags);
            Application saved = applicationRepository.save(application);

            transactionManager.commit(status);
            return ApplicationMapper.toDtoDetailed(saved);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw new CreateAppFailedException("Transaction failed: " + ex.getMessage());
        }
    }

    // создание разработчика
    // разработчик как отдельная роль
    // у developer будет user_id и все поля разработчика останутся в developer
    // блокировка разработчика (и всех его приложений, чтобы они не отображались в селектах)
    // таблица для модерации приложений (при обновлении приложения добавляется в таблицу для модерации) когда админ добавляет / обновляет приложение
}
