package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DetailedPaymentResponse {
    private int paidInstallments;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalDiscount;
    private BigDecimal totalPenalty;
    private boolean isLoanFullyPaid;
    private List<InstallmentPaymentDetail> paidInstallmentDetails;
    private BigDecimal remainingDebt;
    private LocalDateTime paymentDate;
    private String paymentStatus;
} 