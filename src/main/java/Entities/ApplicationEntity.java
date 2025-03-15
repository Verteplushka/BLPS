package entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String developer;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private Integer downloads;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isRecommended;  // Новое поле: является ли приложение рекомендованным
}
