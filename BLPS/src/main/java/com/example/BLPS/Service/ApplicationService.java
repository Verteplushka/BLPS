package com.example.BLPS.Service;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Entities.*;
import com.example.BLPS.Exceptions.AppNotFoundException;
import com.example.BLPS.Exceptions.AppsNotFoundException;
import com.example.BLPS.Exceptions.CreateAppFailedException;
import com.example.BLPS.Exceptions.PlatformNotFoundException;
import com.example.BLPS.Mapper.ApplicationMapper;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Utils.StringUtils;
import com.example.BLPS.Utils.UserXmlReader;
import com.example.BLPS.config.MqttMessageSender;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PlatformService platformService;
    private final TagService tagService;
    private final DeveloperService developerService;
    private final PlatformTransactionManager transactionManager;
    private final MqttMessageSender mqttMessageSender;
    private final String xmlFilePath = "src/main/resources/users.xml";
    private final String moderationQueueName = "moderation-queue";
    private final Random random = new Random(); //Для рандомного изменения рейтинга раз в день

    private Platform platform;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, PlatformService platformService, TagService tagService, DeveloperService developerService, PlatformTransactionManager transactionManager, MqttMessageSender mqttMessageSender) {
        this.applicationRepository = applicationRepository;
        this.platformService = platformService;
        this.tagService = tagService;
        this.developerService = developerService;
        this.transactionManager = transactionManager;
        this.mqttMessageSender = mqttMessageSender;
    }

    @PostConstruct
    private void init() {
        System.out.println(">>> TM is: " + transactionManager.getClass().getName());
        this.platform = platformService.findPlatformByName("phone");
    }

    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findByPlatformsContainingAndStatus(platform, Status.APPROVED);
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
        List<Application> applications = applicationRepository.findTop10ByPlatformAndStatusOrderByRatingDesc(platform, Status.APPROVED);
        return ApplicationMapper.toDtoList(applications);
    }

    public List<ApplicationDto> getRecommendedApplications() {
        List<Application> applications = applicationRepository.findByPlatformAndIsRecommendedTrueAndStatus(platform, Status.APPROVED);
        return ApplicationMapper.toDtoList(applications);
    }

    private List<CategoryDto> getApplicationsByTags() {
        List<Tag> tags = tagService.findAll();
        List<CategoryDto> categories = new ArrayList<>();

        for (Tag tag : tags) {
            List<Application> applications = applicationRepository.findByTagsContainingAndPlatformAndStatus(tag, platform, Status.APPROVED);
            if (!applications.isEmpty()) {
                CategoryDto categoryDto = new CategoryDto(tag.getName(), ApplicationMapper.toDtoList(applications));
                categories.add(categoryDto);
            }
        }

        return categories;
    }

    public List<CategoryDto> getApplicationsByCategories() {
        List<CategoryDto> categories = new ArrayList<>();
        categories.add(new CategoryDto("popular", getTop10Applications()));
        categories.add(new CategoryDto("recommended", getRecommendedApplications()));
        categories.addAll(getApplicationsByTags());

        return categories;
    }

    public ApplicationDtoDetailed findByExactName(String name) {
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatformAndStatus(name, platform, Status.APPROVED);
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
        List<Application> applications = applicationRepository.findByNameContainingIgnoreCaseAndPlatformAndStatus(name, platform, Status.APPROVED);

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
        List<ApplicationDto> applicationDtos =  ApplicationMapper.toDtoList(applicationRepository.findByNameContainingIgnoreCaseAndPlatformAndStatus(name, platform, Status.APPROVED));
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

    public List<ApplicationDtoDetailed> getAllApplicationsForCurrentDeveloper() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserXmlReader reader = new UserXmlReader(xmlFilePath);
            int userId = reader.getUserIdByUsername(username);

            Developer developer = developerService.findByUserId(userId);
            List<Application> applications = applicationRepository.findAllByDeveloper(developer);

            return applications.stream()
                    .map(ApplicationMapper::toDtoDetailed)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch developer's applications: " + e.getMessage());
        }
    }


    public ApplicationDtoDetailed createApplication(CreateApplicationDto request) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserXmlReader reader = new UserXmlReader(xmlFilePath);
            int userId = reader.getUserIdByUsername(username);

            Developer developer = developerService.findByUserId(userId);

            List<Platform> platforms = platformService.findAllById(request.getPlatformIds());
            List<Tag> tags = tagService.findAllById(request.getTagIds());

            Application application = ApplicationMapper.toEntity(request, developer, platforms, tags);
            application.setStatus(Status.AUTO_MODERATION);
            Application saved = applicationRepository.save(application);

            mqttMessageSender.sendMessage(moderationQueueName, saved.getId().toString());

            transactionManager.commit(status);
            return ApplicationMapper.toDtoDetailed(saved);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            ex.printStackTrace(); // ← минимум, чтобы увидеть, в чём ошибка
            throw new CreateAppFailedException("Transaction failed: " + ex.getMessage());
        }
    }

    public void deleteApplicationById(Long appId) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserXmlReader reader = new UserXmlReader(xmlFilePath);
            int userId = reader.getUserIdByUsername(username);
            String role = reader.getUserRoleByUsername(username);

            Application app = applicationRepository.findById(appId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (!role.equals("ADMIN") && app.getDeveloper().getUserId() != userId) {
                throw new RuntimeException("You don't have permission to delete this application");
            }

            applicationRepository.delete(app);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException("Failed to delete application: " + e.getMessage());
        }
    }

    public ApplicationDtoDetailed updateApplication(Long appId, UpdateApplicationDto updatedData) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserXmlReader reader = new UserXmlReader(xmlFilePath);
            int userId = reader.getUserIdByUsername(username);

            Application app = applicationRepository.findById(appId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (app.getDeveloper().getUserId() != userId) {
                throw new RuntimeException("Only the developer of this application can update it");
            }

            List<Platform> platforms = platformService.findAllById(updatedData.getPlatformIds());
            List<Tag> tags = tagService.findAllById(updatedData.getTagIds());

            app.setName(updatedData.getName());
            app.setDescription(updatedData.getDescription());
            app.setPlatforms(platforms);
            app.setTags(tags);
            app.setStatus(Status.AUTO_MODERATION);

            Application saved = applicationRepository.save(app);

            mqttMessageSender.sendMessage(moderationQueueName, saved.getId().toString());

            transactionManager.commit(status);
            return ApplicationMapper.toDtoDetailed(saved);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException("Failed to update application: " + e.getMessage());
        }
    }

    public List<ApplicationDtoDetailed> getApplicationsByStatus(Status status) {
        List<Application> apps = applicationRepository.findAllByStatus(status);
        return apps.stream()
                .map(ApplicationMapper::toDtoDetailed)
                .toList();
    }

    public void updateModerationStatus(Long applicationId, Status newStatus) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(def);

        try {
            Application app = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("App does not exist"));
            app.setStatus(newStatus);
            applicationRepository.save(app);

            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException("Status update error: " + e.getMessage(), e);
        }
    }


    public void rejectAllApplicationsByDeveloper(Integer developerId) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Developer developer = developerService.findById(developerId);
            List<Application> apps = applicationRepository.findAllByDeveloper(developer);

            for (Application app : apps) {
                app.setStatus(Status.REJECTED);
            }

            applicationRepository.saveAll(apps);
            transactionManager.commit(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw new RuntimeException("Couldn't ban developer: " + ex.getMessage(), ex);
        }
    }
    // Метод будет вызываться каждый день в 00:00, обновляя рандомно рейтинг
   // @Scheduled(cron = "0 0 0 * * *")
//    @Scheduled(fixedRate = 10000) // каждые 10 секунд (для отладки)
//    public void updateRatingsDaily() {
//        // Начинаем транзакцию
//        TransactionDefinition def = new DefaultTransactionDefinition();
//        TransactionStatus status = transactionManager.getTransaction(def);
//        try {
//            System.out.println("⏰ [Scheduled Task] Updating app ratings at " + java.time.LocalDateTime.now());
//            List<Application> apps = applicationRepository.findAll();
//
//            // Возможные изменения и веса (чем ближе к 0 — тем выше шанс)
//            float[] changes = {-0.5f, -0.4f, -0.3f, -0.2f, -0.1f, 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
//            double[] weights = {1, 2, 4, 6, 8, 10, 8, 6, 4, 2, 1}; // сумма: 52
//
//            double totalWeight = 0;
//            for (double w : weights) totalWeight += w;
//
//            for (Application app : apps) {
//                Float oldRating = app.getRating() != null ? app.getRating() : 0.0f;
//
//                // Выбираем случайное изменение с учётом веса
//                double rand = Math.random() * totalWeight;
//                double cumulative = 0;
//                Float change = 0f;
//
//                for (int i = 0; i < changes.length; i++) {
//                    cumulative += weights[i];
//                    if (rand <= cumulative) {
//                        change = changes[i];
//                        break;
//                    }
//                }
//
//                Float newRating = Math.max(0.0f, Math.min(5.0f, oldRating + change));
//
//                System.out.printf("🔄 App ID %d | %s | Old: %.2f → New: %.2f (Δ %.1f)%n",
//                        app.getId(),
//                        app.getName(),
//                        oldRating,
//                        newRating,
//                        change
//                );
//
//                app.setRating(newRating);
//                System.out.println("New updated rating: "+app.getRating());
//                applicationRepository.save(app);
//            }
//
//            //applicationRepository.saveAll(apps);
//
//            System.out.println("✅ Daily rating update completed");
//            // Коммитим транзакцию
//            transactionManager.commit(status);
//        }catch (Exception e) {
//            // Откатываем транзакцию в случае ошибки
//            transactionManager.rollback(status);
//            throw new RuntimeException("Failed to update ratings: " + e.getMessage());
//        }
//    }




    // + создание разработчика
    // + разработчик как отдельная роль
    // + у developer будет user_id и все поля разработчика останутся в developer
    // + блокировка разработчика (и всех его приложений, чтобы они не отображались в селектах)
    // + модерация приложений когда админ добавляет / обновляет приложение

    //
}
