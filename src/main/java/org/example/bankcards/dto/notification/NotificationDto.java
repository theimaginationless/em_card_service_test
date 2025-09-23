package org.example.bankcards.dto.notification;

import java.time.Instant;

public record NotificationDto(
        long userId,
        String text,
        Instant pushedAt
) {
}
