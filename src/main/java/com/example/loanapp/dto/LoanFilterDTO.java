package com.example.loanapp.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanFilterDTO {
    private Integer numberOfInstallments;
    private Boolean isPaid;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
} 