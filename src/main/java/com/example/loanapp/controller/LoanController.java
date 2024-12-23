package com.example.loanapp.controller;

import com.example.loanapp.dto.CreateLoanRequest;
import com.example.loanapp.dto.PayLoanRequest;
import com.example.loanapp.dto.PaymentResponse;
import com.example.loanapp.dto.LoanInstallmentDTO;
import com.example.loanapp.dto.CustomerLoanDTO;
import com.example.loanapp.dto.LoanHistoryDTO;
import com.example.loanapp.dto.PaymentPlanDTO;
import com.example.loanapp.dto.EarlyClosureDTO;
import com.example.loanapp.model.Loan;
import com.example.loanapp.service.LoanService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Loan> createLoan(@RequestBody CreateLoanRequest request) {
        return ResponseEntity.ok(loanService.createLoan(request));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerLoanDTO>> getCustomerLoans(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanService.getCustomerLoans(customerId));
    }

    @GetMapping("/{loanId}/installments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanInstallmentDTO>> getLoanInstallments(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanInstallments(loanId));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> payLoan(@RequestBody PayLoanRequest request) {
        return ResponseEntity.ok(loanService.payLoan(request));
    }

    @GetMapping("/{loanId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanHistoryDTO>> getLoanHistory(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanHistory(loanId));
    }

    @GetMapping("/{loanId}/payment-plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentPlanDTO>> getPaymentPlan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getPaymentPlan(loanId));
    }

    @GetMapping("/{loanId}/early-closure-calculation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EarlyClosureDTO> calculateEarlyClosure(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.calculateEarlyClosure(loanId));
    }
} 