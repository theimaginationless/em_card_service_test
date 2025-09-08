package org.example.bankcards.entity.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@MappedSuperclass
public abstract class AbstractUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String login;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "hashed_password")
    private String hashedPassword;

    // Personal Encrypted JWT Secret
    @Column(name = "pe_jwt_secret")
    private String peJwtSecret;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
