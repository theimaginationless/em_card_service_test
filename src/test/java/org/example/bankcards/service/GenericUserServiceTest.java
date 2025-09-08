package org.example.bankcards.service;

import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.dto.CustomerDto;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.dto.StaffDto;
import org.example.bankcards.exception.CustomerNotFoundException;
import org.example.bankcards.exception.UnknownRoleException;
import org.example.bankcards.service.customer.CustomerService;
import org.example.bankcards.service.staff.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GenericUserServiceTest {

    private CustomerService customerService;
    private StaffService staffService;
    private GenericUserService genericUserService;

    @BeforeEach
    void setup() {
        customerService = mock(CustomerService.class);
        staffService = mock(StaffService.class);

        genericUserService = new GenericUserService(Map.of(
                CustomerService.SERVICE_NAME, customerService,
                StaffService.SERVICE_NAME, staffService
        ));
    }

    @Test
    void testGetUserBy_UserRole_Success() throws Exception {
        CustomerDto mockUser = mock(CustomerDto.class);
        when(customerService.getUserById(1L)).thenReturn(mockUser);

        AbstractUserDto result = genericUserService.getUserBy(1L, RoleType.USER);

        assertNotNull(result);
        verify(customerService, times(1)).getUserById(1L);
    }

    @Test
    void testGetUserBy_AdminRole_Success() throws Exception {
        StaffDto mockUser = mock(StaffDto.class);
        when(staffService.getUserById(2L)).thenReturn(mockUser);

        AbstractUserDto result = genericUserService.getUserBy(2L, RoleType.ADMIN);

        assertNotNull(result);
        verify(staffService, times(1)).getUserById(2L);
    }

    @Test
    void testGetUserBy_UnknownRole_ThrowsException() {
        assertThrows(UnknownRoleException.class,
                () -> genericUserService.getUserBy(1L, null));
    }

    @Test
    void testGetUserBy_UserNotFound_ThrowsCustomerNotFoundException() throws Exception {
        when(customerService.getUserById(1L)).thenThrow(new CustomerNotFoundException());

        assertThrows(CustomerNotFoundException.class,
                () -> genericUserService.getUserBy(1L, RoleType.USER));
    }
}
