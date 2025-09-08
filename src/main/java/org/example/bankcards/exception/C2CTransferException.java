package org.example.bankcards.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class C2CTransferException extends Exception {
    public C2CTransferException(String message) {
        super(message);
    }
    public C2CTransferException(Throwable throwable) {
        super(throwable);
    }
}
