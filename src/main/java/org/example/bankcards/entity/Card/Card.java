package org.example.bankcards.entity.Card;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankcards.entity.User.Customer.Customer;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "cards")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "external_card_id", nullable = false, unique = true)
    private String externalCardId;

    @Column(name = "card_number", nullable = false, length = 16, unique = true, updatable = false)
    private String cardNumber;

    @Column(name = "expiry_month", nullable = false)
    private int expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private int expiryYear;

    @Column(name = "card_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CardStatus cardStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", updatable = false)
    private Customer customer;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() {
        this.externalCardId = UUID.randomUUID().toString().replace("-", "");
    }
}
