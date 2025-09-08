package org.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.security.principal.GenericPrincipal;
import org.springframework.security.core.context.SecurityContext;

import java.util.Optional;

@Slf4j
public class SecurityContextUtil {
    public static <T> T getPrincipal(SecurityContext securityContext, Class<T> clazz) {
        Object principal = securityContext.getAuthentication().getPrincipal();
        if (clazz.isInstance(principal)) {
            return clazz.cast(principal);
        }

        throw new ClassCastException();
    }

    public static Optional<GenericPrincipal> getGenericPrincipal(SecurityContext securityContext) {
        try {
            return Optional.ofNullable(getPrincipal(securityContext, GenericPrincipal.class));
        } catch (Exception e) {
            log.error("Invalid principal class", e);
        }

        return Optional.empty();
    }
}
