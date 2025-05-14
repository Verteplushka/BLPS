package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CategoryDto;
import com.example.BLPS.Dto.DeveloperDto;
import com.example.BLPS.Dto.NotFoundDto;
import com.example.BLPS.Exceptions.AppNotFoundException;
import com.example.BLPS.Exceptions.AppsNotFoundException;
import com.example.BLPS.Exceptions.PlatformNotFoundException;
import com.example.BLPS.Service.ApplicationService;
import com.example.BLPS.Service.DeveloperService;
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
    private final DeveloperService developerService;

    @PostMapping("/changePlatform")
    public ResponseEntity<Void> changePlatform(@RequestParam String platform) {
        try {
            applicationService.changePlatform(platform);
            return ResponseEntity.ok().build();
        } catch (PlatformNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(applicationService.getApplicationsByCategories());
    }

    // Поиск приложения по названию (различные варианты совпадений)
    @GetMapping("/searchByName")
    public ResponseEntity<?> searchApplications(@RequestParam String name) {
        Object result = applicationService.searchApplications(name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getApp")
    public ResponseEntity<ApplicationDtoDetailed> getApp(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(applicationService.getApp(id));
        } catch (AppNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/exactSearch")
    public ResponseEntity<?> exactSearch(@RequestParam String name) {
        try{
            return ResponseEntity.ok(applicationService.exactSearch(name));
        } catch (AppsNotFoundException e) {
            return ResponseEntity.ok(new NotFoundDto("Приложение с названием \"" + name + "\" не найдено. Вот приложения, которые могут вам понравиться", applicationService.getRecommendedApplications()));
        }
    }

    @GetMapping("/getDevs")
    public ResponseEntity<List<DeveloperDto>> getAllDevelopers() {
        return ResponseEntity.ok(developerService.getAllDevelopers());
    }
}
