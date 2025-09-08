package org.example.bankcards.repository;

import jakarta.persistence.LockModeType;
import org.example.bankcards.entity.User.Customer.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    @EntityGraph(attributePaths = {"role"})
    Optional<Customer> findCustomerByLogin(String login);

    @Lock(value = LockModeType.PESSIMISTIC_READ)
    @Query("""
    SELECT cu FROM Customer cu
        JOIN FETCH cu.role
            WHERE cu.login = :login
    """)
    Optional<Customer> getCustomerByLoginForShare(@Param("login") String login);

    @EntityGraph(attributePaths = {"role"})
    Optional<Customer> findCustomerById(long id);

    @Modifying
    @Transactional
    @Query("UPDATE Customer cu SET cu.lastLogin = :lastLogin WHERE cu.id = :id")
    void updateLastLogin(@Param("id") long id,
                         @Param("lastLogin") Instant instant);
}
