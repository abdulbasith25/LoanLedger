package com.loanledger.repository;

import com.loanledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByUserId(Long userId);
}
