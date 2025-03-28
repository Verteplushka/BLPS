package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Dto.CategoryDto;
import com.example.BLPS.Service.ApplicationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    // Получить весь каталог приложений
    @GetMapping
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    // Получить топ-10 приложений по рейтингу
    @GetMapping("/top")
    public ResponseEntity<List<ApplicationDto>> getTop10Applications() {
        return ResponseEntity.ok(applicationService.getTop10Applications());
    }

    // Получить рекомендованные приложения
    @GetMapping("/recommended")
    public ResponseEntity<List<ApplicationDto>> getRecommendedApplications() {
        return ResponseEntity.ok(applicationService.getRecommendedApplications());
    }

    // Поиск приложения по названию
    @GetMapping("/search")
    public ResponseEntity<List<ApplicationDto>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(applicationService.searchByName(name));
    }

    @PostMapping("/changePlatform")
    public ResponseEntity<Void> changePlatform(@RequestParam String platform) {
        try {
            applicationService.changePlatform(platform);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(applicationService.getApplicationsByCategory());
    }

}
