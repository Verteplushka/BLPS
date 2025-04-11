package com.example.BLPS.Controllers;

import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CreateApplicationDto;
import com.example.BLPS.Exceptions.CreateAppFailedException;
import com.example.BLPS.Service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationService adminService;

    @PostMapping("/createApplication")
    public ResponseEntity<ApplicationDtoDetailed> createApplication(@RequestBody CreateApplicationDto request) {
        try{
            return ResponseEntity.ok(adminService.createApplication(request));
        } catch (CreateAppFailedException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
