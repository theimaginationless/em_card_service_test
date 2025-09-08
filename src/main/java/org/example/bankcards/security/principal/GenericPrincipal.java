package org.example.bankcards.security.principal;

import lombok.Builder;
import lombok.Data;
import org.example.bankcards.dto.RoleType;

@Data
@Builder
public class GenericPrincipal {
    private long id;
    private String login;
    private RoleType roleType;
}
