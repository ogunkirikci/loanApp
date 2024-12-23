package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponseDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal loanAmount;
    private Integer numberOfInstallments;
    private LocalDateTime createDate;
    private boolean paid;
    private BigDecimal interestRate;
}