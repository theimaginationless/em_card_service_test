package org.example.bankcards.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CardNotFoundException extends Exception {
    public CardNotFoundException(String message) {
        super(message);
    }
}
