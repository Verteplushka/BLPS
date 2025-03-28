package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CategoryDto;
import com.example.BLPS.Service.ApplicationService;
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
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/exactSearch")
    public ResponseEntity<?> exactSearch(@RequestParam String name) {
        Object result = applicationService.exactSearch(name);
        return ResponseEntity.ok(result);
    }
}
