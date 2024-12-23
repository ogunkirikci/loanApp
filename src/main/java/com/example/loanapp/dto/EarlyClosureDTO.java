package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EarlyClosureDTO {
    private BigDecimal totalRemainingDebt;
    private BigDecimal earlyClosureAmount;
    private BigDecimal totalDiscount;
    private BigDecimal savedInterest;
    private LocalDate closureDate;
    private String paymentInstructions;
} 