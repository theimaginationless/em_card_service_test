package org.example.bankcards.repository;

import org.example.bankcards.entity.CardTransferHistory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface CardTransferHistoryRepository extends CrudRepository<CardTransferHistory, Long> {
    @Modifying
    @Query(value = """
                INSERT INTO card_transfer_history (customer_initiator_id, card_from_id, card_to_id, created_at, amount) 
                            VALUES (:customerId, :cardFromId, :cardToId, :createdAt, :amount)
            """, nativeQuery = true)
    void insertTransfer(
            @Param("customerId") long customerId,
            @Param("cardFromId") long cardFromId,
            @Param("cardToId") long cardToId,
            @Param("createdAt") Instant createdAt,
            @Param("amount") BigDecimal amount);
}
