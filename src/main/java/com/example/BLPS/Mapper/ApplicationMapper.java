package com.example.BLPS.Mapper;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Entities.Application;
import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Entities.Tag;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationMapper {
    public static ApplicationDto toDto(Application application) {
        if (application == null) {
            return null;
        }

        List<String> platforms = application.getPlatforms().stream()
                .map(Platform::getName)  // Получаем имя платформы
                .collect(Collectors.toList());

        List<String> tags = application.getTags().stream()
                .map(Tag::getName)  // Получаем имя тега
                .collect(Collectors.toList());

        return new ApplicationDto(
                application.getDeveloper(),
                application.getName(),
                application.getDescription(),
                application.getRating(),
                application.getDownloads(),
                application.getImageUrl(),
                application.getHasPaidContent(),
                application.getHasAds(),
                application.getIsEditorsChoice(),
                application.getAgeLimit(),
                application.getIsRecommended(),
                platforms,
                tags
        );
    }
    public static List<ApplicationDto> toDtoList(List<Application> applications) {
        return applications.stream()
                .map(ApplicationMapper::toDto)
                .collect(Collectors.toList());
    }
}
