package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class InstallmentPaymentDetail {
    private Long installmentId;
    private LocalDate dueDate;
    private BigDecimal originalAmount;
    private BigDecimal paidAmount;
    private boolean wasLate;
    private BigDecimal lateFee;
    private BigDecimal earlyPaymentDiscount;
} 