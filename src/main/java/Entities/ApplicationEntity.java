package Entities;

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
    private String name;  // Название приложения

    @Column(nullable = false)
    private String category; // Категория (игры, утилиты и т. д.)

    @Column(nullable = false)
    private String platform; // Android / iOS

    @Column(nullable = false)
    private String description; // Описание

    @Column(nullable = false)
    private String developer; // Разработчик

    @Column(nullable = false)
    private Double rating; // Рейтинг (0.0 - 5.0)

    @Column(nullable = false)
    private Integer downloads; // Количество скачиваний

    @Column(nullable = false)
    private String imageUrl; // Ссылка на изображение приложения
}
