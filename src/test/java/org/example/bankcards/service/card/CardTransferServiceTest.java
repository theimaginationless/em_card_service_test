package org.example.bankcards.service.card;

import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.exception.C2CTransferException;
import org.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardTransferServiceTest {

    private CardRepository cardRepository;
    private CardCacheService cardCacheService;
    private CardTransferHistoryService cardTransferHistoryService;
    private CardTransferService cardTransferService;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        cardCacheService = mock(CardCacheService.class);
        cardTransferHistoryService = mock(CardTransferHistoryService.class);
        cardTransferService = new CardTransferService(cardRepository, cardCacheService, cardTransferHistoryService);
    }

    @Test
    void testTransferAmount_Success() throws Exception {
        long customerId = 1L;
        String fromCard = "1111-1111-1111-1111";
        String toCard = "2222-2222-2222-2222";
        BigDecimal amount = new BigDecimal("100.00");

        Card from = mock(Card.class);
        Card to = mock(Card.class);

        when(from.getExternalCardId()).thenReturn(fromCard);
        when(to.getExternalCardId()).thenReturn(toCard);
        when(from.getBalance()).thenReturn(new BigDecimal("200.00"));
        when(to.getBalance()).thenReturn(new BigDecimal("50.00"));
        when(from.getCardStatus()).thenReturn(CardStatus.ACTIVE);
        when(to.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        when(cardRepository.getCustomerCardForUpdate(fromCard, customerId)).thenReturn(Optional.of(from));
        when(cardRepository.getCustomerCardForUpdate(toCard, customerId)).thenReturn(Optional.of(to));

        cardTransferService.transferAmount(fromCard, toCard, amount, customerId);

        verify(from).setBalance(new BigDecimal("100.00"));
        verify(to).setBalance(new BigDecimal("150.00"));
        verify(cardTransferHistoryService).insertTransferToHistory(eq(customerId), eq(from), eq(to), any(Instant.class), eq(amount));
        verify(cardCacheService).evictCardByExternalCardId(fromCard);
        verify(cardCacheService).evictCardByExternalCardId(toCard);
    }

    @Test
    void testTransferAmount_SameCard_Throws() {
        String card = "1111-1111-1111-1111";

        C2CTransferException exception = assertThrows(C2CTransferException.class,
                () -> cardTransferService.transferAmount(card, card, BigDecimal.TEN, 1L));

        assertEquals("Attempt to transfer to the same card", exception.getMessage());
    }

    @Test
    void testTransferAmount_NotEnoughBalance_Throws() {
        long customerId = 1L;
        String fromCard = "1111";
        String toCard = "2222";
        BigDecimal amount = new BigDecimal("200.00");

        Card from = mock(Card.class);
        Card to = mock(Card.class);

        when(from.getExternalCardId()).thenReturn(fromCard);
        when(to.getExternalCardId()).thenReturn(toCard);
        when(from.getBalance()).thenReturn(new BigDecimal("100.00"));
        when(from.getCardStatus()).thenReturn(CardStatus.ACTIVE);
        when(to.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        when(cardRepository.getCustomerCardForUpdate(fromCard, customerId)).thenReturn(Optional.of(from));
        when(cardRepository.getCustomerCardForUpdate(toCard, customerId)).thenReturn(Optional.of(to));

        C2CTransferException exception = assertThrows(C2CTransferException.class,
                () -> cardTransferService.transferAmount(fromCard, toCard, amount, customerId));

        assertEquals("Don't enough balance available", exception.getMessage());
    }

    @Test
    void testTransferAmount_CardNotFound_Throws() {
        long customerId = 1L;
        String fromCard = "1111";
        String toCard = "2222";

        when(cardRepository.getCustomerCardForUpdate(fromCard, customerId)).thenReturn(Optional.empty());

        C2CTransferException exception = assertThrows(C2CTransferException.class,
                () -> cardTransferService.transferAmount(fromCard, toCard, BigDecimal.TEN, customerId));

        assertTrue(exception.getMessage().contains("Card with externalCardId"));
    }

    @Test
    void testTransferAmount_CardBlocked_Throws() {
        long customerId = 1L;
        String fromCard = "1111";
        String toCard = "2222";

        Card from = mock(Card.class);
        Card to = mock(Card.class);

        when(from.getExternalCardId()).thenReturn(fromCard);
        when(to.getExternalCardId()).thenReturn(toCard);
        when(from.getBalance()).thenReturn(new BigDecimal("100.00"));
        when(to.getBalance()).thenReturn(new BigDecimal("100.00"));
        when(from.getCardStatus()).thenReturn(CardStatus.BLOCKED);
        when(to.getCardStatus()).thenReturn(CardStatus.ACTIVE);

        when(cardRepository.getCustomerCardForUpdate(fromCard, customerId)).thenReturn(Optional.of(from));
        when(cardRepository.getCustomerCardForUpdate(toCard, customerId)).thenReturn(Optional.of(to));

        C2CTransferException exception = assertThrows(C2CTransferException.class,
                () -> cardTransferService.transferAmount(fromCard, toCard, BigDecimal.TEN, customerId));

        assertEquals("Card is blocked", exception.getMessage());
    }

}
