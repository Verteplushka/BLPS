package com.example.BLPS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@Configuration
public class ClockConfig {
//    @Primary
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Primary
    @Bean
    public Clock fixedClock() {
        return Clock.fixed(LocalDate.of(2025, 4, 22).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}

