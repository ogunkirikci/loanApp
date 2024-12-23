package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class RiskAnalysisDTO {
    private Long customerId;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private BigDecimal totalDebt;
    private BigDecimal unusedCreditLimit;
    private Integer activeLoans;
    private Integer latePayments;
    private Double creditScore;
    private String recommendation;
} 