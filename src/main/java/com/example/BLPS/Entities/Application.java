package com.example.BLPS.Entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "developer_id", nullable = false)
    private Developer developer;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Float rating;
    private Integer downloads;

    @Column(name = "price")
    private Float price = 0.0f;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "has_paid_content", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean hasPaidContent = true;

    @Column(name = "has_ads", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean hasAds = true;

    @Column(name = "is_editors_choice", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isEditorsChoice = false;

    @Column(name = "age_limit", nullable = false)
    private Integer ageLimit;

    @Column(name = "is_recommended", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRecommended = false;

    @ManyToMany
    @JoinTable(
            name = "application_platform",
            joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "platform_id")
    )
    private List<Platform> platforms;

    @ManyToMany
    @JoinTable(
            name = "application_tag",
            joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
}

