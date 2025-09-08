package org.example.bankcards.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.service.PasswordService;
import org.example.bankcards.service.customer.CustomerService;
import org.example.bankcards.service.UserService;
import org.example.bankcards.service.staff.StaffService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Map<String, UserService<? extends AbstractUserDto>> userServiceMap;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public String customerAuthenticate(String login,
                                       String password) throws AuthCustomerException {
        return authenticate(login, password, RoleType.USER);
    }

    public String staffAuthenticate(String login,
                                       String password) throws AuthCustomerException {
        return authenticate(login, password, RoleType.ADMIN);
    }

    public String authenticate(String login,
                               String password,
                               RoleType role)
            throws AuthCustomerException {
        try {
            var userService = getUserServiceByRole(role);
            AbstractUserDto user = userService.getUserByLogin(login);
            if (passwordService.verifyPassword(password, user.getHashedPassword())) {
                refreshLastLoginById(user.getId(), userService);
                return jwtService.generateJwtToken(user.getId(),
                        user.getLogin(),
                        user.getPersonalEncryptedJwtSecret(),
                        user.getRole());
            } else {
                throw new BadCredentialsException("Invalid password!");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AuthCustomerException(e);
        }
    }

    private UserService<? extends AbstractUserDto> getUserServiceByRole(RoleType role) {
        String serviceName = switch (role) {
            case USER -> CustomerService.SERVICE_NAME;
            case ADMIN -> StaffService.SERVICE_NAME;
            case null -> throw new IllegalArgumentException();
        };

        return userServiceMap.get(serviceName);
    }

    private void refreshLastLoginById(long id,
                                      UserService<? extends AbstractUserDto> userService) {
        userService.refreshLastLoginById(id);
    }
}
