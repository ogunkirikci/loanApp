package com.example.loanapp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayLoanRequest {
    private Long loanId;
    private BigDecimal amount;
} 