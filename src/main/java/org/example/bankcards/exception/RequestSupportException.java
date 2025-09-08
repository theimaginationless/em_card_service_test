package org.example.bankcards.exception;

public class RequestSupportException extends Exception {
    public RequestSupportException(Throwable throwable) {
        super(throwable);
    }

    public RequestSupportException(String message) {
        super(message);
    }

    public RequestSupportException() {
        super(); }
}
