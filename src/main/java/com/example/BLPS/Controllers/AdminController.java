package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Entities.Status;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationService applicationService;

    @GetMapping("/pending")
    public ResponseEntity<List<ApplicationDtoDetailed>> getPendingApplications() {
        List<ApplicationDtoDetailed> pendingApps = applicationService.getApplicationsByStatus(Status.PENDING);
        return ResponseEntity.ok(pendingApps);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approveApplication(@PathVariable Long id) {
        applicationService.updateModerationStatus(id, Status.APPROVED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<Void> rejectApplication(@PathVariable Long id) {
        applicationService.updateModerationStatus(id, Status.REJECTED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ban/{developerId}")
    public ResponseEntity<Void> banDeveloper(@PathVariable Integer developerId) {
        applicationService.rejectAllApplicationsByDeveloper(developerId);
        return ResponseEntity.ok().build();
    }

}
