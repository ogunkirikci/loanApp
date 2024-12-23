package com.example.loanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    private BigDecimal loanAmount;
    private Integer numberOfInstallments;
    private LocalDateTime createDate;
    private BigDecimal interestRate;
    private boolean paid;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<LoanInstallment> installments;
} 