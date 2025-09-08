package org.example.bankcards.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.bankcards.config.JwtConfig;
import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.exception.DecryptSecretException;
import org.example.bankcards.exception.UnknownRoleException;
import org.example.bankcards.exception.UserServiceException;
import org.example.bankcards.security.principal.GenericPrincipal;
import org.example.bankcards.service.GenericUserService;
import org.example.bankcards.util.SecurityUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_USER_ROLE = "role";
    private final GenericUserService genericUserService;
    private final JwtConfig jwtConfig;

    public String getMasterKey() {
        return jwtConfig.getMasterKey();
    }

    public String generateJwtToken(long customerId,
                                   String login,
                                   String customerJwtSecret,
                                   RoleType roleType
    ) throws DecryptSecretException {
        try {
            String customerSecret = SecurityUtil.decryptSecret(customerJwtSecret,
                    jwtConfig.getMasterKey());
            SecretKey customerSecretKey = SecurityUtil.getHmacShaSecretKey(customerSecret);
            Instant now = Instant.now();
            return Jwts.builder()
                    .setSubject(login)
                    .claim(CLAIM_USER_ID, customerId)
                    .claim(CLAIM_USER_ROLE, roleType.value)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plus(jwtConfig.getExpiresInSec(), ChronoUnit.SECONDS)))
                    .signWith(customerSecretKey)
                    .compact();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public UsernamePasswordAuthenticationToken getUsernamePasswordAuthToken(AbstractUserDto user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().value));
        GenericPrincipal principal = getPrincipalFromUser(user);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities);
    }

    private GenericPrincipal getPrincipalFromUser(AbstractUserDto user) {
        return GenericPrincipal.builder()
                .id(user.getId())
                .login(user.getLogin())
                .roleType(user.getRole())
                .build();
    }

    public boolean verifyJwtToken(String jwtToken, AbstractUserDto user) {
        return verifyJwtSign(jwtToken,
                user.getPersonalEncryptedJwtSecret(),
                jwtConfig.getMasterKey());
    }

    public AbstractUserDto getUserByToken(String token)
            throws UserServiceException, UnknownRoleException, AuthCustomerException {
        Claims claims = getUnverifiedClaims(token);
        String login = claims.getSubject();
        long uid = claims.get(CLAIM_USER_ID, Long.class);
        RoleType roleType = EnumUtils.getEnum(RoleType.class, claims.get(CLAIM_USER_ROLE, String.class));
        AbstractUserDto user = genericUserService.getUserBy(uid, roleType);
        validateUserLoginAndRole(user, login, roleType);
        return user;
    }

    private void validateUserLoginAndRole(AbstractUserDto userDto, String login, RoleType role)
            throws AuthCustomerException {
        if (!StringUtils.equals(userDto.getLogin(), login)
                || userDto.getRole() != role) {
            throw new AuthCustomerException("Invalid fisrt-step login and role truth");
        }
    }

    public Claims getUnverifiedClaims(String jwtToken) {
        String[] parts = jwtToken.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Invalid JWT token");

        String unsignedJwt = parts[0] + "." + parts[1] + ".";
        return Jwts.parserBuilder()
                .build()
                .parseClaimsJwt(unsignedJwt)
                .getBody();
    }

    private boolean verifyJwtSign(String jwtToken, String customerJwtSecret, String masterKey) {
        try {
            String customerSecret = SecurityUtil.decryptSecret(customerJwtSecret, masterKey);
            SecretKey customerSecretKey = SecurityUtil.getHmacShaSecretKey(customerSecret);
            Jwts.parserBuilder()
                    .setSigningKey(customerSecretKey)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }
}
