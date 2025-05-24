package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Entities.Status;
import com.example.BLPS.Exceptions.ApplicationNotPendingModerationException;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        List<ApplicationDtoDetailed> pendingApps = applicationService.getApplicationsByStatus(Status.ADMIN_MODERATION);
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

    @ExceptionHandler(ApplicationNotPendingModerationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotPending(ApplicationNotPendingModerationException e) {
        return e.getMessage();
    }
}
