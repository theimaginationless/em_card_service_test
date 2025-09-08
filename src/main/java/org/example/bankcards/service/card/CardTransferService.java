package org.example.bankcards.service.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.exception.C2CTransferException;
import org.example.bankcards.exception.CardNotFoundException;
import org.example.bankcards.exception.CardValidationFailedException;
import org.example.bankcards.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardTransferService {

    private final CardRepository cardRepository;
    private final CardCacheService cardCacheService;
    private final CardTransferHistoryService cardTransferHistoryService;

    @Transactional(rollbackFor = C2CTransferException.class)
    public void transferAmount(String fromCard,
                               String toCard,
                               BigDecimal amount,
                               long customerId) throws C2CTransferException {
        if (StringUtils.equals(fromCard, toCard)) {
            log.error("Attempt to transfer to the same card");
            throw new C2CTransferException("Attempt to transfer to the same card");
        }

        try {
            List<String> orderedCardNumbers = Stream.of(fromCard, toCard).sorted().toList();
            Card firstCard = cardRepository.getCustomerCardForUpdate(orderedCardNumbers.getFirst(), customerId)
                    .orElseThrow(() -> generateCardNotFoundException(orderedCardNumbers.getFirst()));
            Card secondCard = cardRepository.getCustomerCardForUpdate(orderedCardNumbers.getLast(), customerId)
                    .orElseThrow(() -> generateCardNotFoundException(orderedCardNumbers.getLast()));
            validateCardForTransfer(firstCard);
            validateCardForTransfer(secondCard);
            List<Card> transferFromToCardOrderList;
            if (fromCard.equals(firstCard.getExternalCardId())) {
//                validateCardOnCustomer(firstCard, customerId);
                if (firstCard.getBalance().compareTo(amount) < 0) {
                    throw new C2CTransferException("Don't enough balance available");
                }
                firstCard.setBalance(firstCard.getBalance().subtract(amount));
                secondCard.setBalance(secondCard.getBalance().add(amount));
                transferFromToCardOrderList = List.of(firstCard, secondCard);
            } else {
//                validateCardOnCustomer(secondCard, customerId);
                secondCard.setBalance(secondCard.getBalance().subtract(amount));
                firstCard.setBalance(firstCard.getBalance().add(amount));
                transferFromToCardOrderList = List.of(secondCard, firstCard);
            }
            cardTransferHistoryService.insertTransferToHistory(
                    customerId,
                    transferFromToCardOrderList.getFirst(),
                    transferFromToCardOrderList.getLast(),
                    Instant.now(),
                    amount);
            orderedCardNumbers.forEach(cardCacheService::evictCardByExternalCardId);
        } catch (CardValidationFailedException e) {
            log.error("C2C Transfer failed: {}", e.getMessage(), e);
            throw new C2CTransferException(e.getMessage());
        } catch (C2CTransferException e) {
            log.error("C2C Transfer failed: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("C2C Transfer failed: {}", e.getMessage(), e);
            throw new C2CTransferException(e.getMessage());
        }
    }

    private void validateCardOnCustomer(Card card, long customerId) throws CardValidationFailedException {
        if (card.getCustomer().getId() != customerId) {
            throw new CardValidationFailedException("Accept to withdraw from another card." +
                    "externalCardId: " + card.getExternalCardId() +
                    "customerId: " + customerId);
        }
    }

    private void validateCardForTransfer(Card card) throws CardValidationFailedException {
        switch (card.getCardStatus()) {
            case BLOCKED -> throw new CardValidationFailedException("Card is blocked");
            case EXPIRED -> throw new CardValidationFailedException("Card is expired");
            case ACTIVE -> { }
        }
    }

    private CardNotFoundException generateCardNotFoundException(String externalCardId) {
        return new CardNotFoundException("Card with externalCardId " + externalCardId + " not found.");
    }

}
