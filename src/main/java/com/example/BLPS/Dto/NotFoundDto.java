package com.example.BLPS.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotFoundDto {
    private String message;
    private List<ApplicationDto> recommendedApps;
}
