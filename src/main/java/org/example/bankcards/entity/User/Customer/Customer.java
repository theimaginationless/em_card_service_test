package org.example.bankcards.entity.User.Customer;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.bankcards.entity.Card.Card;
import org.example.bankcards.entity.User.AbstractUser;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends AbstractUser {
    @OneToMany(mappedBy = "customer")
    private List<Card> cardList;
}
