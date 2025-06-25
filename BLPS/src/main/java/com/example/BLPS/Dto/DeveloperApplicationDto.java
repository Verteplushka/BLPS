package com.example.BLPS.Dto;

import com.example.BLPS.Entities.Developer;
import com.example.BLPS.Entities.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperApplicationDto {
    private Integer id;
    private String name;
    private Developer developer;
    private String description;
    private Float rating;
    private Integer downloads;
    private String imageUrl;
    private Boolean hasPaidContent = true;
    private Boolean hasAds = true;
    private Boolean isEditorsChoice = false;
    private Integer ageLimit;
    private Boolean isRecommended = false;
    private List<String> platforms;
    private List<String> tags;
    private Status status;
}
