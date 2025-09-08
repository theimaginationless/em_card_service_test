package org.example.bankcards.dto;

import org.example.bankcards.entity.Card.CardStatus;

import java.math.BigDecimal;

public record CardDto(
        long internalId,
        String externalId,
        String cardNumber,
        String owner,
        int expiryMonth,
        int expiryYear,
        CardStatus status,
        BigDecimal balance) { }
