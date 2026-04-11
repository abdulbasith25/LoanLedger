package com.loanledger.repository;

import com.loanledger.entity.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
}
