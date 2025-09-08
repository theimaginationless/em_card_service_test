package org.example.bankcards.service.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.dto.StaffDto;
import org.example.bankcards.exception.StaffNotFoundException;
import org.example.bankcards.mapper.UserMapper;
import org.example.bankcards.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffDaService {

    private final StaffRepository staffRepository;

    public StaffDto getStaffDtoById(long id) throws StaffNotFoundException {
        return staffRepository.findStaffById(id)
                .map(UserMapper::staffEntityToDto)
                .orElseThrow(StaffNotFoundException::new);
    }

    public StaffDto getStaffDtoByLogin(String login) throws StaffNotFoundException {
        return staffRepository.findStaffByLogin(login)
                .map(UserMapper::staffEntityToDto)
                .orElseThrow(StaffNotFoundException::new);
    }

    public void refreshLastLoginById(long id) {
        staffRepository.updateLastLogin(id, Instant.now());
    }
}
