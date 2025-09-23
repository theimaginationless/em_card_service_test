package org.example.bankcards.mapper;

import org.apache.commons.collections4.MapUtils;
import org.example.bankcards.dto.notification.NotificationDto;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.time.Instant;
import java.util.Map;

public class NotificationMapper {
    public static NotificationDto MapRecordToDto(MapRecord<String, Object, Object> entry) {
        Map<Object, Object> value = entry.getValue();
        long pushedAtMillis = Long.parseLong(MapUtils.getString(value, "pushed_at"));
        return new NotificationDto(
                MapUtils.getLong(value, "user_id", -1L),
                MapUtils.getString(value, "text", null),
                Instant.ofEpochMilli(pushedAtMillis)
        );
    }

}
