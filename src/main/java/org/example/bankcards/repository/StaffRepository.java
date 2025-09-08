package org.example.bankcards.repository;

import org.example.bankcards.entity.User.Staff.Staff;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface StaffRepository extends CrudRepository<Staff, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Staff s SET s.lastLogin = :lastLogin WHERE s.id = :id")
    void updateLastLogin(@Param("id") long id,
                         @Param("lastLogin") Instant instant);

    @EntityGraph(attributePaths = {"role"})
    Optional<Staff> findStaffByLogin(String login);

    @EntityGraph(attributePaths = {"role"})
    Optional<Staff> findStaffById(long id);
}
