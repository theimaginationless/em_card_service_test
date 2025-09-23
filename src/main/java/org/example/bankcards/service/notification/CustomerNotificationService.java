package org.example.bankcards.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.notification.NotificationDto;
import org.example.bankcards.mapper.NotificationMapper;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerNotificationService implements NotificationService {

    public static final String CUSTOMER_URGENT_NOTIFICATION_STREAM = "customer_urgent_notification_stream";
    public static final String CUSTOMER_URGENT_NOTIFICATION_GROUP = "customer_urgent_notification_group";
    public static final String CUSTOMER_URGENT_NOTIFICATION_CONSUMER = "customer_urgent_notification_consumer";
    public static final String SERVICE_NAME = "customer_notification_service";

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Override
    public void sendNotification(NotificationDto notification) {
        var data = Map.of(
                "user_id", notification.userId(),
                "text", notification.text(),
                "pushed_at", notification.pushedAt().toEpochMilli()
        );
        reactiveStringRedisTemplate.opsForStream()
                .add(CUSTOMER_URGENT_NOTIFICATION_STREAM, data)
                .doOnError(e -> log.error(e.getMessage(), e))
                .retryWhen(Retry.backoff(10, Duration.ofMillis(500)))
                .subscribe();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<NotificationDto> subscribeNotifications(long userId) {
        return reactiveStringRedisTemplate.opsForStream().read(
                        Consumer.from(CUSTOMER_URGENT_NOTIFICATION_GROUP, CUSTOMER_URGENT_NOTIFICATION_CONSUMER),
                        StreamOffset.create(CUSTOMER_URGENT_NOTIFICATION_STREAM, ReadOffset.lastConsumed()))
                .filter(entry ->
                        String.valueOf(entry.getValue().get("user_id")).equals(String.valueOf(userId)))
                .flatMap(record -> {
                    var dto = NotificationMapper.MapRecordToDto(record);
                    return reactiveStringRedisTemplate.opsForStream()
                            .acknowledge(CUSTOMER_URGENT_NOTIFICATION_STREAM, CUSTOMER_URGENT_NOTIFICATION_GROUP, record.getId())
                            .thenReturn(dto);
                })
                .repeatWhen(flux -> flux.delaySubscription(Duration.ofMillis(100)));
    }
}
