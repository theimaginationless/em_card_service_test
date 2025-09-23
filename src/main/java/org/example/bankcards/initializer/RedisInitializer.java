package org.example.bankcards.initializer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.example.bankcards.service.notification.CustomerNotificationService.CUSTOMER_URGENT_NOTIFICATION_GROUP;
import static org.example.bankcards.service.notification.CustomerNotificationService.CUSTOMER_URGENT_NOTIFICATION_STREAM;

@Component
public class RedisInitializer implements ApplicationRunner {

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisInitializer(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String stream = CUSTOMER_URGENT_NOTIFICATION_STREAM;
        String group = CUSTOMER_URGENT_NOTIFICATION_GROUP;

        redisTemplate.opsForStream().groups(stream)
                .collectList()
                .flatMapMany(existingGroups -> {
                    boolean groupExists = existingGroups.stream()
                            .anyMatch(info -> info.groupName().equals(group));
                    if (!groupExists) {
                        return redisTemplate.opsForStream()
                                .createGroup(stream, ReadOffset.from("0"), group)
                                .flux();
                    }
                    return Flux.empty();
                })
                .subscribe();
    }
}
