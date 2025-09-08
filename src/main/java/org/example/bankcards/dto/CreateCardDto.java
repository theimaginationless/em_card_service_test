package org.example.bankcards.dto;

public record CreateCardDto(
        String cardNumber,
        String customerLogin,
        int expiryMonth,
        int expiryYear
) { }
