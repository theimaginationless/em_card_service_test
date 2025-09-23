package org.example.bankcards.controller.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.notification.NotificationDto;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.service.notification.CustomerNotificationService;
import org.example.bankcards.service.notification.NotificationService;
import org.example.bankcards.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/customer/notifications")
public class CustomerNotificationController {

    private final NotificationService notificationService;

    public CustomerNotificationController(
            @Qualifier(CustomerNotificationService.SERVICE_NAME) NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/urgent", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationDto> getUrgentStream() {
        try {
            var principal = SecurityContextUtil.getGenericPrincipal(SecurityContextHolder.getContext())
                    .orElseThrow(AuthCustomerException::new);
            return notificationService.subscribeNotifications(principal.getId());
        } catch (AuthCustomerException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Flux.empty();
    }
}
