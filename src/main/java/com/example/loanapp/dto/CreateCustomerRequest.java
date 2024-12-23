package com.example.loanapp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateCustomerRequest {
    private String name;
    private String surname;
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;
} 