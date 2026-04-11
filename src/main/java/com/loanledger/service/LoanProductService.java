package com.loanledger.service;

import com.loanledger.dto.LoanProductDto;
import com.loanledger.dto.LoanProductRequest;
import com.loanledger.entity.LoanProduct;
import com.loanledger.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanProductService {
    private final LoanProductRepository loanProductRepository;

    public LoanProductDto createProduct(LoanProductRequest request) {
        LoanProduct product = new LoanProduct();
        product.setTitle(request.getTitle());
        product.setTotalAmount(request.getTotalAmount());
        product.setInterestRate(request.getInterestRate());
        product.setTenureMonths(request.getTenureMonths());
        
        LoanProduct saved = loanProductRepository.save(product);
        return mapToDto(saved);
    }

    public List<LoanProductDto> getAllProducts() {
        return loanProductRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    private LoanProductDto mapToDto(LoanProduct p) {
        LoanProductDto dto = new LoanProductDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setTotalAmount(p.getTotalAmount());
        dto.setInterestRate(p.getInterestRate());
        dto.setTenureMonths(p.getTenureMonths());
        return dto;
    }
}
