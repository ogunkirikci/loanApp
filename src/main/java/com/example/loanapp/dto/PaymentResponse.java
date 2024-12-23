package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PaymentResponse {
    private int paidInstallments;
    private BigDecimal totalPaidAmount;
    private boolean isLoanFullyPaid;
    private BigDecimal remainingLoanAmount;
    private List<InstallmentPaymentDetail> paidInstallmentDetails;
} 