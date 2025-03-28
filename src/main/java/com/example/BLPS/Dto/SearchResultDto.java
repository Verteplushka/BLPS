package com.example.BLPS.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private ApplicationDto exactMatch;
    private List<ApplicationDto> similarMatches;
    private String message;
}
