package org.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.User.Customer.Customer;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "card_transfer_history")
public class CardTransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_initiator_id")
    private Customer customerInitiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_from_id")
    private Card cardFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_to_id")
    private Card cardTo;

    @Column(name = "created_at")
    private Instant createdAt;

    private BigDecimal amount;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
