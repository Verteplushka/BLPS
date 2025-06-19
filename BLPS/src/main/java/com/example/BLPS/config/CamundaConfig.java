package com.example.BLPS.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Bean
    public ExternalTaskClient externalTaskClient() {
        return ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest") // URL вашего Camunda
                .asyncResponseTimeout(10000) // 10 сек
                .build();
    }
}
