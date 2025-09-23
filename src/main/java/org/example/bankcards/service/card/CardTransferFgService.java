package org.example.bankcards.service.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.notification.NotificationDto;
import org.example.bankcards.exception.C2CTransferException;
import org.example.bankcards.service.notification.CustomerNotificationService;
import org.example.bankcards.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
public class CardTransferFgService {

    private final CardTransferService cardTransferService;
    private final NotificationService notificationService;

    public CardTransferFgService(
            CardTransferService cardTransferService,
            @Qualifier(CustomerNotificationService.SERVICE_NAME) NotificationService notificationService
    ) {
        this.cardTransferService = cardTransferService;
        this.notificationService = notificationService;
    }

    @Transactional(rollbackFor = C2CTransferException.class)
    public void transferAmount(String fromCard,
                               String toCard,
                               BigDecimal amount,
                               long customerId) throws C2CTransferException {
        cardTransferService.transferAmount(fromCard, toCard, amount, customerId);
        notificationService.sendNotification(generateSuccessfulNotification(fromCard, customerId, amount));
    }

    private NotificationDto generateSuccessfulNotification(String fromCard,
                                                           long customerId,
                                                           BigDecimal amount) {
        return new NotificationDto(
                customerId,
                "Transfer from card with eid " + fromCard + " on " + amount.toString() + " successful!",
                Instant.now()
        );
    }
}
