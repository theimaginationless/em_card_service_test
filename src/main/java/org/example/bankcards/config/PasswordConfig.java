package org.example.bankcards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.password.encrypt.argon2")
public class PasswordConfig {
    private int saltLength;
    private int hashLength;
    private int parallelism;
    private int memory;
    private int iterations;

    @Bean
    public Argon2PasswordEncoder argon2PasswordEncoder() {
        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }
}
