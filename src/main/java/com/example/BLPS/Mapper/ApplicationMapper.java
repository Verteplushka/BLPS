package com.example.BLPS.Mapper;

import com.example.BLPS.Dto.ApplicationDto;
import com.example.BLPS.Dto.ApplicationDtoDetailed;
import com.example.BLPS.Dto.CreateApplicationDto;
import com.example.BLPS.Entities.Application;
import com.example.BLPS.Entities.Developer;
import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Entities.Tag;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationMapper {
    public static ApplicationDtoDetailed toDtoDetailed(Application application) {
        if (application == null) {
            return null;
        }

        List<String> platforms = application.getPlatforms().stream()
                .map(Platform::getName)
                .collect(Collectors.toList());

        List<String> tags = application.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        return new ApplicationDtoDetailed(
                application.getName(),
                application.getDeveloper(),
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
                tags,
                application.getStatus()
        );
    }

    public static List<ApplicationDtoDetailed> toDtoDetailedList(List<Application> applications) {
        return applications.stream()
                .map(ApplicationMapper::toDtoDetailed)
                .collect(Collectors.toList());
    }

    public static ApplicationDto toDto(Application application) {
        if (application == null) {
            return null;
        }

        String tag = application.getTags().get(0).getName();

        return new ApplicationDto(
                application.getId(),
                application.getDeveloper().getName(),
                application.getName(),
                application.getRating(),
                application.getImageUrl(),
                tag
        );
    }

    public static List<ApplicationDto> toDtoList(List<Application> applications) {
        return applications.stream()
                .map(ApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    public static Application toEntity(CreateApplicationDto dto, Developer developer, List<Platform> platforms, List<Tag> tags) {
        Application app = new Application();
        app.setDeveloper(developer);
        app.setName(dto.getName());
        app.setDescription(dto.getDescription());
        app.setPrice(dto.getPrice());
        app.setImageUrl(dto.getImageUrl());
        app.setHasPaidContent(dto.getHasPaidContent());
        app.setHasAds(dto.getHasAds());
        app.setIsEditorsChoice(dto.getIsEditorsChoice());
        app.setAgeLimit(dto.getAgeLimit());
        app.setIsRecommended(dto.getIsRecommended());
        app.setPlatforms(platforms);
        app.setTags(tags);
        app.setDownloads(0);
        app.setRating(0f);
        return app;
    }
}
