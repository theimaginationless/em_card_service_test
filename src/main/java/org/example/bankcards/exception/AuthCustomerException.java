package org.example.bankcards.exception;

public class AuthCustomerException extends Exception {
    public AuthCustomerException(Throwable throwable) {
        super(throwable);
    }
    public AuthCustomerException() {
        super(); }

    public AuthCustomerException(String message) {
        super(message);
    }
}
