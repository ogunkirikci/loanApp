package com.example.loanapp.service;

import com.example.loanapp.dto.CreateLoanRequest;
import com.example.loanapp.exception.LoanValidationException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class LoanValidationService {
    
    private static final List<Integer> VALID_INSTALLMENTS = Arrays.asList(6, 9, 12, 24);
    private static final BigDecimal MIN_INTEREST_RATE = new BigDecimal("0.1");
    private static final BigDecimal MAX_INTEREST_RATE = new BigDecimal("0.5");
    
    public void validateLoanRequest(CreateLoanRequest request) {
        validateInstallmentNumber(request.getNumberOfInstallments());
        validateInterestRate(request.getInterestRate());
        validateAmount(request.getAmount());
    }
    
    private void validateInstallmentNumber(Integer numberOfInstallments) {
        if (!VALID_INSTALLMENTS.contains(numberOfInstallments)) {
            throw new LoanValidationException("Number of installments must be one of: " + VALID_INSTALLMENTS);
        }
    }
    
    private void validateInterestRate(BigDecimal interestRate) {
        if (interestRate.compareTo(MIN_INTEREST_RATE) < 0 || 
            interestRate.compareTo(MAX_INTEREST_RATE) > 0) {
            throw new LoanValidationException("Interest rate must be between 0.1 and 0.5");
        }
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanValidationException("Loan amount must be positive");
        }
    }
} 