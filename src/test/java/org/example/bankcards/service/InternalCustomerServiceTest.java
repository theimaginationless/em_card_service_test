package org.example.bankcards.service;

import org.example.bankcards.dto.RoleType;
import org.example.bankcards.entity.User.Customer.Customer;
import org.example.bankcards.entity.User.Role;
import org.example.bankcards.entity.User.Staff.Staff;
import org.example.bankcards.exception.RegisterCustomerException;
import org.example.bankcards.exception.RoleNotFoundException;
import org.example.bankcards.repository.CustomerRepository;
import org.example.bankcards.repository.StaffRepository;
import org.example.bankcards.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternalCustomerServiceTest {

    private PasswordService passwordService;
    private CustomerRepository customerRepository;
    private StaffRepository staffRepository;
    private RoleService roleService;
    private JwtService jwtService;
    private InternalCustomerService internalCustomerService;

    @BeforeEach
    void setUp() {
        passwordService = mock(PasswordService.class);
        customerRepository = mock(CustomerRepository.class);
        staffRepository = mock(StaffRepository.class);
        roleService = mock(RoleService.class);
        jwtService = mock(JwtService.class);
        internalCustomerService = new InternalCustomerService(
                passwordService, customerRepository, staffRepository, roleService, jwtService
        );
    }

    @Test
    void testRegisterCustomer_Success() throws Exception {
        String login = "john_doe";
        String password = "secret";
        when(customerRepository.findCustomerByLogin(login)).thenReturn(java.util.Optional.empty());
        when(passwordService.hashPassword(password)).thenReturn("hashed");
        Role role = new Role();
        when(roleService.getRoleByType(RoleType.USER)).thenReturn(role);
        when(jwtService.getMasterKey()).thenReturn("master-key");

        try (MockedStatic<org.example.bankcards.util.SecurityUtil> mockedSecurity = mockStatic(org.example.bankcards.util.SecurityUtil.class)) {
            mockedSecurity.when(org.example.bankcards.util.SecurityUtil::generateJwtSecret).thenReturn("personal-secret");
            mockedSecurity.when(() -> org.example.bankcards.util.SecurityUtil.encryptSecret("personal-secret", "master-key"))
                    .thenReturn("encrypted-secret");

            internalCustomerService.registerCustomer(login, password);

            verify(customerRepository).save(any(Customer.class));
        }
    }

    @Test
    void testRegisterCustomer_AlreadyExists() {
        String login = "john_doe";
        when(customerRepository.findCustomerByLogin(login)).thenReturn(java.util.Optional.of(new Customer()));

        RegisterCustomerException exception = assertThrows(RegisterCustomerException.class,
                () -> internalCustomerService.registerCustomer(login, "pwd"));
        assertEquals("Customer is already registered", exception.getMessage());
    }

    @Test
    void testRegisterStaff_Success() throws Exception {
        String login = "staff";
        String password = "pwd";
        when(staffRepository.findStaffByLogin(login)).thenReturn(java.util.Optional.empty());
        when(passwordService.hashPassword(password)).thenReturn("hashed");
        Role role = new Role();
        when(roleService.getRoleByType(RoleType.ADMIN)).thenReturn(role);
        when(jwtService.getMasterKey()).thenReturn("master-key");

        try (MockedStatic<org.example.bankcards.util.SecurityUtil> mockedSecurity = mockStatic(org.example.bankcards.util.SecurityUtil.class)) {
            mockedSecurity.when(org.example.bankcards.util.SecurityUtil::generateJwtSecret).thenReturn("personal-secret");
            mockedSecurity.when(() -> org.example.bankcards.util.SecurityUtil.encryptSecret("personal-secret", "master-key"))
                    .thenReturn("encrypted-secret");

            internalCustomerService.registerStaff(login, password);

            verify(staffRepository).save(any(Staff.class));
        }
    }

    @Test
    void testRegisterStaff_AlreadyExists() {
        String login = "staff";
        when(staffRepository.findStaffByLogin(login)).thenReturn(java.util.Optional.of(new Staff()));

        RegisterCustomerException exception = assertThrows(RegisterCustomerException.class,
                () -> internalCustomerService.registerStaff(login, "pwd"));
        assertEquals("Staff is already registered", exception.getMessage());
    }

    @Test
    void testVerifyCredentials_Success() {
        String login = "john";
        String password = "pwd";
        Customer customer = new Customer();
        customer.setLogin(login);
        customer.setHashedPassword("hashed-pwd");

        when(customerRepository.findCustomerByLogin(login)).thenReturn(java.util.Optional.of(customer));
        when(passwordService.verifyPassword(password, "hashed-pwd")).thenReturn(true);

        assertTrue(internalCustomerService.verifyCredentials(login, password));
    }

    @Test
    void testVerifyCredentials_WrongPassword() {
        String login = "john";
        String password = "pwd";
        Customer customer = new Customer();
        customer.setLogin(login);
        customer.setHashedPassword("hashed-pwd");

        when(customerRepository.findCustomerByLogin(login)).thenReturn(java.util.Optional.of(customer));
        when(passwordService.verifyPassword(password, "hashed-pwd")).thenReturn(false);

        assertFalse(internalCustomerService.verifyCredentials(login, password));
    }

    @Test
    void testVerifyCredentials_CustomerNotFound() {
        when(customerRepository.findCustomerByLogin("unknown")).thenReturn(java.util.Optional.empty());

        assertFalse(internalCustomerService.verifyCredentials("unknown", "pwd"));
    }

    @Test
    void testGetUserRole_ThrowsRoleNotFound() throws Exception {
        String login = "user";
        String password = "pass";
        when(customerRepository.findCustomerByLogin(login)).thenReturn(java.util.Optional.empty());
        when(passwordService.hashPassword(password)).thenReturn("hashed");
        when(roleService.getRoleByType(any())).thenThrow(new RoleNotFoundException());
        try (MockedStatic<org.example.bankcards.util.SecurityUtil> securityUtilMock = mockStatic(org.example.bankcards.util.SecurityUtil.class)) {
            securityUtilMock.when(org.example.bankcards.util.SecurityUtil::generateJwtSecret)
                    .thenReturn("personal-secret");

            securityUtilMock.when(() -> org.example.bankcards.util.SecurityUtil.encryptSecret("personal-secret", "master-key"))
                    .thenReturn("encrypted-secret");
            assertThrows(RegisterCustomerException.class,
                    () -> internalCustomerService.registerCustomer(login, password));
        }
    }

    @Test
    void testGetAdminRole_ThrowsRoleNotFound() throws RoleNotFoundException {
        String login = "admin";
        String password = "pass";
        when(staffRepository.findStaffByLogin(login)).thenReturn(java.util.Optional.empty());
        when(passwordService.hashPassword(password)).thenReturn("hashed");
        when(roleService.getRoleByType(any())).thenThrow(new RoleNotFoundException());
        try (MockedStatic<org.example.bankcards.util.SecurityUtil> securityUtilMock = mockStatic(org.example.bankcards.util.SecurityUtil.class)) {
            securityUtilMock.when(org.example.bankcards.util.SecurityUtil::generateJwtSecret)
                    .thenReturn("personal-secret");
            securityUtilMock.when(() -> org.example.bankcards.util.SecurityUtil.encryptSecret("personal-secret", "master-key"))
                    .thenReturn("encrypted-secret");
            assertThrows(RegisterCustomerException.class,
                    () -> internalCustomerService.registerCustomer(login, password));
        }
    }
}
