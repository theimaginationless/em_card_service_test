package org.example.bankcards.repository;

import org.example.bankcards.dto.RoleType;
import org.example.bankcards.entity.User.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findRoleByRoleType(RoleType roleType);
}
