package com.example.BLPS.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExactMatchDto {
    private ApplicationDtoDetailed exactMatch;
    private String message;
}
