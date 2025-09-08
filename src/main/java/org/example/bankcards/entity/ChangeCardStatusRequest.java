package org.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.Card.CardStatus;
import org.example.bankcards.entity.User.Customer.Customer;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "change_card_status_request")
public class ChangeCardStatusRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "request_id", unique = true, updatable = false, nullable = false)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_customer_id", nullable = false, updatable = false)
    private Customer createdByCustomer;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "card_id", updatable = false, nullable = false)
    private Card card;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "new_card_status", updatable = false, nullable = false)
    private CardStatus newCardStatus;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private RequestStatus requestStatus;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public enum RequestStatus {
        NEW,
        PROCESSING,
        DONE,
        REJECTED
    }
}
