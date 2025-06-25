package com.example.BLPS.Service;

import com.example.BLPS.Dto.*;
import com.example.BLPS.Entities.*;
import com.example.BLPS.Exceptions.*;
import com.example.BLPS.Mapper.ApplicationMapper;
import com.example.BLPS.Repositories.ApplicationRepository;
import com.example.BLPS.Utils.StringUtils;
import com.example.BLPS.Utils.UserXmlReader;
import com.example.BLPS.config.JiraAdapterClient;
import com.example.BLPS.config.MqttMessageSender;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
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
    private final JiraAdapterClient jiraAdapterClient;
    private Platform platform;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, PlatformService platformService, TagService tagService, DeveloperService developerService, PlatformTransactionManager transactionManager, MqttMessageSender mqttMessageSender, JiraAdapterClient jiraAdapterClient, JdbcTemplate jdbcTemplate) {
        this.applicationRepository = applicationRepository;
        this.platformService = platformService;
        this.tagService = tagService;
        this.developerService = developerService;
        this.transactionManager = transactionManager;
        this.mqttMessageSender = mqttMessageSender;
        this.jiraAdapterClient = jiraAdapterClient;
        this.jdbcTemplate = jdbcTemplate;
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

    @Transactional
    public List<ApplicationDto> getTop10Applications() {
        List<Application> applications = applicationRepository.findTop10ByPlatformAndStatusOrderByRatingDesc(platform, Status.APPROVED);
        return ApplicationMapper.toDtoList(applications);
    }

    @Transactional
    public List<ApplicationDto> getRecommendedApplications() {
        List<Application> applications = applicationRepository.findByPlatformAndIsRecommendedTrueAndStatus(platform, Status.APPROVED);
        return ApplicationMapper.toDtoList(applications);
    }

    @Transactional
    public List<CategoryDto> getApplicationsByTags() {
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

    @Transactional
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
        Object exact = searchExactMatch(name);
        if (exact != null) return exact;

        Object fuzzy = searchFuzzyMatch(name);
        if (fuzzy != null) return fuzzy;

        Object partial = searchPartialMatch(name);
        if (partial != null) return partial;

        return nothingFound(name);
    }

    @Transactional
    public ExactMatchDto searchExactMatch(String name) {
        ApplicationDtoDetailed exactMatch = findByExactName(name);
        if (exactMatch != null) {
            return new ExactMatchDto(exactMatch, "Приложение найдено по полному совпадению.");
        }
        return null;
    }

    @Transactional
    public ExactMatchDto searchFuzzyMatch(String name) {
        if (name.length() > 2) {
            Application fuzzyMatch = findFuzzyMatch(name);
            if (fuzzyMatch != null) {
                return new ExactMatchDto(
                        ApplicationMapper.toDtoDetailed(fuzzyMatch),
                        "Найдено похожее приложение. Хотите вместо этого выполнить поиск по исходной строке?"
                );
            }
        }
        return null;
    }

    @Transactional
    public List<ApplicationDto> searchPartialMatch(String name) {
        List<Application> applications = applicationRepository
                .findByNameContainingIgnoreCaseAndPlatformAndStatus(name, platform, Status.APPROVED);

        if (!applications.isEmpty()) {
            return ApplicationMapper.toDtoList(applications);
        }

        return null;
    }

    @Transactional
    public NotFoundDto nothingFound(String name) {
        return new NotFoundDto(
                "Приложение с названием \"" + name + "\" не найдено. Вот приложения, которые могут вам понравиться",
                getRecommendedApplications()
        );
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
        List<ApplicationDto> applicationDtos = ApplicationMapper.toDtoList(applicationRepository.findByNameContainingIgnoreCaseAndPlatformAndStatus(name, platform, Status.APPROVED));
        if (applicationDtos.isEmpty()) {
            throw new AppsNotFoundException("No applications found for exact search with name \" " + name + "\"");
        }
        return applicationDtos;
    }

    @Transactional
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

    @Transactional
    public List<DeveloperApplicationDto> getAllApplicationsForDeveloperByName(String username) {
        try {
            UserXmlReader reader = new UserXmlReader(xmlFilePath);
            int userId = reader.getUserIdByUsername(username);

            Developer developer = developerService.findByUserId(userId);
            List<Application> applications = applicationRepository.findAllByDeveloper(developer);

            return applications.stream()
                    .map(ApplicationMapper::toDeveloperApplicationDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch developer's applications: " + e.getMessage());
        }
    }



    @Transactional
    public DeveloperApplicationDto createApplication(String username, CreateApplicationDto request) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            UserXmlReader reader = new UserXmlReader(xmlFilePath);
            int userId = reader.getUserIdByUsername(username);

            Developer developer = developerService.findByUserId(userId);

            List<Platform> platforms = platformService.findAllById(request.getPlatformIds());
            List<Tag> tags = tagService.findAllById(request.getTagIds());

            Application application = ApplicationMapper.toEntity(request, developer, platforms, tags);
            application.setStatus(Status.AUTO_MODERATION);
            Application saved = applicationRepository.save(application);

            jiraAdapterClient.createModerationTask(saved.getId(), saved.getName(), saved.getDeveloper().getName());

            transactionManager.commit(status);
            return ApplicationMapper.toDeveloperApplicationDto(saved);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            ex.printStackTrace();
            throw new CreateAppFailedException("Transaction failed: " + ex.getMessage());
        }
    }

    @Transactional
    public ApplicationDtoDetailed updateApplication(String username, Long appId, CreateApplicationDto updatedData) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
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
            app.setPrice(updatedData.getPrice());
            app.setImageUrl(updatedData.getImageUrl());
            app.setHasPaidContent(updatedData.getHasPaidContent());
            app.setHasAds(updatedData.getHasAds());
            app.setIsEditorsChoice(updatedData.getIsEditorsChoice());
            app.setAgeLimit(updatedData.getAgeLimit());
            app.setIsRecommended(updatedData.getIsRecommended());
            app.setPlatforms(platforms);
            app.setTags(tags);
            app.setStatus(Status.AUTO_MODERATION);

            Application saved = applicationRepository.save(app);

            jiraAdapterClient.createModerationTask(saved.getId(), saved.getName(), saved.getDeveloper().getName());

            mqttMessageSender.sendMessage(moderationQueueName, saved.getId().toString());

            transactionManager.commit(status);
            return ApplicationMapper.toDtoDetailed(saved);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException("Failed to update application: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteApplicationByIdAndDevName(Long appId, String username) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
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

    @Transactional
    public List<DeveloperApplicationDto> getApplicationsByStatus(Status status) {
        List<Application> apps = applicationRepository.findAllByStatus(status);
        return apps.stream()
                .map(ApplicationMapper::toDeveloperApplicationDto)
                .toList();
    }

    public void updateModerationStatus(Long applicationId, Status newStatus) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(def);

        try {
            Application app = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("App does not exist"));

            if (app.getStatus() != Status.ADMIN_MODERATION) {
                throw new ApplicationNotPendingModerationException(applicationId);
            }

            app.setStatus(newStatus);
//            applicationRepository.save(app);
            System.out.println(newStatus.name() + " " + applicationId);
            jdbcTemplate.update(
                    "UPDATE applications SET moderation_status = ? WHERE id = ?",
                    newStatus.name(),
                    applicationId
            );

            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException("Status update error: " + e.getMessage(), e);
        }
    }


    public void rejectAllApplicationsByDeveloper(Integer developerId, String reason) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // Обновляем статус всех заявок разработчика
            int updatedCount = jdbcTemplate.update(

                    "UPDATE applications SET moderation_status = ? WHERE developer_id = ?",
                    Status.REJECTED.toString(),
                    developerId
            );
            // Пытаемся обновить запись о бане, если она уже существует
            int updatedBan = jdbcTemplate.update(
                    "UPDATE developer_bans SET reason = ?, ban_date = ?, active = true WHERE developer_id = ?",
                    reason,
                    LocalDateTime.now(),
                    developerId
            );

            // Если обновление не затронуло ни одной строки — вставляем новую
            if (updatedBan == 0) {
                jdbcTemplate.update(
                        "INSERT INTO developer_bans (developer_id, reason, ban_date, active) VALUES (?, ?, ?, ?)",
                        developerId,
                        reason,
                        LocalDateTime.now(),
                        true
                );
            }


            // Логируем результат
            System.out.printf("Rejected %d applications for developer %d%n", updatedCount, developerId);

            // Если нужно также обновить статус разработчика (бан)


            transactionManager.commit(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw new RuntimeException("Couldn't ban developer: " + ex.getMessage(), ex);
        }
    }
    public void unbanDeveloper(Integer developerId, String reason) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // Проверка: существует ли разработчик
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM developers WHERE id = ?",
                    Integer.class,
                    developerId
            );
            if (count == null || count == 0) {
                throw new IllegalArgumentException("Developer with ID " + developerId + " does not exist.");
            }

            // Проверка: есть ли активный бан
            Integer activeBanCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM developer_bans WHERE developer_id = ? AND active = true",
                    Integer.class,
                    developerId
            );
            if (activeBanCount == null || activeBanCount == 0) {
                throw new IllegalStateException("Developer with ID " + developerId + " is not currently banned.");
            }

            // 1. Одобряем все заявки разработчика
            int updatedCount = jdbcTemplate.update(
                    "UPDATE applications SET moderation_status = ? WHERE developer_id = ?",
                    Status.APPROVED.toString(),
                    developerId
            );

            // 2. Обновляем запись о бане: active = false и новое сообщение reason
            jdbcTemplate.update(
                    "UPDATE developer_bans SET active = false, reason = ? WHERE developer_id = ?",
                    reason,
                    developerId
            );

            // 3. Лог
            System.out.printf("Unbanned developer %d, approved %d applications. Reason: %s%n", developerId, updatedCount, reason);

            transactionManager.commit(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw new RuntimeException("Couldn't unban developer: " + ex.getMessage(), ex);
        }
    }



}
