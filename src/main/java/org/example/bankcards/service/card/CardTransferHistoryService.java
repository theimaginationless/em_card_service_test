package org.example.bankcards.service.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.repository.CardTransferHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardTransferHistoryService {

    private final CardTransferHistoryRepository transferHistoryRepository;

    public void insertTransferToHistory(long initiatorId,
                                        Card cardFrom,
                                        Card cardTo,
                                        Instant transferInstant,
                                        BigDecimal amount) {
        transferHistoryRepository.insertTransfer(
                initiatorId,
                cardFrom.getId(),
                cardTo.getId(),
                transferInstant,
                amount);
    }
}
