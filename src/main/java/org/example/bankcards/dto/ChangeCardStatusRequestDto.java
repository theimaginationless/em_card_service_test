package org.example.bankcards.dto;

public record ChangeCardStatusRequestDto(
        String requestId,
        String externalId,
        String cardNumber,
        String owner,
        int expiryMonth,
        int expiryYear,
        String status,
        String newStatus) { }
