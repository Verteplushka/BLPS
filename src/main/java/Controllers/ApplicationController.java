package Controllers;

import entities.ApplicationEntity;
import Repositories.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/apps")
@CrossOrigin(origins = "*") // Разрешает запросы с фронтенда
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    public ApplicationController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    //  Получить весь каталог приложений
    @GetMapping
    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }

    //  Получить топ-10 приложений
    @GetMapping("/top")
    public List<ApplicationEntity> getTopApplications() {
        return applicationRepository.findTop10ByOrderByRatingDesc();
    }

    //  Получить рекомендованные приложения
    @GetMapping("/recommended")
    public List<ApplicationEntity> getRecommendedApplications() {
        return applicationRepository.findByIsRecommendedTrue();
    }

    //  Получить приложения по категории
    @GetMapping("/category/{category}")
    public List<ApplicationEntity> getApplicationsByCategory(@PathVariable String category) {
        return applicationRepository.findByCategoryIgnoreCase(category);
    }
    //  Поиск приложений по названию
    @GetMapping("/search")
    public List<ApplicationEntity> searchApplications(@RequestParam String name) {
        return applicationRepository.findByNameContainingIgnoreCase(name);
    }

    //  Фильтрация по категории и платформе
    @GetMapping("/filter")
    public List<ApplicationEntity> filterApplications(@RequestParam String category, @RequestParam String platform) {
        return applicationRepository.findByCategoryAndPlatform(category, platform);
    }

    //  Добавить новое приложение (для админов)
    @PostMapping("/add")
    public ApplicationEntity addApplication(@RequestBody ApplicationEntity application) {
        return applicationRepository.save(application);
    }

    //  Получить информацию об одном приложении
    @GetMapping("/{id}")
    public ApplicationEntity getApplicationById(@PathVariable Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Приложение не найдено"));
    }

    //  Обновить данные приложения
    @PutMapping("/{id}")
    public ApplicationEntity updateApplication(@PathVariable Long id, @RequestBody ApplicationEntity applicationDetails) {
        return applicationRepository.findById(id)
                .map(app -> {
                    app.setName(applicationDetails.getName());
                    app.setCategory(applicationDetails.getCategory());
                    app.setPlatform(applicationDetails.getPlatform());
                    app.setDescription(applicationDetails.getDescription());
                    app.setDeveloper(applicationDetails.getDeveloper());
                    app.setRating(applicationDetails.getRating());
                    app.setDownloads(applicationDetails.getDownloads());
                    app.setImageUrl(applicationDetails.getImageUrl());
                    app.setIsRecommended(applicationDetails.getIsRecommended());
                    return applicationRepository.save(app);
                })
                .orElseThrow(() -> new RuntimeException("Приложение не найдено"));
    }

    //  Удалить приложение
    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable Long id) {
        applicationRepository.deleteById(id);
    }
}