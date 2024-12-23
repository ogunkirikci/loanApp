package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanHistoryDTO {
    private LocalDateTime transactionDate;
    private String transactionType; // CREATION, PAYMENT
    private BigDecimal amount;
    private BigDecimal remainingDebt;
    private String description;
} 