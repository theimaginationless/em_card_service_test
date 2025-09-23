package org.example.bankcards.service.notification;

import org.example.bankcards.dto.notification.NotificationDto;
import reactor.core.publisher.Flux;

public interface NotificationService {
    void sendNotification(NotificationDto notification);
    Flux<NotificationDto> subscribeNotifications(long userId);
}
