package org.example.bankcards.service.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.StaffDto;
import org.example.bankcards.exception.StaffNotFoundException;
import org.example.bankcards.service.UserService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService implements UserService<StaffDto> {

    public static final String SERVICE_NAME = "STAFF_SERVICE";
    private final StaffDaService staffDaService;

    @Override
    public StaffDto getUserByLogin(String login) throws StaffNotFoundException {
        return staffDaService.getStaffDtoByLogin(login);
    }

    @Override
    public StaffDto getUserById(long id) throws StaffNotFoundException {
        return staffDaService.getStaffDtoById(id);
    }

    @Override
    public void refreshLastLoginById(long id) {
        staffDaService.refreshLastLoginById(id);
    }
}
