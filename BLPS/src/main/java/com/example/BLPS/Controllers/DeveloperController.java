package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CreateApplicationDto;
import com.example.BLPS.Dto.UpdateApplicationDto;
import com.example.BLPS.Exceptions.CreateAppFailedException;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
public class DeveloperController {

    private final ApplicationService applicationService;

    @PostMapping("/create")
    public ResponseEntity<ApplicationDtoDetailed> createApplication(@RequestBody CreateApplicationDto request) {
        try {
            ApplicationDtoDetailed app = applicationService.createApplication(request);
            return ResponseEntity.ok(app);
        } catch (CreateAppFailedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/delete/{appId}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long appId) {
        try {
            applicationService.deleteApplicationById(appId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/update/{appId}")
    public ResponseEntity<ApplicationDtoDetailed> updateApplication(
            @PathVariable Long appId,
            @RequestBody UpdateApplicationDto request
    ) {
        try {
            ApplicationDtoDetailed updated = applicationService.updateApplication(appId, request);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationDtoDetailed>> getMyApplications() {
        try {
            List<ApplicationDtoDetailed> apps = applicationService.getAllApplicationsForCurrentDeveloper();
            return ResponseEntity.ok(apps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
