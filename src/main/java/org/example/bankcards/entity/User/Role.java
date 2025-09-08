package org.example.bankcards.entity.User;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.entity.User.Staff.Staff;

import java.util.List;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true)
    private RoleType roleType;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Staff> staffList;


    public Role(RoleType roleType) {
        this.roleType = roleType;
    }
}
