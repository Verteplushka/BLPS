package com.example.BLPS.Dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateApplicationDto {
    private Integer developerId;
    private String name;
    private String description;
    private Float price;
    private String imageUrl;
    private Boolean hasPaidContent;
    private Boolean hasAds;
    private Boolean isEditorsChoice;
    private Integer ageLimit;
    private Boolean isRecommended;
    private List<Integer> platformIds;
    private List<Integer> tagIds;
}

