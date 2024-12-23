package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerLoanDTO {
    private Long id;
    private BigDecimal loanAmount;
    private BigDecimal remainingAmount;
    private Integer numberOfInstallments;
    private LocalDateTime createDate;
    private boolean isPaid;
} 