package com.example.BLPS.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO для запроса
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnbanRequestDto {
    private Integer developerId;
    private String message; // опционально
}

