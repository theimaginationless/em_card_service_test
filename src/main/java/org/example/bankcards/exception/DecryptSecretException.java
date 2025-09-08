package org.example.bankcards.exception;

public class DecryptSecretException extends Exception {
    public DecryptSecretException(Throwable throwable) {
        super(throwable);
    }
    public DecryptSecretException(String message) {
        super(message);
    }
}
