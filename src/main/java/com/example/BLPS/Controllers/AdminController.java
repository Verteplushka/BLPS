package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CreateApplicationDto;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationService adminService;

    @PostMapping("/createApplication")
    public ResponseEntity<ApplicationDtoDetailed> createApplication(@RequestBody CreateApplicationDto request) {
        return ResponseEntity.ok(adminService.createApplication(request));
    }
}
