package com.example.loanapp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateLoanRequest {
    private Long customerId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer numberOfInstallments;
} 