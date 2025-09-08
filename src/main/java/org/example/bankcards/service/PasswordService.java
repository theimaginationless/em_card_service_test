package org.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final Argon2PasswordEncoder encoder;

    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    public boolean verifyPassword(String password, String hashedPassword) {
        return encoder.matches(password, hashedPassword);
    }
}
