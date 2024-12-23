package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentPlanDTO {
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal installmentAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal remainingPrincipal;
    private boolean isPaid;
} 