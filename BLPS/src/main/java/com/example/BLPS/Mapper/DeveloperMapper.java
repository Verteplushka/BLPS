package com.example.BLPS.Mapper;

import com.example.BLPS.Dto.DeveloperDto;
import com.example.BLPS.Entities.Developer;

import java.util.List;
import java.util.stream.Collectors;

public class DeveloperMapper {

    public static DeveloperDto toDto(Developer developer) {
        DeveloperDto dto = new DeveloperDto();
        dto.setId(developer.getId());
        dto.setUserId(developer.getUserId());
        dto.setName(developer.getName());
        dto.setWebsite(developer.getWebsite());
        return dto;
    }

    public static List<DeveloperDto> toDtoList(List<Developer> developers) {
        return developers.stream()
                .map(DeveloperMapper::toDto)
                .collect(Collectors.toList());
    }
}
