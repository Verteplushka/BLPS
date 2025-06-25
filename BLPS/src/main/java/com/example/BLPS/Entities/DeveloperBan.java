package com.example.BLPS.Entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "developer_bans")
public class DeveloperBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "developer_id", nullable = false)
    private Developer developer;


    @Column(name = "reason")
    private String reason;

    @Column(name = "ban_date", nullable = false)
    private LocalDateTime banDate;

    @Column(name = "active", nullable = false)
    private boolean active;

    // Конструкторы, геттеры и сеттеры
    public DeveloperBan() {
    }

    public DeveloperBan(Developer developer, Integer bannedByAdminId, String reason) {
        this.developer = developer;
        this.reason = reason;
        this.banDate = LocalDateTime.now();
        this.active = true;
    }
}
