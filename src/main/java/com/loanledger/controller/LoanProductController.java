package com.loanledger.controller;

import com.loanledger.dto.LoanProductDto;
import com.loanledger.dto.LoanProductRequest;
import com.loanledger.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/loan-products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LoanProductController {
    private final LoanProductService loanProductService;

    @PostMapping
    public LoanProductDto createProduct(@RequestBody LoanProductRequest request) {
        return loanProductService.createProduct(request);
    }

    @GetMapping
    public List<LoanProductDto> getAllProducts() {
        return loanProductService.getAllProducts();
    }
}
