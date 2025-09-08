package org.example.bankcards.service;


import lombok.RequiredArgsConstructor;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.entity.User.Role;
import org.example.bankcards.exception.RoleNotFoundException;
import org.example.bankcards.repository.RoleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Cacheable(value = "roleByType", key = "#type")
    public Role getRoleByType(RoleType type) throws RoleNotFoundException {
        return roleRepository.findRoleByRoleType(type)
                .orElseThrow(RoleNotFoundException::new);
    }
}
