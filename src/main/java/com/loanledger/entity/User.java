package com.loanledger.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String name;
    private BigDecimal walletBalance = BigDecimal.ZERO;
    private String notificationToken;
    private int score = 0;

    public enum Role {
        USER, ADMIN
    }
}
