package com.example.BLPS.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CamundaCategoryDto {
    String processInstanceId;
    List<CategoryDto> categories;
}
