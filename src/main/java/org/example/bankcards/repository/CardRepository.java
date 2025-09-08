package org.example.bankcards.repository;

import jakarta.persistence.LockModeType;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<Card, Long> {
    @EntityGraph(attributePaths = {"customer"})
    Page<Card> findAllByCustomer_Id(long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Page<Card> findAllByCustomer_Login(String login, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    Optional<Card> findByExternalCardIdAndCustomer_Id(String externalCardId, long customerId);

    @EntityGraph(attributePaths = {"customer"})
    Page<Card> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
        SELECT c FROM Card c
            JOIN FETCH c.customer cu
                JOIN FETCH cu.role
                    WHERE cu.id = :customerId AND c.externalCardId = :externalCardId
    """)
    Optional<Card> getCustomerCardForUpdate(
            @Param("externalCardId") String externalCardId,
            @Param("customerId") long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT c FROM Card c JOIN FETCH c.customer cu WHERE cu.id = :customerId AND c.id = :id")
    Optional<Card> getCustomerCardByIdForUpdate(
            @Param("id") long id,
            @Param("customerId") long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT c FROM Card c WHERE c.externalCardId = :externalCardId")
    Optional<Card> getCardForUpdate(@Param("externalCardId") String externalCardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE Card c SET c.cardStatus = :newStatus WHERE c.id = :id")
    void updateCardStatusById(@Param("id") long id,
                              @Param("newStatus") CardStatus newStatus);

    @Query(
            value = "SELECT c.balance FROM card c WHERE c.customer_id = :customerId AND c.externalCardId = :externalCardId",
            nativeQuery = true
    )
    Optional<BigDecimal> getCardBalance(
            @Param("externalCardId") String externalCardId,
            @Param("customerId") long customerId);
}
