package org.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractUserDto {
    private long id;
    private String login;
    private String fullName;
    private String personalEncryptedJwtSecret;
    private String hashedPassword;
    private RoleType role;
}
