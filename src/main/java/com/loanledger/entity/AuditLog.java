package com.loanledger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // Can be "SYSTEM" for background tasks
    private String action; // e.g., "APPLY_FOR_LOAN"
    private String details; // e.g., "Loan Product ID: 123"
    private String status; // "SUCCESS" or "FAILED"
    private String error; // Error message if any
    private LocalDateTime timestamp;
    private String clientIp;
    private Long executionTime;
}
