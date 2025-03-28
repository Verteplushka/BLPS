package com.example.BLPS.Dto;

import com.example.BLPS.Entities.Developer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private int id;
    private String name;
    private String developer;
    private Float rating;
    private String imageUrl;
    private String tag;
}
