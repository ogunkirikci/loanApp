package com.example.loanapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentCalculation {
    private BigDecimal adjustedAmount;
    private long daysDifference;
} 