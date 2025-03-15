package Controllers;

import Entities.ApplicationEntity;
import Repositories.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Разрешает запросы с фронтенда
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    // 1. Получить список всех приложений
    @GetMapping
    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }

    // 2. Поиск приложений по названию
    @GetMapping("/search")
    public List<ApplicationEntity> searchApplications(@RequestParam String name) {
        return applicationRepository.findByNameContainingIgnoreCase(name);
    }

    // 3. Фильтрация по категории и платформе
    @GetMapping("/filter")
    public List<ApplicationEntity> filterApplications(@RequestParam String category, @RequestParam String platform) {
        return applicationRepository.findByCategoryAndPlatform(category, platform);
    }

    // 4. Получить детали конкретного приложения по ID
    @GetMapping("/{id}")
    public Optional<ApplicationEntity> getApplicationById(@PathVariable Long id) {
        return applicationRepository.findById(id);
    }

    // 5. Добавить новое приложение (для админов)
    @PostMapping
    public ApplicationEntity addApplication(@RequestBody ApplicationEntity application) {
        return applicationRepository.save(application);
    }

    // 6. Обновить данные приложения
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
                    return applicationRepository.save(app);
                })
                .orElseThrow(() -> new RuntimeException("Приложение не найдено"));
    }

    // 7. Удалить приложение
    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable Long id) {
        applicationRepository.deleteById(id);
    }
}