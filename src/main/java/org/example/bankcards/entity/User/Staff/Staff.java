package org.example.bankcards.entity.User.Staff;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.bankcards.entity.User.AbstractUser;

@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "staff")
public class Staff extends AbstractUser {

}
