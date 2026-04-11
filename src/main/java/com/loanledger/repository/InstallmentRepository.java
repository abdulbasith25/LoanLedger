package com.loanledger.repository;

import com.loanledger.entity.Installment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
    List<Installment> findByLoanId(Long loanId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Installment i WHERE i.id = :id")
    Optional<Installment> findByIdWithLock(Long id);
}
