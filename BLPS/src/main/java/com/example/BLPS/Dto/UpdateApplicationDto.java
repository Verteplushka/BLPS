package com.example.BLPS.Dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateApplicationDto {
    private String name;
    private String description;
    private String version;
    private List<Integer> platformIds;
    private List<Integer> tagIds;
}

