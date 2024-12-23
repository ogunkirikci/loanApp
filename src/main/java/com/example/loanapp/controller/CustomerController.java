package com.example.loanapp.controller;

import com.example.loanapp.dto.CreateCustomerRequest;
import com.example.loanapp.dto.RiskAnalysisDTO;
import com.example.loanapp.model.Customer;
import com.example.loanapp.service.CustomerService;
import com.example.loanapp.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final LoanService loanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customer> createCustomer(@RequestBody CreateCustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }

    @GetMapping("/{customerId}/risk-analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskAnalysisDTO> analyzeCustomerRisk(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanService.analyzeCustomerRisk(customerId));
    }
} 