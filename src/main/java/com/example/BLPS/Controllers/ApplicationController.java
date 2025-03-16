package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // Поиск приложения по платформе
    @GetMapping("/platform")
    public ResponseEntity<List<ApplicationDto>> searchByPlatform(@RequestParam String platform) {
        return ResponseEntity.ok(applicationService.searchByPlatform(platform));
    }
}
