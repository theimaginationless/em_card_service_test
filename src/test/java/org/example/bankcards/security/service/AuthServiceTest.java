package org.example.bankcards.security.service;

import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.dto.CustomerDto;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.dto.StaffDto;
import org.example.bankcards.exception.AuthCustomerException;
import org.example.bankcards.service.PasswordService;
import org.example.bankcards.service.customer.CustomerService;
import org.example.bankcards.service.staff.StaffService;
import org.example.bankcards.service.UserService;
import org.example.bankcards.security.service.AuthService;
import org.example.bankcards.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private PasswordService passwordService;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomerService customerService;
    @Mock
    private StaffService staffService;
    private Map<String, UserService<? extends AbstractUserDto>> userServiceMap;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userServiceMap = Map.of(
                CustomerService.SERVICE_NAME, customerService,
                StaffService.SERVICE_NAME, staffService
        );
        authService = new AuthService(userServiceMap, jwtService, passwordService);
    }

    @Test
    void testCustomerAuthenticate_Success() throws Exception {
        String login = "john";
        String password = "pass";
        long userId = 1L;
        String personalSecret = "secret";

        CustomerDto customerDto = mock(CustomerDto.class);
        when(customerDto.getId()).thenReturn(userId);
        when(customerDto.getLogin()).thenReturn(login);
        when(customerDto.getHashedPassword()).thenReturn("hashed");
        when(customerDto.getPersonalEncryptedJwtSecret()).thenReturn(personalSecret);
        when(customerDto.getRole()).thenReturn(RoleType.USER);

        when(customerService.getUserByLogin(login)).thenReturn(customerDto);
        when(passwordService.verifyPassword(password, "hashed")).thenReturn(true);
        when(jwtService.generateJwtToken(userId, login, personalSecret, RoleType.USER)).thenReturn("jwt-token");

        String token = authService.customerAuthenticate(login, password);
        assertEquals("jwt-token", token);

        // verify last login was refreshed
        verify(customerService).refreshLastLoginById(userId);
    }

    @Test
    void testCustomerAuthenticate_InvalidPassword() throws Exception {
        String login = "john";
        String password = "wrong";

        CustomerDto customerDto = mock(CustomerDto.class);
        when(customerDto.getHashedPassword()).thenReturn("hashed");
        when(customerService.getUserByLogin(login)).thenReturn(customerDto);
        when(passwordService.verifyPassword(password, "hashed")).thenReturn(false);

        assertThrows(AuthCustomerException.class,
                () -> authService.customerAuthenticate(login, password));
    }

    @Test
    void testCustomerAuthenticate_UserNotFound() throws Exception {
        String login = "unknown";
        String password = "pass";

        when(customerService.getUserByLogin(login)).thenThrow(new RuntimeException("Not found"));

        assertThrows(AuthCustomerException.class,
                () -> authService.customerAuthenticate(login, password));
    }

    @Test
    void testStaffAuthenticate_Success() throws Exception {
        String login = "admin";
        String password = "pass";
        long staffId = 2L;
        String personalSecret = "secret";

        StaffDto staffDto = mock(StaffDto.class);
        when(staffDto.getId()).thenReturn(staffId);
        when(staffDto.getLogin()).thenReturn(login);
        when(staffDto.getHashedPassword()).thenReturn("hashed");
        when(staffDto.getPersonalEncryptedJwtSecret()).thenReturn(personalSecret);
        when(staffDto.getRole()).thenReturn(RoleType.ADMIN);

        when(staffService.getUserByLogin(login)).thenReturn(staffDto);
        when(passwordService.verifyPassword(password, "hashed")).thenReturn(true);
        when(jwtService.generateJwtToken(staffId, login, personalSecret, RoleType.ADMIN)).thenReturn("jwt-admin-token");

        String token = authService.staffAuthenticate(login, password);
        assertEquals("jwt-admin-token", token);

        verify(staffService).refreshLastLoginById(staffId);
    }
}
