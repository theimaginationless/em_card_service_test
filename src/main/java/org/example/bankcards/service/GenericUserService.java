package org.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.exception.UnknownRoleException;
import org.example.bankcards.exception.UserServiceException;
import org.example.bankcards.service.customer.CustomerService;
import org.example.bankcards.service.staff.StaffService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenericUserService {

    private final Map<String, UserService<? extends AbstractUserDto>> userServiceMap;

    public AbstractUserDto getUserBy(long id, RoleType role)
            throws UnknownRoleException, UserServiceException {
        return getUserServiceByRole(role).getUserById(id);
    }

    private UserService<? extends AbstractUserDto> getUserServiceByRole(RoleType role)
            throws UnknownRoleException {
        String serviceName = switch (role) {
            case USER -> CustomerService.SERVICE_NAME;
            case ADMIN -> StaffService.SERVICE_NAME;
            case null -> throw new UnknownRoleException();
        };

        return userServiceMap.get(serviceName);
    }
}
