package org.example.bankcards.config;

import lombok.RequiredArgsConstructor;
import org.example.bankcards.service.notification.CustomerNotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Bean(name = CustomerNotificationService.SERVICE_NAME)
    public CustomerNotificationService customerNotificationService() {
        return new CustomerNotificationService(reactiveStringRedisTemplate);
    }
}
