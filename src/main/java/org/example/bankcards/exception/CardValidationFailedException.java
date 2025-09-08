package org.example.bankcards.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CardValidationFailedException extends Exception {
    public CardValidationFailedException(String message) {
        super(message);
    }
}
